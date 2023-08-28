cmake_minimum_required(VERSION 3.22.1)

file(GLOB allCpp *.cpp)

add_library(
        native-lib
        SHARED
        ${allCpp})

find_library(
        log-lib
        log)

# 引入FFmpeg的头文件
include_directories(${CMAKE_SOURCE_DIR}/include)

# 引入FFmpeg的库文件，设置内部的方式引入，指定库的目录是 -L  指定具体的库-l
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/${CMAKE_ANDROID_ARCH_ABI}")

target_link_libraries(
        native-lib # 总库libnative-lib.so

        # 忽略顺序的方式，导入
        -Wl,--start-group
        avcodec avfilter avformat avutil swresample swscale
        -Wl,--end-group

        log # 日志库，打印日志用的
        z # libz.so库，是FFmpeg需要用ndk的z库，FFMpeg需要额外支持  libz.so
        android # android 后面会专门介绍，目前先要明白的是 ANativeWindow 用来渲染画面的 == Surface对象
        OpenSLES # OpenSLES 后面会专门介绍，目前先要明白的是 OpenSLES 用来播放声音的-特别是在native层播放（最快的）
)