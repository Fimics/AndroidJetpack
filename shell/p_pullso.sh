#!/bin/bash

adb pull /system/lib64/libc++.so  ./xsys_so
adb pull /system/lib64/libutils.so  ./xsys_so
adb pull /system/lib64/libcutils.so  ./xsys_so
adb pull /system/lib64/libtinyalsa.so  ./xsys_so
adb pull /system/lib64/libbacktrace.so ./xsys_so
adb pull /system/lib64/libbase.so  ./xsys_so
adb pull /system/lib64/liblzma.so ./xsys_so
adb pull /system/lib64/libssl.so  ./xsys_so
adb pull /system/lib64/libunwindstack.so  ./xsys_so