package us.marek.pdf.spark

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.util.{ TextPosition, PDFTextStripper }
import scala.collection.JavaConversions._
import us.marek.pdf.inputformat.MyPDFTextStripper
import org.apache.pdfbox.ExtractImages
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDResources
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage
import scala.util.Try
import xml.{ Elem, XML }
import scala.io.Source
import scala.util.{ Failure, Success }
import scalax.io.Resource
import scala.io.Source
import scala.collection.mutable.ListBuffer
 import java.nio.file.{Path, Paths, Files}

/**
 * @author Hajira Jabeen
 * @since 1/05/2016
 */
object Sc2Extractor extends App {

 //val doc = PDDocument.load("/home/hajira/Documents/spark-setup/spark_pdf/src/test/resources/G1.pdf")
  //val numberOfPages = doc.getNumberOfPages

 // var list= doc.getDocumentCatalog().getAllPages();
  /////////////////// eg 1////////////////
//  def using[A <: { def close(): Unit }, B](resource: A)(f: A => B): B =
//        try {
//            f(resource)
//        } finally {
//            resource.close()
//        }
//        
// def readTextFile(filename: String): Option[List[String]] = {
//    try {
//        val lines = using(Source.fromFile(filename)) { source =>
//            (for (line <- source.getLines) yield line).toList
//        }
//        Some(lines)
//    } catch {
//        case e: Exception => None
//  }
// }
//    /////////////end eg 1////////////////////
 /////////777eg 2//////////////////
 //def readCsvFile(filename: String): ListBuffer[String] = {
 val list = new ListBuffer[String]
 val bufferedSource = Source.fromFile("/home/hajira/Documents/spark-setup/spark_pdf/src/test/resources/resource_images.csv")
   for (line <- bufferedSource.getLines) {
    list+=line
    }
 bufferedSource.close
 
   
    
  
  //list
  //}
 var i=0
 for (name <- list) 
   {
  // println(name)
   val cols = (name.split(",").map(_.trim))
    // do whatever you want with the columns here
//   println("line"+ i)
    //println(s"${cols(0)}|${cols(1)}}")
   i=i+1
//  Resource.fromURL(cols(0)).inputStream.copyDataTo(Resource.fromFile("/home/hajira/Documents/spark-setup/spark_pdf/src/test/resources/Downloaded/"+i+".pdf"))

//  def downloadFile(url: String, localPath: String): Unit =
    Try(Resource.fromURL(cols(0)).inputStream.copyDataTo(Resource.fromFile("/home/hajira/Documents/spark-setup/spark_pdf/src/test/resources/Downloaded/"+i+".pdf"))) match {
      case Success(_) => println(s"Downloaded"+ cols(0)+ " into $localPath")
      case Failure(_) => println(s"Something wrong here")// ignore
    }  
  // }
 

// val i =32
 val doc = PDDocument.load("/home/hajira/Documents/spark-setup/spark_pdf/src/test/resources/Downloaded/"+i+".pdf")
  
  val numberOfPages = doc.getNumberOfPages
  
  def getStripper(doc: PDDocument, startPage: Int = 0, endPage: Int = doc.getNumberOfPages): MyPDFTextStripper = {
    val stripper = new MyPDFTextStripper()
    stripper.setStartPage(startPage)
    stripper.setEndPage(endPage)
    stripper
    }
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
    //println(info.getTitle())
    
   // println(stripper.getText(doc))
    //println( "Page Count=" + doc.getNumberOfPages() );
    var list: java.util.List[_]= doc.getDocumentCatalog.getAllPages();
    for(page <- list)
    {
      
      var pdResources = page.asInstanceOf[PDPage].getResources()
     // PDResources pdResources = page.getResources();

    var pageImages = pdResources.asInstanceOf[PDResources].getImages()
     if (pageImages!=null)
     {
       var imageIter = pageImages.keySet().iterator()
       var count=0;
       for(image <- imageIter)
       {
         count=count+1
         var key = image.asInstanceOf[String]
         var pdxObjectImage = pageImages.get(key);
         pdxObjectImage.asInstanceOf[PDXObjectImage].write2file("/home/hajira/Documents/spark-setup/spark_pdf/src/test/resources/Downloaded/"+i+"/Image"+"_" + count)
        }
       
     }
       
                
    }
  
   
//System.out.println( "Title=" + doc.getTitle() )
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
    //
   // println(fullText)

}