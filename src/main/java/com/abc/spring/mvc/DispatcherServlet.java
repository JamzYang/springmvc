package com.abc.spring.mvc;

import com.abc.spring.annotation.Autowired;
import com.abc.spring.annotation.Controller;
import com.abc.spring.annotation.RequestMapping;
import com.abc.spring.annotation.Service;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class DispatcherServlet extends HttpServlet{
    //扫描包下的所有类的权限定类名
    private List<String> packageNames = new ArrayList<>();
    private Map<String, String> nameMap = new HashMap<>();
    private Map<String, Object> beansMap = new HashMap<>();
    //url和方法的对应关系
    private Map<String, Method> urlMethodMap = new HashMap<>();
    private Map<Method, String> methodPackageMap = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        String basePackage = config.getInitParameter("basePackage");
        scanBasePackage(basePackage);
        loadBeans();
        autowireBeans();
        handlerUrlMethodMap();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       /* String contextPath = req.getContextPath();
        String requestURL = req.getRequestURL().toString();
        String s1 = requestURL.replace(contextPath, "");
        String[] pathAndParams = null;
        String[] params = null;
        if(s1.contains("?")){
            pathAndParams = s1.split("\\?");
            params = pathAndParams[1].split("=");
        }*/
        String uri = req.getRequestURI();
        Enumeration<String> parameterNames = req.getParameterNames();
        Method method = this.urlMethodMap.get(uri);
        Object bean = this.beansMap.get(method.getDeclaringClass().getName());
        try {
            method.invoke(bean,"ys");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void handlerUrlMethodMap() {
        for(Map.Entry entry : beansMap.entrySet()){
            String beanName = (String) entry.getKey();
            Object bean = entry.getValue();
            if(bean.getClass().isAnnotationPresent(Controller.class)){
                String pathType = bean.getClass().getAnnotation(RequestMapping.class).value();
                Method[] methods = bean.getClass().getDeclaredMethods();
                for (Method method : methods) {
                    if(method.isAnnotationPresent(RequestMapping.class)){
                        String pathMethod = method.getAnnotation(RequestMapping.class).value();
                        this.urlMethodMap.put(pathType+pathMethod, method);
                    }
                }
            }
        }
    }

    private void autowireBeans() {
        for(Map.Entry entry:beansMap.entrySet()){
            String beanClassName = (String)entry.getKey();
            Object bean = entry.getValue();
            Field[] fields = bean.getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                if(fields[i].isAnnotationPresent(Autowired.class)){
                   fields[i].setAccessible(true);
                    try {
                        Object o = beansMap.get(fields[i].getType().getName());
                        fields[i].set(bean,beansMap.get(fields[i].getType().getName()));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void loadBeans()  {
        for(String packageName : this.packageNames){
            try {
                String beanClassName = packageName.replace(".class", "");
                Class<?> clazz = Class.forName(beanClassName);
                if(clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class)){
                    this.beansMap.put(beanClassName, clazz.newInstance());
                }
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void scanBasePackage(String basePackage) {
        String basePath = basePackage.replaceAll("\\.","/");
        URL baseUrl = this.getClass().getClassLoader().getResource(basePath);
        File baseDir = new File(baseUrl.getPath());
        for(File file : baseDir.listFiles()){
            if(file.isDirectory()){
                String subDirPath = basePackage+"."+file.getName();
                scanBasePackage(subDirPath);
            }else{
                this.packageNames.add(basePackage+"."+file.getName());
            }
        }
    }

}
