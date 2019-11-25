package oh.my.shipper.core.bean;

import lombok.Data;
import oh.my.shipper.core.dsl.DSLDelegate;
import oh.my.shipper.core.enums.HandlerEnums;

import java.util.Map;
@Data
public class Shipper {
    String shipperDescribe;
    Map<HandlerEnums, DSLDelegate> context;
}
