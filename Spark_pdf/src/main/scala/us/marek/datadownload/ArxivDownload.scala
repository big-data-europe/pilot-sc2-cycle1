package us.marek.datadownload

import scala.util.Try
import xml.{ Elem, XML }
import scala.io.Source
import scala.util.{ Failure, Success }
import scalax.io.Resource

/**
 * PDF download from Arxiv
 *
 * @author Marek Kolodziej
 * @since Dec. 11, 2014
 */
object ArxivDownload extends App {

  val categories = Map(
    "machineLearning" -> "stat.ML",
    "quantumPhysics" -> "quant-ph")

  //  val categories = Map(
  //    "genomics" -> "q-bio.GN",
  //    "machineLearning" -> "stat.ML",
  //    "numberTheory" -> "math.NT",
  //    "quantumPhysics" -> "quant-ph")

  def downloadFile(url: String, localPath: String): Unit =
    Try(Resource.fromURL(url).inputStream.copyDataTo(Resource.fromFile(localPath))) match {
      case Success(_) => println(s"Downloaded $url into $localPath")
      case Failure(_) => // ignore
    }

  def links(url: String) = {
    println(s"Accessing URL $url")
    XML.loadString(Source.fromURL(url).mkString("")) \\ "link"
  }

  def pdfLinks(url: String) = links(url).flatMap { link =>
    link.attribute("href").filter(_.toString.contains("pdf")).map(node => s"${node.toString}.pdf")
  }.zipWithIndex

  def url(category: String, maxResults: Int) =
    s"http://export.arxiv.org/api/query?search_query=cat:$category&max_results=$maxResults"

  def localPath(category: String) = s"/Users/mkolodziej/Downloads/pdfs/$category"

  val maxResults = 100

  categories.foreach {
    case (catName, catCode) =>

      pdfLinks(url(catCode, maxResults)).foreach {
        case (link, num) =>

          val filePath = s"${localPath(catName)}/$num.pdf"
          downloadFile(link, filePath)
      }

  }
}