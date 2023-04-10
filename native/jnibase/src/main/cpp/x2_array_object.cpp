#include <jni.h>
#include <string>
#include "com_mic_jnibase_NativeLib.h"
#include "xlog.h"
// jint == int
// jstring == String
// jintArray == int[]
// jobjectArray == 引用类型对象，例如 String[]   Test[]   Student[]  Person[]
extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_NativeLib_testArrayAction(JNIEnv *env, jobject thiz, jint count,
                                               jstring text_info, jintArray ints,
                                               jobjectArray strs) {
    // ① 基本数据类型  jint count， jstring text_info， 最简单的
    int countInt = count; // jint本质是int，所以可以用int接收
    LOGI("参数一 countInt:%d\n", countInt);

    // const char* GetStringUTFChars(jstring string, jboolean* isCopy)
    const char *textInfo = env->GetStringUTFChars(text_info, NULL);
    LOGI("参数二 textInfo:%s\n", textInfo);

    // ② 把int[] 转成 int*
    // jint* GetIntArrayElements(jintArray array, jboolean* isCopy)
    int *jintArray = env->GetIntArrayElements(ints, NULL);

    // Java层数组的长度
    // jsize GetArrayLength(jarray array) // jintArray ints 可以放入到 jarray的参数中去
    jsize size = env->GetArrayLength(ints);

    for (int i = 0; i < size; ++i) {
        *(jintArray + i) += 100; // C++的修改，影响不了Java层
        LOGI("参数三 int[]:%d\n", *jintArray + i);
    }
    // 目前无法控制Java的数组 变化 +100
    // 操作杆 ----> JMV
    // env->

    /**
     * 0:           刷新Java数组，并 释放C++层数组
     * JNI_COMMIT:  只提交 只刷新Java数组，不释放C++层数组
     * JNI_ABORT:   只释放C++层数组
     */
    env->ReleaseIntArrayElements(ints, jintArray, 0);
    // ③：jobjectArray 代表是Java的引用类型数组，不一样
    jsize strssize = env->GetArrayLength(strs);
    for (int i = 0; i < strssize; ++i) {
        jstring jobj = static_cast<jstring>(env->GetObjectArrayElement(strs, i));
        // 模糊：isCopy内部启动的机制
        // const char* GetStringUTFChars(jstring string, jboolean* isCopy)
        const char *jobjCharp = env->GetStringUTFChars(jobj, NULL);
        LOGI("参数四 引用类型String 具体的：%s\n", jobjCharp);
        // 释放jstring
        env->ReleaseStringUTFChars(jobj, jobjCharp);
    }
}

// jobject student == Student
// jstring str  == String
extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_NativeLib_putObject(JNIEnv *env, jobject thiz, jobject student, jstring str) {
    const char *strChar = env->GetStringUTFChars(str, NULL);
    LOGI("strChar：%s\n", strChar);
    env->ReleaseStringUTFChars(str, strChar);
    // --------------
    // 1.寻找类 Student
    // jclass studentClass = env->FindClass("com/derry/as_jni_project/Student"); // 第一种
    jclass studentClass = env->GetObjectClass(student); // 第二种

    // 2.Student类里面的函数规则  签名
    jmethodID setName = env->GetMethodID(studentClass, "setName", "(Ljava/lang/String;)V");
    jmethodID getName = env->GetMethodID(studentClass, "getName", "()Ljava/lang/String;");
    jmethodID showInfo = env->GetStaticMethodID(studentClass, "showInfo", "(Ljava/lang/String;)V");

    // 3.调用 setName
    jstring value = env->NewStringUTF("AAAA");
    env->CallVoidMethod(student, setName, value);

    // 4.调用 getName
    jstring getNameResult = static_cast<jstring>(env->CallObjectMethod(student, getName));
    const char *getNameValue = env->GetStringUTFChars(getNameResult, NULL);
    LOGE("调用到getName方法，值是:%s\n", getNameValue);

    // 5.调用静态showInfo
    jstring jstringValue = env->NewStringUTF("静态方法你好，我是C++");
    env->CallStaticVoidMethod(studentClass, showInfo, jstringValue);
}

// C++ 堆 栈 ...
// JNI函数  局部引用，全局引用，...
extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_NativeLib_insertObject(JNIEnv *env, jobject thiz) {
    // 好习惯：
    // 我用完了，我记释放，在我函数执行过程中，不会导致 内存占用多
//    env->ReleaseStringUTFChars()*/

    // 1.通过包名+类名的方式 拿到 Student class  凭空拿class
    const char *studentstr = "com/mic/jnibase/Student";
    jclass studentClass = env->FindClass(studentstr);
    // 2.通过student的class  实例化此Student对象   C++ new Student
    jobject studentObj = env->AllocObject(studentClass); // AllocObject 只实例化对象，不会调用对象的构造函数
    // 方法签名的规则
    jmethodID setName = env->GetMethodID(studentClass, "setName", "(Ljava/lang/String;)V");
    jmethodID setAge = env->GetMethodID(studentClass, "setAge", "(I)V");

    // 调用方法
    jstring strValue = env->NewStringUTF("Derry");
    env->CallVoidMethod(studentObj, setName, strValue);
    env->CallVoidMethod(studentObj, setAge, 99);
    // env->NewObject() // NewObject 实例化对象，会调用对象的构造函数
    // ====================  下面是 Person对象  调用person对象的  setStudent 函数等

    // 4.通过包名+类名的方式 拿到 Student class  凭空拿class
    const char *personstr = "com/mic/jnibase/Person";
    jclass personClass = env->FindClass(personstr);
    jobject personObj = env->AllocObject(personClass); // AllocObject 只实例化对象，不会调用对象的构造函数
    // setStudent 此函数的 签名 规则
    jmethodID setStudent = env->GetMethodID(personClass, "setStudent",
                                            "(Lcom/mic/jnibase/Student;)V");
    env->CallVoidMethod(personObj, setStudent, studentObj);
    // 规范：一定记得释放【好习惯】
    // 第一类
    env->DeleteLocalRef(studentClass);
    env->DeleteLocalRef(personClass);
    env->DeleteLocalRef(studentObj);
    env->DeleteLocalRef(personObj);
    // 第二类
    // env->ReleaseStringUTFChars()
    // TODO 局部引用： jobject jclass jstring ...  【函数结束后，会自动释放】
}

jclass dogClass; // 你以为这个是全局引用，实际上他还是局部引用
extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_NativeLib_testQuote(JNIEnv *env, jobject thiz) {
    if (NULL == dogClass) {
        /*const char * dogStr = "com/derry/as_jni_project/Dog";
        dogClass = env->FindClass(dogStr);*/

        // 升级全局引用： JNI函数结束也不释放，反正就是不释放，必须手动释放   ----- 相当于： C++ 对象 new、手动delete
        const char * dogStr = "com/mic/jnibase/Dog";
        jclass temp = env->FindClass(dogStr);
        dogClass = static_cast<jclass>(env->NewGlobalRef(temp)); // 提升全局引用
        // 记住：用完了，如果不用了，马上释放，C C++ 工程师的赞美
        env->DeleteLocalRef(temp);
    }

    // <init> V  是不会变的
    // 构造函数一
    jmethodID init = env->GetMethodID(dogClass, "<init>", "()V");
    jobject dog = env->NewObject(dogClass, init);
    // 构造函数2
    init = env->GetMethodID(dogClass, "<init>", "(I)V");
    dog = env->NewObject(dogClass, init, 100);
    // 构造函数3
    init = env->GetMethodID(dogClass, "<init>", "(II)V");
    dog = env->NewObject(dogClass, init, 200, 300);
    // 构造函数4
    init = env->GetMethodID(dogClass, "<init>", "(III)V");
    dog = env->NewObject(dogClass, init, 400, 500, 600);

    env->DeleteLocalRef(dog); // 释放
    // dogClass = NULL; // 是不是问题解决了，不能这样干（JNI函数结束后，还怎么给你释放呢）
    // 这样就解决了
    /*env->DeleteGlobalRef(studentClass);
    studentClass = NULL;*/
}

// JNI函数结束，会释放局部引用   dogClass虽然被释放，但是还不等于NULL，只是一个悬空指针而已，所以第二次进不来IF，会奔溃
// 非常方便，可以使用了
extern int age; // 声明age
extern void show(); // 声明show函数  5000行代码

// 手动释放全局引用
extern "C"
JNIEXPORT void JNICALL
Java_com_mic_jnibase_NativeLib_delQuote(JNIEnv *env, jobject thiz) {
    if (dogClass != NULL) {
        LOGE("全局引用释放完毕，上面的按钮已经失去全局引用，再次点击会报错");
        env->DeleteGlobalRef(dogClass);
        dogClass = NULL; // 最好给一个NULL，指向NULL的地址，不要去成为悬空指针，为了好判断悬空指针的出现
    }
    // 测试下
    show();
}

//33333333333333333333333333333--------------------------------------------------
