#!/bin/bash

FILE=hash

# nws_icon.put("bkn.png",R.drawable.bkn);

echo > $FILE

for  i in `seq 0 115`
do
  echo "temp_icon.put(\"$i\",R.drawable.temp_$i);"  >> $FILE
done

for  i in `seq 116 156`
do
  j=`expr 115 - $i`
  echo "temp_icon.put(\"$j\",R.drawable.temp_$i);" >> $FILE
done


