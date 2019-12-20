package top.trister.shipper.core.bean;

import lombok.Data;
import top.trister.shipper.core.dsl.DSLDelegate;
import top.trister.shipper.core.enums.HandlerEnums;

import java.util.Map;
@Data
public class Shipper {
    String shipperDescribe;
    Map<HandlerEnums, DSLDelegate> context;
}
