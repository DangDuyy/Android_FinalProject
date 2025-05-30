package fit24.duy.musicplayer.exception;

public class StorageException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Exception e) {
        super(message, e);
    }
}
