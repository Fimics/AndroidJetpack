#!/bin/bash

#./record_android_trace -t 30s sched gfx wm -a com.wp.one


./record_android_trace -o trace_file.perfetto-trace -t 30s -b 64mb \
sched freq idle am wm gfx view binder_driver hal dalvik camera input res memory cpu

