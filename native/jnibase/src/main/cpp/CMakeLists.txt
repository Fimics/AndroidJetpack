cmake_minimum_required(VERSION 3.22.1)# 最低支持Cmake版本

project("jnibase")
include_directories(fmod_includes)
add_library(
        jnibase
        SHARED
        x1_jnibase.cpp
        x2_array_object.cpp
        x2_extern_global_test.cpp
        x3_fmod.cpp)

#自动查找lib
find_library(
        log-lib
        log)
#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}")

target_link_libraries(
        jnibase
        ${log-lib}
#        fmod # 具体的库 链接到 libnative-lib.so里面去
        #fmodL # 具体的库 链接到 libnative-lib.so里面去
)