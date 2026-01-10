package com.mic.hilt.javassist;

import java.lang.reflect.Method;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;

public class JavassistUtils {

    public static void test() {
        try {
            // 修复关键：在获取类之前先预加载
            Class.forName("com.mic.hilt.javassist.Student");
            Class.forName("com.mic.hilt.javassist.Person");

            ClassPool pool = ClassPool.getDefault();
            pool.insertClassPath(new javassist.LoaderClassPath(Thread.currentThread().getContextClassLoader()));

            CtClass ctClass = pool.get("com.mic.hilt.javassist.Student");
            CtClass pClass = pool.get("com.mic.hilt.javassist.Person");

            ctClass.setSuperclass(pClass);
            ctClass.addField(CtField.make("private String name;", ctClass));
            ctClass.addMethod(CtMethod.make("public void setName(String name){this.name=name;}", ctClass));
            ctClass.addMethod(CtMethod.make("public String getName(){return this.name;}", ctClass));

            Class studentClass = ctClass.toClass();

            Method setNameMethod = studentClass.getMethod("setName", String.class);
            Method getNameMethod = studentClass.getMethod("getName");
            Method getPersonNameMethod = studentClass.getMethod("getPersonName");

            Student student = (Student) studentClass.newInstance();
            setNameMethod.invoke(student, "自定义的名称");

            String name = (String) getNameMethod.invoke(student);
            System.out.println("name=" + name);

            String personName = (String) getPersonNameMethod.invoke(student);
            System.out.println("personName=" + personName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}