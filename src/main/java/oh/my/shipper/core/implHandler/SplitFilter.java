package oh.my.shipper.core.implHandler;

import lombok.Data;
import oh.my.shipper.core.api.Filter;

import java.util.*;

@Data
public class SplitFilter implements Filter {
    String field;
    String terminator;
    @SuppressWarnings("unchecked")
    @Override
    public void filter(List<Map> event) {
        List<Map> addList=new ArrayList<>();
        Iterator<Map> iterator = event.iterator();
        while (iterator.hasNext()) {
            Map next = iterator.next();
            Object v = next.get(field);
            if (v instanceof String){
                String[] split = ((String) v).split(terminator);
                for (String s : split) {
                    Map newMap=new HashMap<>(next);
                    newMap.put(field,s);
                    addList.add(newMap);
                }
                iterator.remove();
            }
        }
        event.addAll(addList);
    }
}
