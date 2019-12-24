package top.trister.shipper.core.exception;

import lombok.Getter;

import java.util.List;

public class MultipleException extends ShipperException {
    @Getter
    private List<Exception> exceptions;

    public MultipleException(List<Exception> exceptions) {
        this.exceptions = exceptions;
    }
}
