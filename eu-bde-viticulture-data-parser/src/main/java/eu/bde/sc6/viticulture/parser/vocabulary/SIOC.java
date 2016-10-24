package eu.bde.sc6.viticulture.parser.vocabulary;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 *
 * @author http://www.turnguard.com/turnguard
 */
public class SIOC {
    
    public static final String NAMESPACE = "http://rdfs.org/sioc/ns#";
    
    /* CLASSES */
    
    /* PREDICATES */
    public static final URI CONTENT = new URIImpl(NAMESPACE.concat("content"));
}
