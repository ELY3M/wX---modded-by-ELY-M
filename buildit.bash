#!/bin/bash
#
# assemble production release and backup
#
#export NDK_TOOLCHAIN_VERSION=clang 
#export PATH=~/android-studio/jre/bin:$PATH
APP_NAME=wX
dropBoxPath=${HOME}/Dropbox/wX/
#dateString=$(date '+%Y_%m_%d_%H_%M')
dateString=$(date '+%Y/%m/%d %H:%M')
gitRepo="https://gitlab.com/joshua.tee/wx.git"
gitRepoShortname="wx"
ver=$(grep versionName app/src/main/AndroidManifest.xml |awk -F'"'  '{print $2}')
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
cp app/build/outputs/apk/release/app-release.apk ${HOME}/StudioProjects/wX${ver}.apk

#
# git stuff
#
echo Git commit and push
touch doc/ChangeLog.txt
git commit -a -m "release ${ver} commit ${dateString}"
git push origin master

# needed for f-droid
if [ "$1" == "-t" ]; then
	echo Git tag for fdroid
	git tag ${ver}
	git push origin --tags
fi

#
# git clone / zip
#
echo Clone and zip
rm -rf ${tmpPath}
mkdir ${tmpPath} || exit 1
cd ${tmpPath} || exit 1
git clone ${gitRepo}
zip -r $APP_NAME${ver}.zip ${gitRepoShortname} -x *.git* > /dev/null
cp $APP_NAME${ver}.zip ~/StudioProjects/
rm -rf ${tmpPath} 

#
# copy to my storage
#
echo Copy to Storage
cd ${HOME}/StudioProjects
ls -l $APP_NAME${ver}.zip 
ls -l wX${ver}.apk 
cp wX${ver}.apk ${dropBoxPath}
cp $APP_NAME${ver}.zip  ${dropBoxPath}
