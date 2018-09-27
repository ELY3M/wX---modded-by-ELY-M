#!/bin/bash

while IFS=' ' read station lat1 lat2 lon1 lon2
do
   if [ "$lat2" == "00" ]; then
       lat_prec=".00"
   else
      lat_prec=`echo "$lat2/60" | bc -l`
   fi

   if [ "$lon2" == "00" ]; then
       lon_prec=".00"
   else
       lon_prec=`echo "$lon2/60" | bc -l`
   fi
   echo $station $lat1$lat_prec -$lon1$lon_prec
done < us_metar2.txt > us_metar3.txt
