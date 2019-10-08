package oh.my.shipper.core.implHandler;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import oh.my.shipper.core.api.Input;
import oh.my.shipper.core.exception.ShipperException;

import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * stdio的input组件
 */
@ToString(exclude = "scanner" )
public class StdInput extends SimpleCodifiedHandler<String,Map> implements Input<String> {
    private Scanner scanner = new Scanner(System.in);
    /**
     * 元素分隔符
     */
    @Setter
    @Getter
    private String delimiter;

    @Override
    public Map read(TimeUnit unit, long timeout) {
        long millis = timeout < 0 ? -1 : unit.toMillis(timeout);
        long counter = 0;
        while (!scanner.hasNext() && (millis < 0 || counter < millis)) {
            counter++;
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new ShipperException(e);
            }
        }
        if (millis > 0 && counter >= millis)
            throw new ShipperException("timeout " + unit + timeout);
        if (Objects.nonNull(delimiter))
            scanner.useDelimiter(delimiter);//当delimiter不为空时,使用注入的delimiter
        return codec.codec(scanner.next());
    }

    @Override
    public boolean ready() {
        return true;
    }

    @Override
    public void close() {
        scanner.close();
    }

}
