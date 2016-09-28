package us.marek.pdf.inputformat

import java.io.IOException
import org.apache.hadoop.io.{ LongWritable, Text }
import org.apache.hadoop.mapreduce.{ InputSplit, RecordReader, TaskAttemptContext }
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat

class PDFBoxInputFormat extends FileInputFormat[LongWritable, PDFBoxParsedPdfWritable] {

  // Hadoop Java-land, please excuse annotating checked exceptions :(
  @throws[IOException]
  @throws[InterruptedException]
  def createRecordReader(split: InputSplit, context: TaskAttemptContext): RecordReader[LongWritable, PDFBoxParsedPdfWritable] =
    new PDFBoxRecordReader

}