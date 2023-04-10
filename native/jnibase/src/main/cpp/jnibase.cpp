#include <jni.h>
#include <string>
#include "com_mic_jnibase_NativeLib.h"
#include "xlog.h"

/**
 * extern "C"： 必须采用C的编译方式，为什么，请看JNIEnv内部源码
 *无论是C还是C++ 最终是调用到 C的JNINativeInterface，所以必须采用C的方式 extern "C"函数的实现
 */
extern "C"

/**
 * 标记该方法可以被外部调用（VS上不加入 运行会报错， AS上不加入运行没有问题）
 *Linux运行不加入，不报错,  Win 你必须加入 否则运行报错,   MacOS 还不知道
 */
JNIEXPORT
//Java <---> native 转换用的
jstring
// 代表是 JNI标记，可以少
JNICALL

/**
 *Java_包名_类名_方法名  ，注意：我们的包名 _
 * JNIEnv * env  JNI：的桥梁环境    300多个函数，所以的JNI操作，必须靠他
 *jobject jobj  谁调用，就是谁的实例  MainActivity this
 * jclass clazz 谁调用，就是谁的class MainActivity.class
 */
Java_com_mic_jnibase_NativeLib_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_NativeLib_changeName(JNIEnv *env, jobject thiz) {
    // 获取class
    jclass jc = env->GetObjectClass(thiz);
    // 获取属性  L对象类型 都需要L
    // jfieldID GetFieldID(MainActivity.class, 属性名, 属性的签名)
    jfieldID jfield_name = env->GetFieldID(jc, "name", "Ljava/lang/String;");
    // 转换工作
    jstring j_str= static_cast<jstring>(env->GetObjectField(thiz, jfield_name));
    // 打印字符串  目标
    char * c_str=const_cast<char *>(env->GetStringUTFChars(j_str,NULL));
    LOGD("native:%s\n",c_str);
    //修改name
    jstring  jname=env->NewStringUTF("python");
    env->SetObjectField(thiz,jfield_name,jname);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_NativeLib_changeAge(JNIEnv *env, jclass clazz) {
    jfieldID  j_fid=env->GetStaticFieldID(clazz,"A","I");
    jint age=env->GetStaticIntField(clazz,j_fid);
    age=age+10;
    env->SetStaticIntField(clazz,j_fid,age);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_NativeLib_callAddMethod(JNIEnv *env, jobject thiz) {
    jclass j_class =env->GetObjectClass(thiz);
    jmethodID j_method=env->GetMethodID(j_class,"add","(II)I");
    jint  sum = env->CallIntMethod(thiz,j_method,3,5);
    LOGD("sum result:%d\n",sum);
}