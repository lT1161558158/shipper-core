package top.trister.shipper.core;

import lombok.Builder;
import lombok.Data;
import top.trister.shipper.core.builder.*;
import top.trister.shipper.core.executor.ShipperExecutor;
import top.trister.shipper.core.executor.StandardShipperExecutor;
import top.trister.shipper.core.task.ShipperTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * 简单启动类
 */
@Builder
@Data
public final class BootStrapShipper {
    ShipperExecutor shipperExecutor;
    HandlerBuilder handlerBuilder;
    ShipperBuilder shipperBuilder;
    ShipperTaskBuilder shipperTaskBuilder;

    /**
     * 成员状态检查
     */
    private void stateCheck(){
        if (handlerBuilder == null)
            handlerBuilder = new StandardHandlerBuilder();
        if (!handlerBuilder.initialized())
            handlerBuilder.reLoadHandler();
        if (shipperBuilder == null)
            shipperBuilder = new StandardShipperBuilder(handlerBuilder);
        if (shipperTaskBuilder == null)
            shipperTaskBuilder = new StandardShipperTaskBuilder();
        if (shipperExecutor == null)
            shipperExecutor = new StandardShipperExecutor(ForkJoinPool.commonPool());
    }
    public List<CompletableFuture<ShipperTask>> submit(String dsl) {
        stateCheck();
        return shipperTaskBuilder
                .build(shipperBuilder.build(dsl))
                .stream()
                .map(t->CompletableFuture.supplyAsync(t::doing, shipperExecutor))
                .collect(Collectors.toList());
    }
}
