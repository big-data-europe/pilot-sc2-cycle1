package us.marek.pdf.spark

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.util.{ TextPosition, PDFTextStripper }
import scala.collection.JavaConversions._
import us.marek.pdf.inputformat.MyPDFTextStripper
import org.apache.pdfbox.ExtractImages
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDResources
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage

/**
 * @author Marek Kolodziej
 * @since 1/26/2015
 */
object PdfBoxTest extends App {

  val doc = PDDocument.load("/home/hajira/Documents/spark-setup/spark_pdf/src/test/resources/Genotyping.and.Phenotyping.of.the.Potential.Clones.pdf")
  val numberOfPages = doc.getNumberOfPages

 // var list= doc.getDocumentCatalog().getAllPages();
  
  


 // doc.get
  def getStripper(doc: PDDocument, startPage: Int = 0, endPage: Int = doc.getNumberOfPages): MyPDFTextStripper = {
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

    removeTrailingMeta(s.substring(s.indexOf("+") + 1, s.length))
  }

  def getPageFontStats(doc: PDDocument)(pageNumber: Int): Map[String, Int] = {

    val stripper = getStripper(doc = doc, startPage = pageNumber, endPage = pageNumber + 1)
    stripper.getText(doc) // need to have this side effect :(
    val info= doc.getDocumentInformation()
    println(info.getTitle())
    
    val chars = stripper.myGetCharactersByArticle
    val allTextPos = chars.flatten[TextPosition]

    allTextPos.groupBy(x => normalizeFontName(x.getFont.getBaseFont)).map {

      case (font: String, pos: Seq[TextPosition]) => {

        (font, pos.map(x => x.getCharacter.filter(c => c != ' ' && c != "\n" && c != "\t").length).sum)
      }
    }
  }

  val perPageStats = (1 to numberOfPages).map(getPageFontStats(doc))

  val wholeDocStats = perPageStats.flatten.toList.groupBy(_._1).map { case (k, v) => k -> v.map(_._2).sum }

  println(s"""Whole-document stats:
              |
              |Number of pages: $numberOfPages
              |
              |Fonts (in descending order or prevalence):
              |
              |${wholeDocStats.toList.sortWith((a, b) => a._2 > b._2).map(tuple => s"${tuple._1}: ${tuple._2}").mkString("\n")}
              |""".stripMargin)

    val fullText = getStripper(doc, 1, doc.getNumberOfPages).getText(doc)
  //  println(fullText)

}