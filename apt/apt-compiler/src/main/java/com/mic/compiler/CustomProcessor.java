package com.mic.compiler;

import com.google.auto.service.AutoService;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes("com.mic.annotation.Inject")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class CustomProcessor extends AbstractProcessor {

    private Messager messager;// 日志
    /**
     * 初始化
     * @param processingEnvironment 处理工具集合
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnvironment.getMessager();
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
        return false;
    }
}
