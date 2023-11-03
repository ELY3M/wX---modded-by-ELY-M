#!/bin/bash
#
# assemble production release but do nothing else
# used for temp testing
#
#export NDK_TOOLCHAIN_VERSION=clang 
#export PATH=~/android-studio/jre/bin:$PATH
APP_NAME=wX
tmpPath=${HOME}/tmp

#
# compile
#
echo Compile release
export PATH=/Applications/"Android Studio.app"/Contents/jre/Contents/Home/bin:~/android-studio/jre/bin:$PATH
./gradlew assembleRelease --warning-mode=all

#
# copy
#
cp app/build/outputs/apk/release/app-release.apk ${HOME}/StudioProjects/wX-testing.apk
