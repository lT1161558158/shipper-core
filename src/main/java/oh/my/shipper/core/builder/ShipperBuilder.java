package oh.my.shipper.core.builder;

import oh.my.shipper.core.bean.Shipper;

/**
 * shipper的构造器
 */
public interface ShipperBuilder {
    Shipper build(String shipper);
}
