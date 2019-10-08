package oh.my.shipper.core.util;

import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassUtils {



    public static List<Class<?>> getAllClass(String packageName) {
        List<String> classNameList =  getClassName(packageName);
        if (classNameList==null)
            return null;
        List<Class<?>> list = new ArrayList<>();
        for(String className : classNameList){
            try {
                list.add(Class.forName(className));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("load class from name failed:"+className+e.getMessage());
            }
        }
        return list;
    }

    /**
     *  通过包名获取所有的类
     * @param packageName 包名
     * @return 包下的所有类
     */
    public static List<String> getClassName(String packageName) {

        List<String> fileNames = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");
        URL url = loader.getResource(packagePath);
        if (url==null)
            return null;
        String type = url.getProtocol();
        if (type.equals("file")) {
            String fileSearchPath = url.getPath();
//                fileSearchPath = fileSearchPath.substring(0,fileSearchPath.indexOf("/classes"));
            fileNames = getClassNameByFile(fileSearchPath);
        } else if (type.equals("jar")) {
            try{
                JarURLConnection jarURLConnection = (JarURLConnection)url.openConnection();
                JarFile jarFile = jarURLConnection.getJarFile();
                fileNames = getClassNameByJar(jarFile);
            }catch (java.io.IOException e){
                throw new RuntimeException("open Package URL failed："+e.getMessage());
            }
        }else{
            throw new RuntimeException("file system not support! cannot load MsgProcessor！type "+type);
        }
        return fileNames;
    }
    /**
     * 从文件获取类名
     * @return 类的完整名称
     */
    private static List<String> getClassNameByFile(String filePath) {
        List<String> myClassName = new ArrayList<>();
        File file = new File(filePath);
        File[] childFiles = file.listFiles();
        if (childFiles==null)
            return myClassName;
        for (File childFile : childFiles) {
            if (childFile.isDirectory()) {
                myClassName.addAll(getClassNameByFile(childFile.getPath()));
            } else {
                String childFilePath = childFile.getPath();
                if (childFilePath.endsWith(".class")) {
                    childFilePath = childFilePath.substring(childFilePath.indexOf("\\classes") + 9, childFilePath.lastIndexOf("."));
                    childFilePath = childFilePath.replace("\\", ".");
                    myClassName.add(childFilePath);
                }
            }
        }

        return myClassName;
    }

    /**
     * 从jar获取某包下所有类
     * @return 类的完整名称
     */
    private static List<String> getClassNameByJar(JarFile jarFile) {
        List<String> myClassName = new ArrayList<>();
        try {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String entryName = jarEntry.getName();
                if (entryName.endsWith(".class")) {
                    entryName = entryName.replace("/", ".").substring(0, entryName.lastIndexOf("."));
                    myClassName.add(entryName);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("发生异常:"+e.getMessage());
        }
        return myClassName;
    }

    public static boolean isChild(Class<?> clazz,Class<?> type){
        Class<?>[] allInterfacesForClass = org.springframework.util.ClassUtils.getAllInterfacesForClass(clazz);
        for (Class<?> interfacesForClass : allInterfacesForClass) {
            if (type.equals(interfacesForClass)){
                return true;
            }
        }
        return false;
    }
}
