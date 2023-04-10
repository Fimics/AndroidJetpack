cmake_minimum_required(VERSION 3.22.1)
project("jnibase")
add_library(
        jnibase
        SHARED
        jnibase.cpp
        extern_global_test.cpp)

find_library(
        log-lib
        log)

target_link_libraries(
        jnibase
        ${log-lib})