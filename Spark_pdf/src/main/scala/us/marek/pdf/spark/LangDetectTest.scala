package us.marek.pdf.spark

import com.cybozu.labs.langdetect.{ Detector, DetectorFactory }

/**
 * @author Marek Kolodziej
 * @since 1/27/2015
 */
object LangDetectTest extends App {

  def path(file: String) = s"/Users/mkolodziej/Downloads/$file"
  def fromFile(file: String) = scala.io.Source.fromFile(path(file)).getLines.mkString("\n")

  DetectorFactory.loadProfile("/Users/mkolodziej/Downloads/langdetect/profiles/longtext")

  def detect(text: String) = {

    val detector = DetectorFactory.create()
    detector.append(text)
    detector.detect()
  }

  val polish = fromFile("a.txt")
  val english = fromFile("b.txt")

  println(s"Polish: ${detect(polish)}")
  println(s"English: ${detect(english)}")

}
