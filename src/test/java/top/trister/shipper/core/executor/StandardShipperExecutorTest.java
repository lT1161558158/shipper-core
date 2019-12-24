//package top.trister.shipper.core.executor;
//
//import org.junit.Test;
////@Slf4j
//public class StandardShipperExecutorTest {
//    @Test
//    public void test() throws Exception {
////        Stream<String> lines = new BufferedReader(new FileReader("C:\\work\\code\\java\\shipper\\src\\main\\resources\\test.shipper")).lines();
////        String dsl = lines.collect(Collectors.joining("\n"));
////        StandardHandlerBuilder handlerBuilder = new StandardHandlerBuilder();
////        handlerBuilder.reLoadHandler();
////        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), r -> {
////            Thread thread = new Thread(r);
//////            thread.setUncaughtExceptionHandler((t, e) -> log.error("shipper executor error", e));
////            return thread;
////        });
////        ShipperTaskBuilder ShipperTaskBuilder = new StandardShipperTaskBuilder();
////        try (ShipperExecutor standardShipperExecutor = new StandardShipperExecutor(handlerBuilder, ShipperTaskBuilder, threadPoolExecutor)) {
////            standardShipperExecutor.execute(dsl);
////        }
//
//    }
//}