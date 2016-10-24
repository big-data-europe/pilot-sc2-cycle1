package eu.bde.sc6.viticulture.parser.api;

/**
 *
 * @author http://www.turnguard.com/turnguard
 */
public class TransformationException extends Exception {

    public TransformationException() {
    }

    public TransformationException(String message) {
        super(message);
    }

    public TransformationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransformationException(Throwable cause) {
        super(cause);
    }

    public TransformationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
