package eu.bde.sc6.viticulture.parser.base;

import eu.bde.sc6.viticulture.parser.api.ViticultureDataParser;
import eu.bde.sc6.viticulture.parser.api.TransformationException;
import eu.bde.sc6.viticulture.parser.vocabulary.SIOC;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.apache.pdfbox.util.PDFTextStripper;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.FOAF;

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
         * input stuff
         */
        InputStreamReader inputStreamReader = null;
        ByteArrayInputStream byteArrayInputStream = null;        
        /**
         * pdfBox stuff
         */
        PDFParser pdfParser = null;
        PDFTextStripper pdfTextStripper = null;
        /**
         * output stuff
         */
        List<Statement> data = new ArrayList<>();
        Literal source = new LiteralImpl(fileName);
        URI subject = new URIImpl(INSTANCE_NAMESPACE.concat(UUID.randomUUID().toString()));
        try {
            /**
             * input
             */
            byteArrayInputStream = new ByteArrayInputStream(file);
            inputStreamReader = new InputStreamReader(byteArrayInputStream, "UTF-8");
            /**
             * pdfBox
             */
            pdfParser = new PDFParser(byteArrayInputStream);
            pdfParser.parse();
            pdfTextStripper = new PDFTextStripper();
            /**
             * output
             */
            data.add(
                new StatementImpl(
                        subject,
                        SIOC.CONTENT,
                        new LiteralImpl(pdfTextStripper.getText(pdfParser.getPDDocument()))
                )
            );
            data.add(
                new StatementImpl(
                        subject,
                        DCTERMS.SOURCE,
                        source
                )
            ); 
            
            /**
             * add more statements here, e.g.
             *  for(Object page : pdfParser.getPDDocument().getDocumentCatalog().getAllPages()){
             *       for(PDXObjectImage image : ((PDPage)page).getResources().getImages().values()){
             *           data.add(new StatementImpl(subject,FOAF.IMAGE,new URIImpl("urn:image:"+UUID.randomUUID().toString())));
             *       }
             *   }   
             * 
             */
            
            
        } catch (IOException ex) {
            throw new TransformationException(ex);
        } finally {
            if(inputStreamReader!=null){
                try {
                    inputStreamReader.close();
                } catch (IOException ex) {}
            }
            if(byteArrayInputStream!=null){
                try {
                    byteArrayInputStream.close();
                } catch (IOException ex) {}
            }   
            
            if(pdfParser != null){
                pdfParser.clearResources();
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
