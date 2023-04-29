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
        native-lib

        # 具体的库文件，链接到总库
        # 这样写会报错，有坑
        # avcodec avfilter avformat avutil swresample swscale

        # 先把有依赖的库，先依赖进来
        avcodec
        avformat
        avutil
        swscale

        ${log-lib})