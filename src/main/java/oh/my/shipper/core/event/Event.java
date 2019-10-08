package oh.my.shipper.core.event;

import java.util.List;
import java.util.Map;

public class Event {
    public static final String VERSION = "@version";
    public static final String TIMESTAMP = "@timestamp";
    public static final String TYPE = "@type";
    public static final String TAGS = "@tags";
    public static final String ATTRIBUTE = "@attr";
    public static final String MESSAGE = "message";//默认的值名字

    public Event() {

    }
    List<Map> source;

}
