package com.mic.compiler;

import com.google.auto.service.AutoService;
import com.mic.annotation.Component;
import com.mic.annotation.Inject;
import com.mic.annotation.Provider;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;

/**
 * 自定义注解处理器，用于处理 @Component 和 @Inject 注解
 * 生成依赖注入相关的代码，包括 Factory、MembersInjector 和 DaggerComponent
 *
 * 功能说明：
 * 1. 处理 @Component 注解的接口，生成对应的 DaggerComponent 实现类
 * 2. 处理 @Inject 注解的字段，生成对应的 Factory 和 MembersInjector 类
 * 3. 支持泛型类型的依赖注入
 * 4. 自动生成 Builder 模式用于组件构建
 */
@AutoService(Processor.class) // 使用 Google AutoService 自动注册注解处理器
@SupportedAnnotationTypes({"com.mic.annotation.Inject", "com.mic.annotation.Component"}) // 指定处理的注解类型
@SupportedSourceVersion(SourceVersion.RELEASE_21) // 指定支持的 Java 版本
public class CustomProcessor extends AbstractProcessor {

    // 处理器工具类实例
    private Messager messager;    // 用于输出日志和错误信息
    private Types typeUtils;      // 用于类型操作的工具类
    private Filer filer;          // 用于创建新文件
    private Elements elementUtils; // 用于元素操作的工具类

    // 用于记录已生成的文件，避免重复生成
    private Set<String> generatedFiles;

    /**
     * 初始化注解处理器
     * 在处理器开始工作前被调用，用于获取必要的工具类实例
     *
     * @param processingEnv 处理环境，提供各种工具类
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        // 获取工具类实例
        messager = processingEnv.getMessager();
        typeUtils = processingEnv.getTypeUtils();
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        generatedFiles = new HashSet<>();
        // 输出初始化日志
        messager.printMessage(Diagnostic.Kind.NOTE, "CustomProcessor init");
    }

    /**
     * 主要的处理方法，处理所有被支持的注解
     *
     * @param annotations 当前轮次中要处理的注解类型
     * @param roundEnv    当前轮次的环境信息
     * @return 如果这些注解被此处理器声明，返回 true；否则返回 false
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(Diagnostic.Kind.NOTE, "CustomProcessor process start");

        try {
            // 处理被@Inject注解的字段，生成Factory（必须先处理，因为Component依赖Factory）
            processInjectFields(roundEnv);
            // 处理被@Component注解的类，生成DaggerComponent
            processComponents(roundEnv);
        } catch (Exception e) {
            // 处理过程中的异常捕获和日志输出
            messager.printMessage(Diagnostic.Kind.ERROR, "Processing error: " + e.getMessage());
            e.printStackTrace();
        }

        return true; // 返回true表示注解已被处理
    }

    /**
     * 处理所有被 @Component 注解标记的类或接口
     * 为每个 Component 生成对应的 DaggerComponent 实现类
     *
     * @param roundEnv 当前轮次的环境信息
     */
    private void processComponents(RoundEnvironment roundEnv) {
        // 获取所有被 @Component 注解的元素
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Component.class);
        for (Element element : elements) {
            // 确保元素是类或接口类型
            if (element instanceof TypeElement) {
                TypeElement componentElement = (TypeElement) element;
                // 为每个 Component 生成对应的类
                createComponentClasses(componentElement);
            }
        }
    }

    /**
     * 处理所有被 @Inject 注解标记的字段
     * 为每个注入类型生成 Factory，为每个包含注入字段的类生成 MembersInjector
     *
     * @param roundEnv 当前轮次的环境信息
     */
    private void processInjectFields(RoundEnvironment roundEnv) {
        // 获取所有被 @Inject 注解的元素
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Inject.class);

        // 收集所有需要生成 Factory 的类型（去重）
        Set<TypeMirror> typesToCreateFactory = new HashSet<>();

        for (Element element : elements) {
            if (element instanceof VariableElement) {
                VariableElement variableElement = (VariableElement) element;
                TypeMirror fieldType = variableElement.asType();
                typesToCreateFactory.add(fieldType);
            }
        }

        // 为每个需要注入的类型创建 Factory
        for (TypeMirror type : typesToCreateFactory) {
            createFactoryForType(type);
        }

        // 按包含类对注入字段进行分组，用于创建 MembersInjector
        Map<TypeElement, List<VariableElement>> injectFieldsByClass = new HashMap<>();
        for (Element element : elements) {
            if (element instanceof VariableElement) {
                VariableElement variableElement = (VariableElement) element;
                Element enclosingElement = variableElement.getEnclosingElement();

                // 确保字段属于一个类
                if (enclosingElement instanceof TypeElement) {
                    TypeElement enclosingClass = (TypeElement) enclosingElement;
                    // 使用 computeIfAbsent 确保每个类对应的列表存在
                    injectFieldsByClass
                            .computeIfAbsent(enclosingClass, k -> new ArrayList<>())
                            .add(variableElement);
                }
            }
        }

        // 为每个包含注入字段的类创建 MembersInjector
        for (Map.Entry<TypeElement, List<VariableElement>> entry : injectFieldsByClass.entrySet()) {
            createMembersInjector(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 为 Component 接口创建对应的实现类
     *
     * @param componentElement 被 @Component 注解的接口元素
     */
    private void createComponentClasses(TypeElement componentElement) {
        // 生成类名：Dagger + 原接口名
        String componentName = componentElement.getSimpleName().toString();
        String daggerComponentName = "Dagger" + componentName;

        // 分离接口中的方法：provider方法（有返回值）和inject方法（void）
        List<ExecutableElement> providerMethods = new ArrayList<>();
        List<ExecutableElement> injectMethods = new ArrayList<>();

        // 遍历接口中的所有元素
        for (Element enclosed : componentElement.getEnclosedElements()) {
            if (enclosed instanceof ExecutableElement) {
                ExecutableElement method = (ExecutableElement) enclosed;
                // 只处理抽象方法（接口方法）
                if (method.getModifiers().contains(Modifier.ABSTRACT)) {
                    if (method.getReturnType().getKind() == TypeKind.VOID) {
                        // void方法通常是inject方法，用于字段注入
                        injectMethods.add(method);
                    } else {
                        // 有返回值的方法是provider方法，用于提供实例
                        providerMethods.add(method);
                    }
                }
            }
        }

        // 创建 DaggerComponent 实现类
        createDaggerComponent(componentElement, daggerComponentName, providerMethods, injectMethods);
    }

    /**
     * 创建 DaggerComponent 的具体实现类
     *
     * @param componentInterface Component 接口元素
     * @param className          生成的类名
     * @param providerMethods    provider方法列表
     * @param injectMethods      inject方法列表
     */
    private void createDaggerComponent(TypeElement componentInterface, String className,
                                       List<ExecutableElement> providerMethods,
                                       List<ExecutableElement> injectMethods) {
        String packageName = elementUtils.getPackageOf(componentInterface).getQualifiedName().toString();
        String fullClassName = packageName + "." + className;

        // 检查是否已经生成过，避免重复生成
        if (isAlreadyGenerated(fullClassName)) {
            messager.printMessage(Diagnostic.Kind.NOTE, "Component already generated: " + fullClassName);
            return;
        }

        // 创建 Builder 类（用于构建模式）
        String builderClassName = className + "_Builder";
        TypeSpec builderType = createBuilderClass(className, builderClassName);

        // 创建接口方法的实现
        List<MethodSpec> interfaceMethods = new ArrayList<>();

        // 处理 provider 方法（有返回值的方法）
        for (ExecutableElement method : providerMethods) {
            TypeMirror returnTypeMirror = method.getReturnType();
            String methodName = method.getSimpleName().toString();

            // 确保返回类型的 Factory 已经生成
            createFactoryForType(returnTypeMirror);

            // 获取对应的 Factory 类名
            String factoryClassName = getFactoryClassName(returnTypeMirror);

            // 创建方法实现：调用对应 Factory 的 get 方法
            MethodSpec methodSpec = MethodSpec.methodBuilder(methodName)
                    .addAnnotation(Override.class) // 添加@Override注解
                    .addModifiers(Modifier.PUBLIC)
                    .returns(TypeName.get(returnTypeMirror))
                    .addStatement("return new $L().get()", factoryClassName) // 调用Factory获取实例
                    .build();

            interfaceMethods.add(methodSpec);
        }

        // 处理 inject 方法（void方法，用于字段注入）
        for (ExecutableElement method : injectMethods) {
            String methodName = method.getSimpleName().toString();

            // 获取方法的参数（通常只有一个参数，即要注入的目标对象）
            List<? extends VariableElement> parameters = method.getParameters();
            if (parameters.size() == 1) {
                VariableElement param = parameters.get(0);
                TypeMirror targetType = param.asType();
                TypeElement targetElement = (TypeElement) typeUtils.asElement(targetType);

                if (targetElement != null) {
                    // 生成对应的 MembersInjector 类名
                    String membersInjectorName = targetElement.getSimpleName() + "_MembersInjector";
                    String targetPackageName = elementUtils.getPackageOf(targetElement).getQualifiedName().toString();

                    // 创建方法实现：调用对应的 MembersInjector 进行字段注入
                    MethodSpec methodSpec = MethodSpec.methodBuilder(methodName)
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(TypeName.VOID)
                            .addParameter(TypeName.get(targetType), "target")
                            .addStatement("$T.inject(target)", ClassName.get(targetPackageName, membersInjectorName))
                            .build();

                    interfaceMethods.add(methodSpec);
                }
            }
        }

        // 如果没有任何有效的方法，则跳过创建
        if (interfaceMethods.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.WARNING,
                    "No valid methods found for component: " + componentInterface.getSimpleName());
            return;
        }

        // 创建主类的静态 builder 方法
        MethodSpec mainBuilderMethod = MethodSpec.methodBuilder("builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get("", builderClassName)) // 使用简单类名（内部类）
                .addStatement("return new $L()", builderClassName) // 返回Builder实例
                .build();

        // 创建主类（DaggerComponent实现类）
        TypeSpec componentType = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(TypeName.get(componentInterface.asType())) // 实现Component接口
                .addMethods(interfaceMethods) // 添加接口方法实现
                .addMethod(mainBuilderMethod) // 添加builder方法
                .addType(builderType) // 添加内部Builder类
                .build();

        // 写入Java文件
        JavaFile javaFile = JavaFile.builder(packageName, componentType)
                .build();

        try {
            javaFile.writeTo(filer);
            generatedFiles.add(fullClassName);
            messager.printMessage(Diagnostic.Kind.NOTE, "Created DaggerComponent: " + fullClassName);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Failed to create DaggerComponent: " + e.getMessage());
        }
    }

    /**
     * 创建 Builder 内部类，用于构建模式
     *
     * @param componentClassName 主类名
     * @param builderClassName   Builder类名
     * @return Builder类的TypeSpec定义
     */
    private TypeSpec createBuilderClass(String componentClassName, String builderClassName) {
        // Builder类的静态builder方法
        MethodSpec builderStaticMethod = MethodSpec.methodBuilder("builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get("", builderClassName)) // 返回Builder类型
                .addStatement("return new $L()", builderClassName) // 创建Builder实例
                .build();

        // Builder类的build方法
        MethodSpec buildMethod = MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get("", componentClassName)) // 返回主类类型
                .addStatement("return new $L()", componentClassName) // 创建主类实例
                .build();

        // 创建Builder类
        return TypeSpec.classBuilder(builderClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC) // 静态内部类
                .addMethod(builderStaticMethod) // 添加静态builder方法
                .addMethod(buildMethod) // 添加build方法
                .build();
    }

    /**
     * 为指定类型创建 Factory（如果类型支持）
     *
     * @param typeMirror 需要创建Factory的类型
     */
    private void createFactoryForType(TypeMirror typeMirror) {
        // 跳过不支持的类型：基本类型、void、数组等
        if (typeMirror.getKind().isPrimitive() ||
                typeMirror.getKind() == TypeKind.VOID ||
                typeMirror.getKind() == TypeKind.ARRAY) {
            return;
        }

        // 获取类型对应的元素
        TypeElement typeElement = (TypeElement) typeUtils.asElement(typeMirror);
        if (typeElement == null) {
            return;
        }

        // 创建具体的Factory
        createFactory(typeElement, typeMirror);
    }

    /**
     * 创建具体的 Factory 类
     *
     * @param typeElement 类型元素
     * @param typeMirror  类型镜像
     */
    private void createFactory(TypeElement typeElement, TypeMirror typeMirror) {
        String factoryName = getFactoryClassName(typeMirror);
        String packageName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
        String fullFactoryName = packageName + "." + factoryName;

        // 检查是否已经生成过
        if (isAlreadyGenerated(fullFactoryName)) {
            messager.printMessage(Diagnostic.Kind.NOTE, "Factory already generated: " + fullFactoryName);
            return;
        }

        // 创建get方法：返回类型的新实例
        MethodSpec methodSpec = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.get(typeMirror))
                .addStatement("return new $T()", TypeName.get(typeMirror)) // 创建新实例
                .build();

        // 创建Provider接口的泛型参数：Provider<T>
        TypeName providerType = ParameterizedTypeName.get(
                ClassName.get(Provider.class),
                TypeName.get(typeMirror));

        // 创建Factory类
        TypeSpec typeSpec = TypeSpec.classBuilder(factoryName)
                .addMethod(methodSpec) // 添加get方法
                .addSuperinterface(providerType) // 实现Provider接口
                .addModifiers(Modifier.PUBLIC)
                .build();

        // 写入Java文件
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                .build();

        try {
            javaFile.writeTo(filer);
            generatedFiles.add(fullFactoryName);
            messager.printMessage(Diagnostic.Kind.NOTE, "Created Factory: " + fullFactoryName);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Failed to create Factory: " + e.getMessage());
        }
    }

    /**
     * 根据类型镜像生成对应的 Factory 类名
     * 处理泛型类型，例如：List<User> 生成 List_User_Factory
     *
     * @param typeMirror 类型镜像
     * @return Factory类名
     */
    private String getFactoryClassName(TypeMirror typeMirror) {
        TypeElement typeElement = (TypeElement) typeUtils.asElement(typeMirror);
        if (typeElement == null) {
            return "Unknown_Factory";
        }

        String baseName = typeElement.getSimpleName().toString();

        // 处理泛型类型
        if (typeMirror instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) typeMirror;
            if (!declaredType.getTypeArguments().isEmpty()) {
                StringBuilder genericPart = new StringBuilder();
                // 为每个类型参数添加后缀
                for (TypeMirror typeArg : declaredType.getTypeArguments()) {
                    TypeElement typeArgElement = (TypeElement) typeUtils.asElement(typeArg);
                    if (typeArgElement != null) {
                        genericPart.append("_").append(typeArgElement.getSimpleName());
                    }
                }
                return baseName + genericPart + "_Factory";
            }
        }

        // 非泛型类型：类名 + _Factory
        return baseName + "_Factory";
    }

    /**
     * 为包含注入字段的类创建 MembersInjector
     * 用于注入字段值到目标实例中
     *
     * @param targetClass   需要注入字段的目标类
     * @param injectFields 需要注入的字段列表
     */
    private void createMembersInjector(TypeElement targetClass, List<VariableElement> injectFields) {
        String name = targetClass.getSimpleName() + "_MembersInjector";
        String packageName = elementUtils.getPackageOf(targetClass).getQualifiedName().toString();
        String fullName = packageName + "." + name;

        // 检查是否已经生成过
        if (isAlreadyGenerated(fullName)) {
            messager.printMessage(Diagnostic.Kind.NOTE, "MembersInjector already generated: " + fullName);
            return;
        }

        // 创建静态的inject方法：用于字段注入
        MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(TypeName.get(targetClass.asType()), "instance"); // 目标实例参数

        // 为每个注入字段添加赋值语句
        for (VariableElement field : injectFields) {
            String fieldName = field.getSimpleName().toString();
            TypeMirror fieldType = field.asType();

            // 获取对应的Factory类名
            String factoryClassName = getFactoryClassName(fieldType);
            // 添加字段赋值语句：instance.field = new Factory().get()
            injectMethodBuilder.addStatement("instance.$L = new $L().get()", fieldName, factoryClassName);
        }

        MethodSpec injectMethod = injectMethodBuilder.build();

        // 创建静态的create方法：创建实例并自动注入字段
        MethodSpec createMethod = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.get(targetClass.asType()))
                .addStatement("$T instance = new $T()", // 创建新实例
                        TypeName.get(targetClass.asType()),
                        TypeName.get(targetClass.asType()))
                .addStatement("inject(instance)") // 注入字段
                .addStatement("return instance") // 返回实例
                .build();

        // 创建MembersInjector类
        TypeSpec typeSpec = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(injectMethod) // 添加inject方法
                .addMethod(createMethod) // 添加create方法
                .build();

        // 写入Java文件
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                .build();

        try {
            javaFile.writeTo(filer);
            generatedFiles.add(fullName);
            messager.printMessage(Diagnostic.Kind.NOTE, "Created MembersInjector: " + fullName);
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Failed to create MembersInjector: " + e.getMessage());
        }
    }

    /**
     * 检查指定类是否已经生成过
     * 避免重复生成导致的编译错误
     *
     * @param fullClassName 完整类名（包名+类名）
     * @return 如果已经生成过返回true，否则返回false
     */
    private boolean isAlreadyGenerated(String fullClassName) {
        // 首先检查内存中的缓存（当前处理轮次中生成的）
        if (generatedFiles.contains(fullClassName)) {
            return true;
        }

        // 然后检查是否已经存在于元素中（之前轮次或其他处理器生成的）
        try {
            return elementUtils.getTypeElement(fullClassName) != null;
        } catch (Exception e) {
            return false;
        }
    }
}