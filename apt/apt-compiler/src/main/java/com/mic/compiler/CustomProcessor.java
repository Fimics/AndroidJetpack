package com.mic.compiler;

import com.google.auto.service.AutoService;
import com.mic.annotation.Inject;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.mic.annotation.Inject")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class CustomProcessor extends AbstractProcessor {

    private Messager messager;// 日志
    private Types typeUtils;
    /**
     * 初始化
     * @param processingEnvironment 处理工具集合
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnvironment.getMessager();
        typeUtils = processingEnvironment.getTypeUtils();
        messager.printMessage(Diagnostic.Kind.NOTE, "CustomProcessor init");
    }

    /**
     * 处理注解,扫描到指定的注解被使用，就进入该方法
     * @param set 指定注解的集合
     * @param roundEnvironment 提供当前的注解的信息获取
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        messager.printMessage(Diagnostic.Kind.NOTE, "CustomProcessor process");
        //哪些地方用到了Inject注解
        Set<? extends Element> elements =roundEnvironment.getElementsAnnotatedWith(Inject.class);
        for (Element element : elements) {
            messager.printMessage(Diagnostic.Kind.NOTE, "CustomProcessor process element:" + element.getSimpleName());
            VariableElement variableElement = (VariableElement) element;
            TypeMirror typeMirror = variableElement.asType();
            //variableTypeElement 就是User
            TypeElement variableTypeElement = (TypeElement) typeUtils.asElement(typeMirror);
            messager.printMessage(Diagnostic.Kind.NOTE, "CustomProcessor process variableTypeElement:" + variableTypeElement.getQualifiedName());
            //拿到父节点 MainActivity
            TypeElement parentElement = (TypeElement)variableElement.getEnclosingElement();
            messager.printMessage(Diagnostic.Kind.NOTE, "CustomProcessor process parentElement:" + parentElement.getQualifiedName());

            //拿到父节点的所有子节点 MainActivity的所以子节点
            for (Element subElement : parentElement.getEnclosedElements()) {
                messager.printMessage(Diagnostic.Kind.NOTE, "CustomProcessor process subElement:" + subElement.getSimpleName());
            }

            //获取注解的值
            String value =variableElement.getAnnotation(Inject.class).value();
            messager.printMessage(Diagnostic.Kind.NOTE, "CustomProcessor process value:" + value);
        }
        return false;
    }
}
