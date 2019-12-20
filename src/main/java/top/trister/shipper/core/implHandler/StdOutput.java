package top.trister.shipper.core.implHandler;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.trister.shipper.core.api.Output;

import java.util.Map;
import java.util.concurrent.TimeUnit;
@EqualsAndHashCode(callSuper = false)
@Data
public class StdOutput extends SimpleCodifiedHandler<Map,String> implements Output<String> {

    @Override
    public void write(Map event, TimeUnit unit, long timeout){
        System.out.println(codec.codec(event));
    }

}
