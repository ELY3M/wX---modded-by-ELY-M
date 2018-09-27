#!/bin/sh
#
# CSV from http://simplemaps.com/resources/us-cities-data
#
# run dos2unix against it

for  s in `awk -F, '{print $2}' cities.csv | sort -u`
do
    s_l=`echo $s | tr [A-Z] [a-z]`
    grep ",${s}," cities.csv | awk -F, '{print $3","$4","$5}' |sort| awk -F, 'a !~ $1; {a=$1}' > city${s_l}.txt
done

# awk -F, 'a !~ $1; {a=$1}' MI.s | wc

