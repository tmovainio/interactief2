package nl.utwente.di.interactief2.exceptions;

public class BadRequestException extends RuntimeException{

    public BadRequestException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
