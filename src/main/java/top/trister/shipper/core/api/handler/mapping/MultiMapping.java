package top.trister.shipper.core.api.handler.mapping;

import java.util.Collection;
import java.util.stream.Collectors;

public interface MultiMapping<T, R> extends Mapping<T, R> {
    default Collection<R> multiMapping(Collection<T> list) {
        return list.stream().map(this::mapping).collect(Collectors.toList());
    }
}
