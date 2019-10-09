package oh.my.shipper.core.builder;

import lombok.extern.slf4j.Slf4j;
import oh.my.shipper.core.api.Handler;
import oh.my.shipper.core.util.ClassUtils;
import oh.my.shipper.core.util.PropertyUtil;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
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
     * @throws IOException
     */
    public void reLoadHandler() throws IOException {
        Enumeration<URL> resources = HandlerBuilder.class.getClassLoader().getResources("META-INF/collector.factories");
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            UrlResource resource = new UrlResource(url);
            Properties properties = PropertiesLoaderUtils.loadProperties(resource);
            String packageScan = properties.getProperty("handlerPackage");
            List<Class<?>> allClass = ClassUtils.getAllClass(packageScan);
            if (allClass==null){
                log.error("not find any class of {}",packageScan);
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Class<Handler>> collect = allClass.stream()
                    .filter(c -> !ClassUtils.isChild(c, Handler.class))
                    .map(o->(Class<Handler>)o)
                    .collect(
                            Collectors.toMap(
                                    org.springframework.util.ClassUtils::getShortName,
                                    Function.identity()
                            ));
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
     * @param name
     * @param args
     * @return
     */
    public Handler builderHandler(String name,Map<String,Object> args){
        Class<Handler> handleByName = findHandleByName(name);
        try{
            Handler handler = handleByName.newInstance();
            if (args==null)
                return handler;
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