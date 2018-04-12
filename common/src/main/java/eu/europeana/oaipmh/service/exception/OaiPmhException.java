package eu.europeana.oaipmh.service.exception;


import javax.xml.bind.annotation.XmlRootElement;

/**
 * Base error class for this application
 * @author Patrick Ehlert
 * Created on 27-02-2018
 */
@XmlRootElement
public class OaiPmhException extends Exception {

    private ErrorCode errorCode;

    public OaiPmhException(String msg, Throwable t) {
        super(msg, t);
    }

    public OaiPmhException(String msg, ErrorCode errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

    public OaiPmhException(String msg) {
        super(msg);
    }

    /**
     * @return boolean indicating whether this type of exception should be logged or not
     */
    public boolean doLog() {
        return true; // default we log all exceptions
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
