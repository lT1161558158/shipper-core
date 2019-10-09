package oh.my.shipper.core.implHandler.codec;

import lombok.Data;
import oh.my.shipper.core.api.InputCodec;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static oh.my.shipper.core.event.Event.MESSAGE;
import static oh.my.shipper.core.event.Event.TIMESTAMP;

@Data
public class SimpleCodec implements InputCodec<String> {
    private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private String format = DEFAULT_FORMAT;

    @Override
    public Map codec(String input) {
        Map<Object, Object> event = new HashMap<>();
        event.put(TIMESTAMP, builderFormat().format(new Date()));
        event.put(MESSAGE, input);
        return event;
    }
    private SimpleDateFormat builderFormat(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        try{
            simpleDateFormat.applyPattern(format);
        }catch (Exception ignore){
            simpleDateFormat.applyPattern(DEFAULT_FORMAT);
            format=DEFAULT_FORMAT;
        }
        return simpleDateFormat;
    }
}
