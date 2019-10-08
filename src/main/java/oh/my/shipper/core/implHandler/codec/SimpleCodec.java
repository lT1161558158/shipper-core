package oh.my.shipper.core.implHandler.codec;

import oh.my.shipper.core.api.InputCodec;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static oh.my.shipper.core.event.Event.MESSAGE;
import static oh.my.shipper.core.event.Event.TIMESTAMP;


public class SimpleCodec implements InputCodec<String> {
    @Override
    public Map codec(String input) {
        Map<Object, Object> event = new HashMap<>();
        event.put(TIMESTAMP,new Date());
        event.put(MESSAGE,input);
        return event;
    }
}
