package oh.my.shipper.core.builder;

import lombok.extern.slf4j.Slf4j;
import oh.my.shipper.core.api.Handler;
import oh.my.shipper.core.util.ClassUtils;
import oh.my.shipper.core.util.PropertyUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public final class HandlerBuilder {
    private final Map<String,Class<Handler>> SIMPLE_NAME_CACHE=new ConcurrentHashMap<>();

    /**
     * 加载Handler
     * @throws IOException 加载时可能的异常
     */
    public void reLoadHandler() throws IOException {
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("META-INF/shipper.factories");
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            Properties properties = new Properties();
            try(InputStream inputStream=url.openStream()){
                properties.load(inputStream);
            }
            String packageScan = properties.getProperty("handlerPackage");
            List<Class<?>> allClass = ClassUtils.getAllClass(packageScan);
            if (allClass==null){
                log.error("not find any class of {}",packageScan);
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Class<Handler>> collect = allClass.stream()
                    .filter(Handler.class::isAssignableFrom)
                    .map(o->(Class<Handler>)o)
                    .collect(Collectors.toMap(Class::getSimpleName, Function.identity()));
            SIMPLE_NAME_CACHE.putAll(collect);
        }
    }

    /**
     *
     * @param name 类的短名
     * @return 对应的类
     */
    public Class<Handler> findHandleByName(String name){
        return SIMPLE_NAME_CACHE.get(name);
    }

    /**
     * args中可以找到属性的args将会被移除,并且设置进生成的handler对象中
     * 也就是说将会剩下一些 Handler 中未使用的属性
     * @param name 处理器名字
     * @param args 给处理器设置的参数
     * @return 处理器的一个实例
     */
    public Handler builderHandler(String name,Map<String,Object> args){
        Handler handler = builderHandler(name);
        try{
            Iterator<Map.Entry<String, Object>> iterator = args.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> next = iterator.next();
                try {
                    PropertyUtil.setProperty(handler,next.getKey(),next.getValue());
                    iterator.remove();
                } catch (Exception ignore) { }
            }
            return handler;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param name 处理器名字
     * @return 未设置参数的 实例
     */
    public Handler builderHandler(String name){
        Class<Handler> handleByName = findHandleByName(name);
        if (handleByName==null)
            throw new RuntimeException("not find Handler "+name);
        try {
            return handleByName.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}