package eu.bde.sc6.viticulture.parser.impl;

import eu.bde.sc6.viticulture.parser.api.ViticultureDataParser;
import eu.bde.sc6.viticulture.parser.api.ViticultureDataParserRegistry;
import eu.bde.sc6.viticulture.parser.api.UnknownViticultureDataParserException;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 *
 * @author http://www.turnguard.com/turnguard
 */
public class ViticultureDataParserRegistryImpl implements ViticultureDataParserRegistry {
    
    private static ViticultureDataParserRegistryImpl registry;
    private final ServiceLoader<ViticultureDataParser> loader;

    private ViticultureDataParserRegistryImpl() {
        loader = ServiceLoader.load(ViticultureDataParser.class);
    }

    public static synchronized ViticultureDataParserRegistryImpl getInstance() {
        if (registry == null) {
            registry = new ViticultureDataParserRegistryImpl();
        }
        return registry;
    }

    @Override
    public ViticultureDataParser getViticultureDataParser(String identifier) throws UnknownViticultureDataParserException {
        for (ViticultureDataParser parser : this.loader) {
            if(parser.getIdentifier().equals(identifier)){
                return parser;
            }
        }
        throw new UnknownViticultureDataParserException();
    }

    @Override
    public ViticultureDataParser getViticultureDataParserForFileName(String fileName) throws UnknownViticultureDataParserException {
        for (ViticultureDataParser parser : this.loader) {
            if(parser.canHandleByFileName(fileName)){
                return parser;
            }
        }
        throw new UnknownViticultureDataParserException();
    }
}
