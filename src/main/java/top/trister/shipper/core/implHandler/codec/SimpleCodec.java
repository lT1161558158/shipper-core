package top.trister.shipper.core.implHandler.codec;

import lombok.Data;
import top.trister.shipper.core.api.handler.codec.Codec;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static top.trister.shipper.core.event.Event.MESSAGE;
import static top.trister.shipper.core.event.Event.TIMESTAMP;

/**
 * 添加一个时间filed发编码器
 * 使用 format 变量进行时间格式化
 * 若使用了错误的格式化则会使用默认的format yyyy-MM-dd HH:mm:ss
 */
@Data
public class SimpleCodec implements Codec<Object, Map> {
    private static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private String format = DEFAULT_FORMAT;

    private SimpleDateFormat builderFormat() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        try {
            simpleDateFormat.applyPattern(format);
        } catch (Exception ignore) {
            simpleDateFormat.applyPattern(DEFAULT_FORMAT);
            format = DEFAULT_FORMAT;
        }
        return simpleDateFormat;
    }

    @Override
    public Map codec(Object input) {
        Map<Object, Object> event = new HashMap<>();
        event.put(TIMESTAMP, builderFormat().format(new Date()));
        event.put(MESSAGE, input);
        return event;
    }
}
