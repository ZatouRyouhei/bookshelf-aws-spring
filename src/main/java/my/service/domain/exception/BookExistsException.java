package my.service.domain.exception;

public class BookExistsException extends RuntimeException {
    
    public BookExistsException (String message) {
        super(message);
    }
}
