#!/bin/bash

FILE=temp_levellistdraw.xml

echo " <level-list xmlns:android=\"http://schemas.android.com/apk/res/android\">" > $FILE
for  i in `seq 0 115`
do
  echo "<item android:maxLevel=\"$i\" android:drawable=\"@drawable/temp_$i\" />" >> $FILE
done

for  i in `seq 116 156`
do
  echo "<item android:maxLevel=\"$i\" android:drawable=\"@drawable/temp_$i\" />" >> $FILE
done


echo "</level-list>" >> $FILE
