package top.trister.shipper.core.implHandler;

import lombok.Data;
import lombok.EqualsAndHashCode;
import top.trister.shipper.core.api.handler.output.CodecOutput;

@EqualsAndHashCode(callSuper = false)
@Data
public class StdOutput extends SimpleCodifiedHandler<Object, String> implements CodecOutput<Object, String> {

    @Override
    public void write(String event) {
        System.out.println(event);
    }
}
