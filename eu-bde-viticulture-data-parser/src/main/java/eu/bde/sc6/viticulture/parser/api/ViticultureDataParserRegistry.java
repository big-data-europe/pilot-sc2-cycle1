package eu.bde.sc6.viticulture.parser.api;

/**
 *
 * @author http://www.turnguard.com/turnguard
 */
public interface ViticultureDataParserRegistry {
    public ViticultureDataParser getViticultureDataParser(String identifier) throws UnknownViticultureDataParserException;
    public ViticultureDataParser getViticultureDataParserForFileName(String fileName) throws UnknownViticultureDataParserException;
}
