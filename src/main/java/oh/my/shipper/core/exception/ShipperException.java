package oh.my.shipper.core.exception;

public class ShipperException extends RuntimeException {
    public ShipperException() {
    }

    public ShipperException(String message) {
        super(message);
    }

    public ShipperException(String message, Throwable cause) {
        super(message, cause);
    }

    public ShipperException(Throwable cause) {
        super(cause);
    }

    public ShipperException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
