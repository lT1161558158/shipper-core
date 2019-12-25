package top.trister.shipper.core.implHandler;

import lombok.Data;
import top.trister.shipper.core.api.handler.mapping.Mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;

@Data
public class SplitMapping implements Mapping<Map,List<Map>> {
    String field;
    String terminator;


    @SuppressWarnings("unchecked")
    @Override
    public List<Map> mapping(Map event) {
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

    public static void main(String[] args) {
        System.out.println(BinaryOperator.class.asSubclass(Function.class));
    }

}
