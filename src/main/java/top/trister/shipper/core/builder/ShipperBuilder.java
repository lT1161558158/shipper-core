package top.trister.shipper.core.builder;

import top.trister.shipper.core.bean.Shipper;

/**
 * shipper的构造器
 */
public interface ShipperBuilder {
    Shipper build(String shipper);
}
