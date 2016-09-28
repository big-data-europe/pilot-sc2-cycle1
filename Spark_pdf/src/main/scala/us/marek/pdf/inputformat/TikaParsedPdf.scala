package us.marek.pdf.inputformat

import org.apache.tika.metadata.Metadata
import org.apache.tika.sax.BodyContentHandler

/**
 * @author Marek Kolodziej
 * @since Dec. 11, 2014
 *
 * @param contentHandler
 * @param metadata
 */
case class TikaParsedPdf(contentHandler: BodyContentHandler, metadata: Metadata, encrypted: Boolean = false)
