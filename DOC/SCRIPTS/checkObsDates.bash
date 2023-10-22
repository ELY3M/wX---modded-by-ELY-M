#!/bin/bash
#
# https://w1.weather.gov/xml/current_obs/seek.php?state=nd&Find=Find
# https://tgftp.nws.noaa.gov/data/observations/metar/stations/
# https://forecast-v3.weather.gov/obs?state=MI
#
obsFile=/Users/josh/StudioProjects/wX/app/src/main/res/raw/us_metar3.txt
url=https://tgftp.nws.noaa.gov/data/observations/metar/stations/
tmpDir=/var/tmp
dateToCheck=$(date '+%d-%b-%Y')
awk '{print $1}' ${obsFile} |sort > ${tmpDir}/obs.sorted
curl ${url} > ${tmpDir}/html
rm ${tmpDir}/badObs2
for i in $(cat ${tmpDir}/obs.sorted)
do
  grep $i ${tmpDir}/html | grep -v ${dateToCheck}  | awk -F'<' '{print $4" "$7}' | awk -F'>' '{print $1" "$3}' | sed 's/a href="//'|sed 's/.TXT"//' >> ${tmpDir}/badObs2
done

#grep href  observations_metar_stations.html | awk -F'<' '{print $4" "$7}' | awk -F'>' '{print $1" "$3}' | sed 's/a href="//'|sed 's/.TXT"//'|sort >obs2
#join obs1 obs2|grep -v "Jun-2019"


