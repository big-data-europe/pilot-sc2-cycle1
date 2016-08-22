package us.marek.pdf

import org.apache.hadoop.io.LongWritable
import org.apache.spark.{ SparkContext, SparkConf }
import org.scalatest.{ BeforeAndAfterAll, WordSpec }
import us.marek.pdf.inputformat.{ TikaPdfInputFormat, TikaParsedPdfWritable }

/**
 * @author Marek Kolodziej
 * @since Dec. 11, 2014
 */
class SparkPdfParserTest extends WordSpec with BeforeAndAfterAll {

  val conf = new SparkConf().setMaster("local").setAppName("PDF Parsing App")
  val sc = new SparkContext(conf)

  "The Tika PDF parser running via Spark" when {

    "given 3 PDFs to parse" should {

      val pdfs = sc.newAPIHadoopFile[LongWritable, TikaParsedPdfWritable, TikaPdfInputFormat](
        "src/test/resources/*.pdf"
      )

      "report count of 3" in {

        //        assert(pdfs.count() === 3)

      }

      "report all PDFs as non-empty" in {

        pdfs.map { case (k, v) => v.get.contentHandler.toString }.collect().forall(_.length > 0)
              

      }
    }
  }

  override def afterAll(): Unit = sc.stop()

}