package oh.my.shipper.core.implHandler;

import oh.my.shipper.core.api.Output;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class StdOutput extends SimpleCodifiedHandler<Map,String> implements Output<String> {

    @Override
    public void write(Map event, TimeUnit unit, long timeout){
        System.out.println(codec.codec(event));
    }

}
