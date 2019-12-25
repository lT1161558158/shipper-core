package top.trister.shipper.core.implHandler.codec;

import com.google.gson.Gson;
import lombok.ToString;
import top.trister.shipper.core.api.handler.codec.Codec;

/**
 * 将输入的 event 对象转化为json字符串的codec
 */
@ToString
public class JsonCodec implements Codec<Object, String> {

    @Override
    public String codec(Object input) {
        return new Gson().toJson(input);
    }
}
