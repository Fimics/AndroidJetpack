cmake_minimum_required(VERSION 3.4.1)

# 引入子目录的CmakeList.txt rmtp静态库
add_subdirectory(librtmp)

#导入头文件的路径 自动查看头文件 <>  <faac.h>
include_directories(librtmp libx264/include libfaac/include)

# 只要有一个C++文件，就是属于C++ == CXX CMAKE_CXX_FLAGS
# set(CMAKE_CXX_FLAGS)

# 设置x264库路径
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/libx264/libs/${CMAKE_ANDROID_ARCH_ABI}")

# 设置faac库路径
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/libfaac/libs/${CMAKE_ANDROID_ARCH_ABI}")

# 代码太少，不需要批量导入
add_library(
        native-lib
        SHARED
        native-lib.cpp
        VideoChannel.cpp
        AudioChannel.cpp
)

target_link_libraries(
        native-lib
        rtmp
        x264
        faac
        log)