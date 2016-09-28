package us.marek.pdf.inputformat

import org.apache.pdfbox.pdmodel.PDDocument

case class PDFBoxParsedPdf(document: PDDocument, isEncrypted: Boolean = false)