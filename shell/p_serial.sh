#!/bin/bash


# 模拟器用到的串口无非是 ttyS0 ~ ttyS3 索性全部授权
adb shell chmod 777 /dev/ttyS*
# adb -s shell chmod 777 /dev/ttyS*

#1.使能
#O1F3016B
#
#
#2.运动控制指令：控制闭环电机相对运动的角度即位置模式控制
#01FD02FF00000c806B
#
#
#usb 线序
#黑->voc
#黄->tx
#红->rx
#绿->gnd
#
#电机 轴向下
#绿->左上
#红->  左下
#黄 ->右下