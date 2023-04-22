cmake_minimum_required(VERSION 3.4.1)

# 批量导入
file(GLOB allCPP *.cpp)

add_library(
        native-lib
        SHARED
        ${allCPP})

find_library(
        log-lib
        log)

#[[# 导入静态库 【第二方式】 可读性更强，代码多
add_library(getndk STATIC IMPORTED)
# 开始真正导入 静态库
set_target_properties(getndk PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/libgetndk.a)]]

# 导入动态库 【第二方式】 可读性更强，代码多  gradle版本有兼容问题
#add_library(getndk SHARED IMPORTED)
#set_target_properties(getndk PROPERTIES
#        IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}/libgetndk.so)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}")

# QQ变声就是这样导入库的 【第一种方式】 简洁，不好理解
# set(CMAKE_CXX_FLAGS )

target_link_libraries(
        native-lib
        ${log-lib}
        getndk # 链接此静态库 到 总库 libnative-lib.so
)