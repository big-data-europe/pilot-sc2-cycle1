package eu.bde.sc6.viticulture.parser.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.PDFTextStripper;

public class Test {
	static String getText(File pdfFile) throws IOException {
	    PDDocument doc = PDDocument.load(pdfFile);
	    return new PDFTextStripper().getText(doc);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		BasicConfigurator.configure();
		PDFTextStripper pdfStripper = null;
		PDDocument doc = null;
		try {
			doc = PDDocument.load("/home/hajira/Documents/spark-setup/spark_pdf/src/test/resources/Downloaded/1.pdf");
			PDDocumentInformation info = doc.getDocumentInformation();
			System.out.println( "Title=" + info.getTitle() );
			//System.out.println("fileXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
			System.out.println( "Author=" + info.getAuthor() );
			System.out.println( "Subject=" + info.getSubject() );
			System.out.println( "Keywords=" + info.getKeywords() );
			System.out.println( "Creator=" + info.getCreator() );
			System.out.println( "Producer=" + info.getProducer() );
			System.out.println( "Creation Date=" + info.getCreationDate() );
			System.out.println( "Modification Date=" + info.getModificationDate());
			System.out.println(new PDFTextStripper().getText(doc));
			
			List pages = doc.getDocumentCatalog().getAllPages();
		    Iterator iter = pages.iterator(); 
		    int i =1;
		    String name = null;

		    while (iter.hasNext()) {
		        PDPage page = (PDPage) iter.next();
		        PDResources resources = page.getResources();
		        Map pageImages = resources.getImages();
		        if (pageImages != null) { 
		            Iterator imageIter = pageImages.keySet().iterator();
		            while (imageIter.hasNext()) {
		                String key = (String) imageIter.next();
		                PDXObjectImage image = (PDXObjectImage) pageImages.get(key);
		                image.write2file("/home/hajira/Documents/spark-setup/spark_pdf/src/test/resources/Downloaded/1/" + i);
		                i ++;
		            }
		        }
		    }
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		
		
	
		
		
	}

}
