package eu.bde.sc6.viticulture.parser;

import eu.bde.sc6.viticulture.parser.api.TransformationException;
import eu.bde.sc6.viticulture.parser.api.UnknownViticultureDataParserException;
import eu.bde.sc6.viticulture.parser.api.ViticultureDataParser;
import eu.bde.sc6.viticulture.parser.impl.ViticultureDataParserRegistryImpl;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Statement;

/**
 *
 * @author turnguard
 */
public class ViticultureBaseParserTest {
    
    public ViticultureBaseParserTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Currently we only have one single parser, so it should be returned by the registry
     * for everything that is a pdf
     * @throws UnknownViticultureDataParserException 
     */
    //@Test
    public void testAvailableInRegistry() throws UnknownViticultureDataParserException {
        ViticultureDataParser parser = ViticultureDataParserRegistryImpl
                .getInstance().getViticultureDataParserForFileName("xxx.pdf");
        assertNotNull(parser);
    }
    
    //@Test
    public void testParseSimpleFile() throws UnknownViticultureDataParserException, IOException, TransformationException{
        String fileName = "simple-openoffice.pdf";
        byte[] file = IOUtils.toByteArray(ViticultureBaseParserTest.class.getResourceAsStream("/".concat(fileName)));
        ViticultureDataParser parser = ViticultureDataParserRegistryImpl
                .getInstance().getViticultureDataParserForFileName(fileName);        
        parser.transform(fileName, file).forEach( s -> {
            System.out.println(s);
        });
    }
     
    @Test
    public void testPDFExtraction() throws IOException{        
        PDDocument pdDocument = null;
        PDDocumentInformation info = null;
        try {            
            //pdDocument = PDDocument.load(ViticultureBaseParserTest.class.getResourceAsStream("/simple-openoffice.pdf"));
            /* note: 1.pdf doesn't contain any text, simple-openoffice.pdf does contain some text */
            pdDocument = PDDocument.load(ViticultureBaseParserTest.class.getResourceAsStream("/1.pdf"));
            info = pdDocument.getDocumentInformation();
            System.out.println( "Title=" + info.getTitle() );            
            System.out.println( "Author=" + info.getAuthor() );
            System.out.println( "Subject=" + info.getSubject() );
            System.out.println( "Keywords=" + info.getKeywords() );
            System.out.println( "Creator=" + info.getCreator() );
            System.out.println( "Producer=" + info.getProducer() );
            System.out.println( "Creation Date=" + info.getCreationDate() );
            System.out.println( "Modification Date=" + info.getModificationDate());            
            System.out.println(new PDFTextStripper().getText(pdDocument));
        } finally{
            if(pdDocument!=null){
                pdDocument.close();
            }
        }
    }
    
    //@Test
    public void testAllPDFsForDataErrors() throws IOException{
        
        File rootDirectory = new File(ViticultureBaseParserTest.class.getClass().getResource("/").getFile());
        Path path = Paths.get(rootDirectory.toURI());
        
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {                         
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if(file.toString().endsWith("pdf")){
                    try {
                        ViticultureDataParser parser = ViticultureDataParserRegistryImpl.getInstance().getViticultureDataParserForFileName(file.getFileName().toString());
                        List<Statement> states = parser.transform(file.toString(), Files.readAllBytes(file));
                        System.out.println(file.toString() + " " + states.size());
                        for(Statement state : states){
                            System.out.println("\t"+state);
                        }
                    } catch (TransformationException | RuntimeException ex) {
                        System.out.println("PROBLEMATIC FILE: " + file.toAbsolutePath());                    
                    } catch (UnknownViticultureDataParserException ex) {
                        System.out.println("UNKNOWN PARSER FOR: " + file.toAbsolutePath());
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
        
}
