package us.marek.pdf.spark

import org.apache.spark.SparkContext
import SparkContext._

import org.apache.hadoop.io.LongWritable
import org.apache.spark.rdd.RDD
import org.apache.spark.{ SparkContext, SparkConf }
import org.apache.tika.metadata.Metadata
import scala.collection.mutable
import us.marek.pdf.inputformat.{ TikaParsedPdf, TikaParsedPdfWritable, TikaPdfInputFormat }

/**
 * @author Marek Kolodziej
 * @since 1/22/2015
 */
object Wrangling {

  def main(args: Array[String]): Unit = {

    val jobStart = System.currentTimeMillis

    val conf = new SparkConf().setMaster("local").setAppName("PDF Wrangling App")
    val sc = new SparkContext(conf)

    val sample = 0.01

    def getRDD(directory: String, sample: Double = 1.0): RDD[TikaParsedPdf] =
      sc.newAPIHadoopFile[LongWritable, TikaParsedPdfWritable, TikaPdfInputFormat](
        s"$directory/*"
      ) map {
          case (offset, tikaObj) => tikaObj
        } filter {
          elem => elem.get != null && !elem.get.encrypted // && elem.get.metadata()
        } map {
          _.get
        } sample (withReplacement = false, fraction = sample)

    def sanitizeString(s: String) =
      s.toLowerCase
        .replaceAll("[^\\w ]", "") // alphas only
        .replaceAll("\\s+", "") // no spaces

    def metadataFields(meta: Metadata) = meta.names.map(name => (sanitizeString(name), meta get name))

    def metadataFieldsAsString(meta: Metadata) =
      meta.names.map(name => s"name: $name, value: ${meta.get(name)}").mkString("\n")

    // get only ML and QP papers for now
    val base = "/home/hajira/Documents/spark-setup/spark_pdf/src/test"
    //    val mlPapers: RDD[TikaParsedPdf] = getRDD(s"$base/machineLearning")
    //    val qpPapers: RDD[TikaParsedPdf] = getRDD(s"$base/quantumPhysics")
    val nitroPapers: RDD[TikaParsedPdf] = getRDD(directory = s"$base/resources", sample)

    // language detection and filtering for English (Tika)
    val nitroMeta = nitroPapers.map { elem => elem.metadata }

    val cleanMeta = nitroMeta.flatMap { elem => metadataFields(elem) }
      .groupByKey()

    // alphabetically sorted superset of all terms (lower-case, non-alpha characters removed)

    cleanMeta.foreach(elem => println(s"\n$elem\n"))

    // sorted key counts by occurrence (percentage of analyzed documents)

    // sorted key counts by occurrence with non-empty values

    // alphabetically sorted total intersection of keys across all documents

    // alphabetically sorted total intersection of keys across all documents, with non-empty values

    //    nitroMeta.foreach(doc => println(s"\n${metadataFields(doc)}\n"))

    sc.stop()

    val jobEndSeconds = (System.currentTimeMillis - jobStart) / 1000L

    println(s"\nJob duration: $jobEndSeconds seconds\n")
  }

  // non-alpha character removal

  // word segmentation (e.g. Epic)

  // Americanization (e.g. http://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/process/Americanize.html)

  // TF (Spark)

  // IDF (Spark)

  //    // test set
  //    val mlTest = getRDD(base + "test/machineLearning")
  //    val qpTest = getRDD(base + "test/quantumPhysics")
  //
  //    val test = getRDD("src/test/resources/*.pdf")
  //
  //    //    object Foo extends Serializable
  //
  //    val corpusCounts: RDD[(String, Int)] = test.flatMap {
  //      case (_, pdf) =>
  //
  //        val text = pdf.get().contentHandler.toString
  //        // get rid of non-words
  //        val alphasOnly = text.replaceAll("[^\\w ]", "")
  //        // get rid of multiple whitespaces
  //        val singleSpaces = alphasOnly.replaceAll("\\s+", " ")
  //        // make everything lowercase
  //        val lowerCase = singleSpaces.toLowerCase
  //        // split on whitespace
  //        lowerCase.split(" ")
  //      // stop words
  //      // stemming etc.
  //
  //    }.map(word => (word, 1)).reduceByKey(_ + _)
  //
  //    val corpusWords = corpusCounts.keys.collect()
  //
  //    //    sc.broadcast(corpusWords)
  //
  //    corpusCounts.collect().take(10).foreach(println)

  //.collect().take(10).foreach(println)

  //    pdfs.foreach {
  //      case (docNum, writable) =>
  //
  //        println(s"Document # $docNum")
  //        val pdf = writable.get()
  //        val text = pdf.contentHandler.toString
  //        val metadata = pdf.metadata
  //        println(
  //          s"""
  //           Text:
  //           $text
  //
  //           Metadata:
  //           $metadata
  //         """.stripMargin
  //        )
  //
  //    }
  //    println(s"Number of lines in PDF: ${pdfs.count()}")
  //    sc.stop()
  //  }

}
