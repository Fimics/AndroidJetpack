//
// Created by mac on 2023/4/11.
//
#include <jni.h>
#include <string>
#include "com_mic_jnibase_NativeLib.h"
#include "xlog.h"
/**
 * 在AS上 pthread不需要额外配置，默认就有
 */
#include <pthread.h>



// 默认情况下，就是静态注册，静态注册是最简单的方式，NDK开发过程中，基本上使用静态注册
// Android 系统的C++源码：基本上都是动态注册（麻烦）

// 静态注册： 优点：开发简单
// 缺点
// 1.JNI函数名非常长
// 2.捆绑 上层 包名 + 类名
// 3.运行期 才会去 匹配JNI函数，性能上 低于 动态注册
extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_NativeLib_staticRegister(JNIEnv *env, jobject thiz) {
    // TODO: implement staticRegister()
}
static JavaVM *jVm = nullptr; // 0x003545 系统乱值，C++11后，取代NULL，作用是可以初始化指针赋值
static  char *mainActivityClassName = "com/mic/jnibase/NativeLib";

// native 真正的函数
// void dynamicMethod01(JNIEnv *env, jobject thiz) { // OK的
void dynamicMethod01(JNIEnv *env, jobject thiz) { // 也OK  如果你用不到  JNIEnv jobject ，可以不用写
    LOGD("我是动态注册的函数 dynamicMethod01...");
}

int dynamicMethod02(JNIEnv *env, jobject thiz, jstring valueStr) { // 也OK
    const char *text = env->GetStringUTFChars(valueStr, nullptr);
    LOGD("我是动态注册的函数 dynamicMethod02... %s", text);
    env->ReleaseStringUTFChars(valueStr, text);
    return 200;
}

/*
     typedef struct {
        const char* name;       // 函数名
        const char* signature; // 函数的签名
        void*       fnPtr;     // 函数指针
     } JNINativeMethod;
     */
static const JNINativeMethod jniNativeMethod[] = {
        {"dynamicJavaMethod01", "()V",                   (void *) (dynamicMethod01)},
        {"dynamicJavaMethod02", "(Ljava/lang/String;)I", (int *) (dynamicMethod02)},
};

//2调用System.loadLibrary("native-lib") 时会调用JNI_OnLoad函数
// Java：像 Java的构造函数，如果你不写构造函数，默认就有构造函数，如果你写构造函数 覆写默认的构造函数
// JNI JNI_OnLoad函数，如果你不写JNI_OnLoad，默认就有JNI_OnLoad，如果你写JNI_OnLoad函数 覆写默认的JNI_OnLoad函数
extern "C"
JNIEXPORT jint JNI_OnLoad(JavaVM *javaVm, void *) {
    // this.javaVm = javaVm;
    ::jVm = javaVm;
    // 做动态注册 全部做完
    JNIEnv *jniEnv = nullptr;
    int result = javaVm->GetEnv(reinterpret_cast<void **>(&jniEnv), JNI_VERSION_1_6);
    // result 等于0  就是成功    【C库 FFmpeg 成功就是0】
    if (result != JNI_OK) {
        return -1; // 会奔溃，故意奔溃
    }

    LOGE("System.loadLibrary ---》 JNI Load init");
    jclass mainActivityClass = jniEnv->FindClass(mainActivityClassName);
    // jint RegisterNatives(Class, 我们的数组==jniNativeMethod， 注册的数量 = 2)
    jniEnv->RegisterNatives(mainActivityClass,
                            jniNativeMethod,
                            sizeof(jniNativeMethod) / sizeof(JNINativeMethod));
    LOGE("动态 注册没有毛病");
    return JNI_VERSION_1_6; //  // AS的JDK在JNI默认最高1.6      存Java的JDKJNI 1.8
}
//2--------------------------------------------线程区-----------------------------------
class MyContext {
public:
    JNIEnv *jniEnv = nullptr;  // 不能跨线程 ，会奔溃
    jobject instance = nullptr; // 不能跨线程 ，会奔溃
};

//当前是异步线程
void *myThreadTaskAction(void *pVoid) {
    LOGE("myThreadTaskAction run");
    // 需求：有这样的场景，例如：下载完成 ，下载失败，等等，必须告诉Activity UI端，所以需要在子线程调用UI端
    // 这两个是必须要的
    // JNIEnv *env
    // jobject thiz   OK

    MyContext *myContext = static_cast<MyContext *>(pVoid);
    // jclass mainActivityClass = myContext->jniEnv->FindClass(mainActivityClassName); // 不能跨线程 ，会奔溃
    // mainActivityClass = myContext->jniEnv->GetObjectClass(myContext->instance); // 不能跨线程 ，会奔溃

    // TODO 解决方式 （安卓进程只有一个 JavaVM，是全局的，是可以跨越线程的）
    JNIEnv *jniEnv = nullptr; // 全新的JNIEnv  异步线程里面操作
    // 附加当前异步线程后，会得到一个全新的 env，此env相当于是子线程专用env
    jint attachResult = ::jVm->AttachCurrentThread(&jniEnv,nullptr);
    if (attachResult != JNI_OK) {
        return 0; // 附加失败，返回了
    }

    // 1.拿到class
    jclass mainActivityClass = jniEnv->GetObjectClass(myContext->instance);
    // 2.拿到方法
    jmethodID updateActivityUI = jniEnv->GetMethodID(mainActivityClass, "updateActivityUI", "()V");
    // 3.调用
    jniEnv->CallVoidMethod(myContext->instance, updateActivityUI);
    ::jVm->DetachCurrentThread(); // 必须解除附加，否则报错
    LOGE("C++ 异步线程OK");
    return nullptr;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_NativeLib_nativeThread(JNIEnv *env, jobject thiz) {
    MyContext *myContext = new MyContext;
    myContext->jniEnv = env;
    // myContext->instance = job; // 默认是局部引用，会奔溃
    myContext->instance = env->NewGlobalRef(thiz); // 提升全局引用

    pthread_t pid;
    pthread_create(&pid, nullptr, myThreadTaskAction, myContext);
    pthread_join(pid, nullptr);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_NativeLib_closeThread(JNIEnv *env, jobject thiz) {
    // 做释放工作
}

void *run(void *) {
    // native的子线程 env地址  和  Java的子线程env地址，一样吗  不一样的
    JNIEnv *newEnv = nullptr;
    ::jVm->AttachCurrentThread(&newEnv, nullptr);
    // 打印：当前函数env地址， 当前函数jvm地址， 当前函数clazz地址,  JNI_OnLoad的jvm地址

    LOGE("run jvm地址:%p,  当前run函数的newEnv地址:%p \n", ::jVm, newEnv);

    ::jVm->DetachCurrentThread();
    return nullptr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_NativeLib_nativeFun1(JNIEnv *env, jobject thiz) {
    JavaVM *javaVm = nullptr;
    env->GetJavaVM(&javaVm);
    // 打印：当前函数env地址， 当前函数jvm地址， 当前函数job地址,  JNI_OnLoad的jvm地址
    LOGE("nativeFun1 当前函数env地址%p,  当前函数jvm地址:%p,  当前函数job地址:%p, JNI_OnLoad的jvm地址:%p\n", env, javaVm,
         thiz, ::jVm);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_NativeLib_nativeFun2(JNIEnv *env, jobject thiz) {
    JavaVM *javaVm = nullptr;
    env->GetJavaVM(&javaVm);

    // 打印：当前函数env地址， 当前函数jvm地址， 当前函数job地址,  JNI_OnLoad的jvm地址
    LOGE("nativeFun2 当前函数env地址%p,  当前函数jvm地址:%p,  当前函数job地址:%p, JNI_OnLoad的jvm地址:%p\n", env, javaVm,
         thiz, ::jVm);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_NativeLib_staticFun3(JNIEnv *env, jclass clazz) {
    JavaVM *javaVm = nullptr;
    env->GetJavaVM(&javaVm);
    // 打印：当前函数env地址， 当前函数jvm地址， 当前函数clazz地址,  JNI_OnLoad的jvm地址
    LOGE("nativeFun3 当前函数env地址%p,  当前函数jvm地址:%p,  当前函数clazz地址:%p, JNI_OnLoad的jvm地址:%p\n", env,
         javaVm, clazz, ::jVm);
    // 调用run
    pthread_t pid;
    pthread_create(&pid, nullptr, run, nullptr);
}

// Java子线程调用的
extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_NativeLib_staticFun4(JNIEnv *env, jclass clazz) {
    JavaVM *javaVm = nullptr;
    env->GetJavaVM(&javaVm);
    // 打印：当前函数env地址， 当前函数jvm地址， 当前函数clazz地址,  JNI_OnLoad的jvm地址
    LOGE("nativeFun4 当前函数env地址%p,  当前函数jvm地址:%p,  当前函数clazz地址:%p, JNI_OnLoad的jvm地址:%p\n", env,
         javaVm, clazz, ::jVm);
}

// TODO 纠结细节的结论：
// 1. JavaVM全局，绑定当前进程， 只有一个地址
// 2. JNIEnv线程绑定， 绑定主线程，绑定子线程
// 3. jobject 谁调用JNI函数，谁的实例会给jobject

// JNIEnv *env 不能跨越线程，否则奔溃，  他可以跨越函数 【解决方式：使用全局的JavaVM附加当前异步线程 得到权限env操作】
// jobject thiz 不能跨越线程，否则奔溃，不能跨越函数，否则奔溃 【解决方式：默认是局部引用，提升全局引用，可解决此问题】
// JavaVM 能够跨越线程，能够跨越函数