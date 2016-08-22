package us.marek.pdf.inputformat

import org.apache.pdfbox.util.{ TextPosition, PDFTextStripper }

class MyPDFTextStripper extends PDFTextStripper {

  import java.util.{ List, Vector }

  def myGetCharactersByArticle: Vector[List[TextPosition]] = getCharactersByArticle

}