package top.trister.shipper.core.builder;

import lombok.extern.slf4j.Slf4j;
import top.trister.shipper.core.api.Handler;
import top.trister.shipper.core.exception.ShipperException;
import top.trister.shipper.core.util.ClassUtils;
import top.trister.shipper.core.util.PropertyUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public final class StandardHandlerBuilder implements HandlerBuilder {

    private volatile Map<String, Class<Handler>> simpleNameCache = new ConcurrentHashMap<>();

    /**
     * 加载Handler
     *
     * @throws ShipperException 加载时可能的异常
     */
    @Override
    public void reLoadHandler() throws ShipperException{
        Enumeration<URL> resources;
        try {
            resources = Thread.currentThread().getContextClassLoader().getResources("META-INF/shipper.factories");
        } catch (IOException e) {
            throw new ShipperException(e);
        }
        Map<String, Class<Handler>> temp=new HashMap<>();
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            Properties properties = new Properties();
            try (InputStream inputStream = url.openStream()) {
                properties.load(inputStream);
            } catch (IOException e) {
                throw new ShipperException("open " + url + " failed", e);
            }
            String packageScan = properties.getProperty("handlerPackage");
            List<Class<?>> allClass = ClassUtils.getAllClass(packageScan);
            if (allClass == null) {
                log.warn("not find any class of {}", packageScan);
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Class<Handler>> collect = allClass.stream()
                    .filter(Handler.class::isAssignableFrom)
                    .map(o -> (Class<Handler>) o)
                    .collect(Collectors.toMap(Class::getSimpleName, Function.identity()));
            temp.putAll(collect);
        }
        synchronized (this) {
            simpleNameCache = new ConcurrentHashMap<>(temp);
        }
    }

    /**
     * @param name 类的短名
     * @return 对应的类
     */
    public Class<Handler> findHandleByName(String name) {
        return simpleNameCache.get(name);
    }

    /**
     * args中可以找到属性的args将会被移除,并且设置进生成的handler对象中
     * 也就是说将会剩下一些 Handler 中未使用的属性
     *
     * @param name 处理器名字
     * @param args 给处理器设置的参数
     * @return 处理器的一个实例
     */
    public Handler builderHandler(String name, Map<String, Object> args) {
        Handler handler = builderHandler(name);
        try {
            Iterator<Map.Entry<String, Object>> iterator = args.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> next = iterator.next();
                try {
                    PropertyUtil.setProperty(handler, next.getKey(), next.getValue());
                    iterator.remove();
                } catch (Exception ignore) {
                }
            }
            return handler;
        } catch (Exception e) {
            throw new ShipperException(e);
        }
    }

    /**
     * @param name 处理器名字
     * @return 未设置参数的 实例
     */
    @Override
    public Handler builderHandler(String name) {
        Class<Handler> handleByName = findHandleByName(name);
        if (handleByName == null)
            throw new ShipperException("not find Handler " + name);
        try {
            return handleByName.newInstance();
        } catch (Exception e) {
            throw new ShipperException(e);
        }
    }

}