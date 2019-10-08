package oh.my.shipper.core.implHandler;

import oh.my.shipper.core.api.Output;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class StdOutput extends SimpleCodifiedHandler<Map,String> implements Output<String> {

    @Override
    public void write(Map event){
        System.out.println(codec.codec(event));
    }

    @Override
    public void write(Map event, TimeUnit unit, long timeout){
        write(event);
    }

    @Override
    public boolean writeAble() {
        return true;
    }

    @Override
    public void close() {
        //ignore
    }
}
