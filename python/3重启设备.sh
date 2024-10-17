#!/bin/bash

# 连接到设备
adb connect ai.wanpinghui.com:20634

# 重启设备
adb reboot

# 断开连接
adb disconnect