#!/bin/bash

# 连接到设备
adb connect ai.wanpinghui.com:20515

# 增加音量
adb shell "input keyevent 24"

# 断开连接
adb disconnect



