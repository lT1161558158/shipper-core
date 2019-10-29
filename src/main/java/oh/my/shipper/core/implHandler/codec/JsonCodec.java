package oh.my.shipper.core.implHandler.codec;

import com.google.gson.Gson;
import lombok.ToString;
import oh.my.shipper.core.api.OutCodec;

import java.util.Map;

/**
 * 将输入的 event 对象转化为json字符串的codec
 */
@ToString
public class JsonCodec implements OutCodec<String> {
    @Override
    public String codec(Map input) {
        return new Gson().toJson(input);
    }
}
