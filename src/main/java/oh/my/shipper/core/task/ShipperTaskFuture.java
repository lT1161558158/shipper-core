package oh.my.shipper.core.task;

import lombok.Getter;
import lombok.experimental.Delegate;

import java.util.concurrent.Future;

public class ShipperTaskFuture<T> implements Future<T> {
    @Getter
    private final ShipperTask task;
    @Delegate
    private final Future<T> future;

    public ShipperTaskFuture(ShipperTask task, Future<T> future) {
        this.task = task;
        this.future = future;
    }
}
