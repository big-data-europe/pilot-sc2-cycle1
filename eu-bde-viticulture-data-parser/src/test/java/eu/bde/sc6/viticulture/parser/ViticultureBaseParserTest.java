package eu.bde.sc6.viticulture.parser;

import eu.bde.sc6.viticulture.parser.api.TransformationException;
import eu.bde.sc6.viticulture.parser.api.UnknownViticultureDataParserException;
import eu.bde.sc6.viticulture.parser.api.ViticultureDataParser;
import eu.bde.sc6.viticulture.parser.impl.ViticultureDataParserRegistryImpl;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
    @Test
    public void testAvailableInRegistry() throws UnknownViticultureDataParserException {
        ViticultureDataParser parser = ViticultureDataParserRegistryImpl
                .getInstance().getViticultureDataParserForFileName("xxx.pdf");
        assertNotNull(parser);
    }
    
    @Test
    public void testParseSimpleFile() throws UnknownViticultureDataParserException, IOException, TransformationException{
        String fileName = "simple-openoffice.pdf";
        byte[] file = IOUtils.toByteArray(ViticultureBaseParserTest.class.getResourceAsStream("/".concat(fileName)));
        ViticultureDataParser parser = ViticultureDataParserRegistryImpl
                .getInstance().getViticultureDataParserForFileName(fileName);        
        parser.transform(fileName, file).forEach( s -> {
            System.out.println(s);
        });
    }
        
}
