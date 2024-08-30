package nl.utwente.di.interactief2.exceptions;

public class UnSanitizedInputException extends RuntimeException {

    public UnSanitizedInputException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }

    public UnSanitizedInputException() {
        super("", new Throwable());
    }
}
