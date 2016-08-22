package us.marek.pdf.spark

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.util.TextPosition
import org.apache.spark.SparkContext
import SparkContext._

import org.apache.hadoop.io.LongWritable
import org.apache.spark.rdd.RDD
import org.apache.spark.{ SparkContext, SparkConf }
import us.marek.pdf.inputformat._
import scala.collection.JavaConversions._

object PDFBoxDriver {

  def main(args: Array[String]): Unit = {

    val jobStart = System.currentTimeMillis

    val conf = new SparkConf().setMaster("local").setAppName("PDF Parsing App")
    val sc = new SparkContext(conf)

    def getRDD(directory: String) = sc.newAPIHadoopFile[LongWritable, PDFBoxParsedPdfWritable, PDFBoxInputFormat](
      directory
    )

    def getStripper(doc: PDDocument, startPage: Int = 0, endPage: Int): MyPDFTextStripper = {
      val stripper = new MyPDFTextStripper()
      stripper.setStartPage(startPage)
      stripper.setEndPage(endPage)
      stripper
    }

    // e.g. convert "KNHSZO+SegoeUI-BoldItalic" to "SegoeUI"
    def normalizeFontName(s: String): String = {

      def removeTrailingMeta(x: String): String = x.indexOf("-") match {

        case pos if pos > 0 => x.substring(0, pos)
        case _ => x
      }

      if (s == null) "Unknown"
      else removeTrailingMeta(s.substring(s.indexOf("+") + 1, s.length)).replaceAll("[^a-zA-Z]", "")
    }

    def getPageFontStats(doc: PDDocument)(pageNumber: Int): Map[String, Long] = {

      val stripper = getStripper(doc = doc, startPage = pageNumber, endPage = pageNumber + 1)
      stripper.getText(doc) // need to have this side effect :(
      val chars = stripper.myGetCharactersByArticle
      val allTextPos = chars.flatten[TextPosition]

      allTextPos.groupBy(x => normalizeFontName(x.getFont.getBaseFont)).map {

        case (font: String, pos: Seq[TextPosition]) => {

          (font, pos.map(x => x.getCharacter.filter(c => c != ' ' && c != "\n" && c != "\t").length).sum.toLong)
        }
      }
    }

    def numberOfPages(doc: PDDocument) = doc.getNumberOfPages

    def perPageStats(doc: PDDocument) = (1 to numberOfPages(doc)).map(getPageFontStats(doc))

    def wholeDocStats(doc: PDDocument) = perPageStats(doc).flatten.toList
      .groupBy(_._1).map { case (k, v) => k -> v.map(_._2).sum }

    // 12 PDFs starting with 000 - 2,762 total
    val pdfs = getRDD("/home/hajira/Documents/spark-setup/spark_pdf/src/test/resources").map {

      case (l: LongWritable, pdf: PDFBoxParsedPdfWritable) => pdf.get

    }.filter(doc => !doc.isEncrypted).map(_.document).cache()

    val result: List[(String, Long)] = pdfs.map{ case (doc: PDDocument) => wholeDocStats(doc)}
                  .fold(Map[String, Long]()) { case (a, b) =>
      (a.toList ++ b.toList).groupBy(_._1).map {
        case (k, v) => k -> v.map(_._2).reduce(_ + _)
      }
    }.toList.sortWith((a, b) => a._2 > b._2)

    val docCount = pdfs.count()

    println(
      s"""Aggregate character count per font in descending order
         |
         |Number of documents: $docCount
         |
         |${result.map(tuple => s"${tuple._1}: ${tuple._2}").mkString("\n")}
         |
         |The job took ${(System.currentTimeMillis - jobStart) / 1000} seconds
       """.stripMargin)

    sc.stop()

  }

}
