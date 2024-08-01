#!/bin/sh
VERSIONNAME=$(grep -Po 'android:versionName="(.*)"' app/src/main/AndroidManifest.xml | sed -r 's/.*android:versionName="([^ ]+)".*/\1/')
echo VERSIONNAME is: $VERSIONNAME
git add . 
git commit -m $VERSIONNAME
git push