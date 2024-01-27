#!/bin/bash
#
# https://en.wikipedia.org/wiki/Latin-1_Supplement_%28Unicode_block%29
# for degree symbol copy/paste U+00B0
#
# downsize font from 16 to 14 to accommodate symbol
# point size from 14 to 13

for i in `seq 0 99`
do
   convert -size 25x25 -alpha transparent Xc:White -fill white -gravity Center  -weight 700 -pointsize 15 -annotate 0 "$i°" temp_$i.png
done
for i in `seq 100 115`
do
   convert -size 25x25 -alpha transparent Xc:White -fill white -gravity Center  -weight 700 -pointsize 13 -annotate 0 "$i°" temp_$i.png
done

for i in `seq 116 156`
do
   j=`expr $i - 115`
   convert -size 25x25 -alpha transparent Xc:White -fill white -gravity Center  -weight 700 -pointsize 13 -annotate 0 "-$j°" temp_$i.png
done

