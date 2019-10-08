package oh.my.shipper.core.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PropertyUtil {

    public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName) throws IntrospectionException {//使用 PropertyDescriptor 提供的 get和set方法
         return new PropertyDescriptor(propertyName, clazz);
    }

    public static void setProperty(Object obj, String propertyName, Object value) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Class<?> clazz = obj.getClass();// 获取对象的类型
        PropertyDescriptor pd = getPropertyDescriptor(clazz, propertyName);// 获取
        Method setMethod = pd.getWriteMethod();// 从属性描述器中获取 set 方法
        setMethod.invoke(obj, value);// 调用 set
    }

    public static Object getProperty(Object obj, String propertyName) throws InvocationTargetException, IllegalAccessException, IntrospectionException {
        Class<?> clazz = obj.getClass();// 获取对象的类型
        PropertyDescriptor pd = getPropertyDescriptor(clazz, propertyName);// 获取
        Method getMethod = pd.getReadMethod();// 从属性描述器中获取 get 方法
        return getMethod.invoke(clazz);// 返回值
    }


}