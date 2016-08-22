package us.marek.pdf.spark

import org.apache.spark.SparkContext
import SparkContext._

import org.apache.hadoop.io.LongWritable
import org.apache.spark.rdd.RDD
import org.apache.spark.{ SparkContext, SparkConf }
import us.marek.pdf.inputformat.{ TikaParsedPdfWritable, TikaPdfInputFormat }

object TikaDriver {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local").setAppName("PDF Parsing App")
    val sc = new SparkContext(conf)

    val pdfs = sc.newAPIHadoopFile[LongWritable, TikaParsedPdfWritable, TikaPdfInputFormat](
      "src/test/resources/*.pdf"
    )

    def getRDD(directory: String) = sc.newAPIHadoopFile[LongWritable, TikaParsedPdfWritable, TikaPdfInputFormat](
      s"$directory/*.pdf"
    )

    val base = "/Users/marek/Downloads/pdfs"
    val mlPapers = getRDD(s"$base/ml")
    val qpPapers = getRDD(s"$base/qp")

    // training set
    val mlTrain = getRDD(base + "train/machineLearning")
    val qpTrain = getRDD(base + "train/quantumPhysics")

    // test set
    val mlTest = getRDD(base + "test/machineLearning")
    val qpTest = getRDD(base + "test/quantumPhysics")

    val test = getRDD("src/test/resources/*.pdf")

    //    object Foo extends Serializable

    val corpusCounts: RDD[(String, Int)] = test.flatMap {
      case (_, pdf) =>

        val text = pdf.get().contentHandler.toString
        // get rid of non-words
        val alphasOnly = text.replaceAll("[^\\w ]", "")
        // get rid of multiple whitespaces
        val singleSpaces = alphasOnly.replaceAll("\\s+", " ")
        // make everything lowercase
        val lowerCase = singleSpaces.toLowerCase
        // split on whitespace
        lowerCase.split(" ")
      // stop words
      // stemming etc.

    }.map(word => (word, 1)).reduceByKey(_ + _)

    val corpusWords = corpusCounts.keys.collect()

    //    sc.broadcast(corpusWords)

    corpusCounts.collect().take(10).foreach(println)

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
    sc.stop()
  }

}
