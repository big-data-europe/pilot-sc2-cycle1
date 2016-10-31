package eu.bde.sc6.viticulture.parser.base;

import eu.bde.sc6.viticulture.parser.api.ViticultureDataParser;
import eu.bde.sc6.viticulture.parser.api.TransformationException;
import eu.bde.sc6.viticulture.parser.vocabulary.SIOC;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;
import org.openrdf.model.vocabulary.RDF;

/**
 *  Sample implementation of a ViticultureDataParser.
 * 
 * @author http://www.turnguard.com/turnguard
 */
public class ViticulturePDFBaseParser implements ViticultureDataParser {
    
    /**
     * if the fileName ends in pdf (e.g. xxx.pdf) this parser will be used.
     * in case other parsers are developed this parser's identifier must be adapted to not match any pdf.
     * (it is also possible to remove the fully qualified class name from
     * here /src/main/resources/META-INF/services/eu.bde.sc6.viticulture.parser.api.ViticultureDataParser)
     * new parsers must also be registered in above service description.
     */
    private final static String IDENTIFIER = ".*pdf$";    
    private final static String INSTANCE_NAMESPACE = "http://viticulture.big-data-europe.eu/sc2/";
        
    @Override
    public List<Statement> transform(String fileName, byte[] file) throws TransformationException {
        
        /**
         * pdfBox stuff
         */        
        PDDocument pdDocument = null;
        PDFTextStripper pdfTextStripper = null;

        /**
         * output stuff
         */
        List<Statement> data = new ArrayList<>();
        Literal source = new LiteralImpl(fileName);
        URI subject = new URIImpl(INSTANCE_NAMESPACE.concat(UUID.randomUUID().toString()));
        try {
            
            /**
             * pdfBox
             */
            pdDocument = PDDocument.load(file);
            pdfTextStripper = new PDFTextStripper();
            /**
             * output
             */
            
            String plainText = pdfTextStripper.getText(pdDocument);
            if(!plainText.trim().equals("")){
                data.add(
                    new StatementImpl(
                            subject,
                            SIOC.CONTENT,
                            new LiteralImpl(plainText)
                    )
                );
            }
            
            data.add(
                new StatementImpl(
                        subject,
                        DCTERMS.SOURCE,
                        source
                )
            ); 
            
            
             // add more statements here, e.g.
               for(PDPage page : pdDocument.getDocumentCatalog().getPages()){
                   for(COSName cosName : page.getResources().getXObjectNames()){
                       if(page.getResources().isImageXObject(cosName)){
                           PDImageXObject image = (PDImageXObject)page.getResources().getXObject(cosName);
                            /**
                             * store image into hdfs and create an apropriate uri here
                             * (instead of UUID.randomUUID()...)
                             */
                            URI imageURI = new URIImpl("urn:image:"+UUID.randomUUID().toString());
                            /**
                             * uncomment and adapt output directory to check the images on the local filesystem 
                             */
                            /*
                            ImageIO.write(
                                image.getImage(), 
                                image.getSuffix(), 
                                new FileOutputStream(
                                    new File(
                                        "/home/turnguard/Downloads/"
                                            .concat(imageURI.getLocalName())
                                            .concat(".")
                                            .concat(image.getSuffix())
                                    )
                                )
                            );
                            */
                            data.add(new StatementImpl(subject, DCTERMS.HAS_PART, imageURI));
                            data.add(new StatementImpl(imageURI,RDF.TYPE,FOAF.IMAGE));
                       }
                   }
                }   
              
             
            
            
        } catch (IOException ex) {
            throw new TransformationException(ex);
        } finally {
            if(pdDocument!=null){
                try {
                    pdDocument.close();
                } catch (IOException ex) {}
            }            
        }
        return data;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public boolean canHandleByFileName(String fileName) {
        return fileName.matches(IDENTIFIER);
    }

}
