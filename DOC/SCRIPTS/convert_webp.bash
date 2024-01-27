#!/bin/bash
#
# Script assumes backup copy has been made
# cp -rp $HOME/StudioProjects/wX/app/src/main/res/drawable-nodpi $HOME/StudioProjects/wX/DOC
#
WEBP_BIN=$HOME/libwebp-0.5.0-linux-x86-64/bin/cwebp
IMG_PATH=$HOME/StudioProjects/wX/app/src/main/res/drawable-nodpi
cd $IMG_PATH || exit 1
for i in `ls *`
do
  FILE_PREFIX=`echo $i | awk -F. '{print $1}'`
  echo $FILE_PREFIX 
  $WEBP_BIN $i -o $FILE_PREFIX.webp
done
