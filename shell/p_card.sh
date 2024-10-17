#!/bin/bash

adb root
sleep 2
adb remount
sleep 2
adb shell setenforce 0
sleep 1
adb shell chmod 777 /dev/snd/*
sleep 2
adb shell ls -l /dev/snd