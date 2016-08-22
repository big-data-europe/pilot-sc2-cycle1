package us.marek.pdf.inputformat

import java.io._
import org.apache.hadoop.io.Writable

/*
 This is horribly imperative because we're implementing Writable from the Hadoop Java API :(
 */

/**
 * @author Marek Kolodziej
 * @since Dec. 11, 2014
 */
class PDFBoxParsedPdfWritable(pdf: PDFBoxParsedPdf = null) extends Writable with Serializable {

  // this has to be a var because Hadoop re-uses the object for data serialization and deserialization :(
  private[this] var value = pdf

  override def readFields(in: DataInput): Unit = {

    val dis = in.asInstanceOf[DataInputStream]
    val buffer = new Array[Byte](dis.available)
    val ois = new ObjectInputStream(dis)
    value = ois.readObject().asInstanceOf[PDFBoxParsedPdf]
  }

  override def write(out: DataOutput): Unit = {

    val baos = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(baos)
    oos.writeObject(value)
    out.write(baos.toByteArray)
  }

  // The methods below aren't part of Writable, but they are common in Hadoop Writable implementations
  def get(): PDFBoxParsedPdf = value

  def set(newVal: PDFBoxParsedPdf) = this.value = newVal

}

