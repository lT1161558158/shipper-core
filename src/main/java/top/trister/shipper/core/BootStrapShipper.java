package top.trister.shipper.core;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import top.trister.shipper.core.builder.*;
import top.trister.shipper.core.executor.ShipperExecutor;
import top.trister.shipper.core.executor.StandardShipperExecutor;
import top.trister.shipper.core.task.ShipperTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * 简单启动类
 */
@Builder
@Data
@Slf4j
public final class BootStrapShipper {
    ShipperExecutor shipperExecutor;
    HandlerBuilder handlerBuilder;
    ShipperBuilder shipperBuilder;
    ShipperTaskBuilder shipperTaskBuilder;

    /**
     * 成员状态检查
     */
    private void stateCheck() {

        if (shipperBuilder == null) {
            if (handlerBuilder == null)
                handlerBuilder = new StandardHandlerBuilder();
            if (!handlerBuilder.initialized())
                handlerBuilder.reLoadHandler();
            shipperBuilder = new StandardShipperBuilder(handlerBuilder);
        }

        if (shipperTaskBuilder == null)
            shipperTaskBuilder = new StandardShipperTaskBuilder();
        if (shipperExecutor == null)
            shipperExecutor = new StandardShipperExecutor(ForkJoinPool.commonPool());
    }

    /**
     * @param dsl dsl
     * @return dsl中包含的多个任务
     */
    public List<CompletableFuture<ShipperTask>> submit(String dsl) {
        stateCheck();
        return shipperTaskBuilder
                .build(shipperBuilder.build(dsl))
                .stream()
                .map(shipperExecutor::submit)
                .collect(Collectors.toList());
    }


    /**
     * 如果 shipper 文件能读到,则执行这个 shipper
     *
     * @param sourceFile resources下的文件
     */
    public void executeBySource(String sourceFile) {
        Optional.ofNullable(Thread.currentThread().getContextClassLoader().getResource(sourceFile)).ifPresent(url -> {
            try {
                String dsl = new BufferedReader(new InputStreamReader(url.openStream()))
                        .lines()
                        .collect(Collectors.joining("\n"));
                List<CompletableFuture<ShipperTask>> submit = submit(dsl);
                CompletableFuture<Void> voidCompletableFuture = CompletableFuture.allOf(submit.toArray(new CompletableFuture[0]));
                voidCompletableFuture.whenComplete((v, e) -> voidCompletableFuture.completeExceptionally(e));
                voidCompletableFuture.join();
            } catch (IOException e) {
                log.error("", e);
            }
        });
    }
}
