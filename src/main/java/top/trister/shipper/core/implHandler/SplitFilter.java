package top.trister.shipper.core.implHandler;

import lombok.Data;
import top.trister.shipper.core.api.Filter;

import java.util.*;

@Data
public class SplitFilter implements Filter {
    String field;
    String terminator;

    @SuppressWarnings("unchecked")
    @Override
    public List<Map> filter(Map event) {
        List<Map> addList = new ArrayList<>();
        Object v = event.get(field);
        if (v instanceof String) {
            String[] split = ((String) v).split(terminator);
            for (String s : split) {
                Map newMap = new HashMap<>(event);
                newMap.put(field, s);
                addList.add(newMap);
            }
        } else {
            addList.add(event);
        }
        return addList;
    }
}
