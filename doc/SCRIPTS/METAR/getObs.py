#!/usr/bin/python
#
# parse file found at https://www.aviationweather.gov/docs/metar/stations.txt
#
# compare to https://tgftp.nws.noaa.gov/data/observations/metar/decoded/

# state 0-1
# name 3-18
# icao 20-22
# metar 62 

# us_metar3.txt 
# KDRM 46.00 -083.75000000000000000000
# KDRO 37.15000000000000000000 -107.76666666666666666666
#

# stations_us4.txt
#
# AK,AKUTAN          ,PAUT
# AK,AMBLER          ,PAFM
#


import urllib2
import re
from datetime import datetime, timedelta

class MetarSite:
	def __init__(self, state, name, icao, lat, lon):
		self.state = state
		self.name = name
		self.icao = icao
		self.lat = lat
		self.lon = lon
	def __str__(self):
     		return self.state + ", " + self.name + ":" + self.icao + ":" + self.lat + ":" + self.lon

# https://docs.python.org/3/library/time.html#time.strftime
class MetarData:
	def __init__(self, site, time):
		self.site = site
		self.time = time
		self.realTime = datetime.strptime(time, '%d-%b-%Y %H:%M')
		self.isCurrent = True
		if self.realTime < thirtyDaysAgo:
			self.isCurrent = False
	def __str__(self):
     		return self.site + ", " + str(self.isCurrent) + " " + str(self.realTime)

# expects something like "176 39"
# returns something like "176.50"
#
def convertToDegrees(numberString):
	tokens = numberString.split()	
	number1 = float(tokens[0])
	number2 = float(tokens[1]) / 60.0	
	return str(number1 + number2)

def writeSiteToFiles(name):
	stationsList.append(metarSitesByIcao[name].state + "," + metarSitesByIcao[name].name + "," + metarSitesByIcao[name].icao + "\n")
	metarList.append(metarSitesByIcao[name].icao  + " " + metarSitesByIcao[name].lat  + " -" + metarSitesByIcao[name].lon  + "\n")

def printToTwoFiles():
	stationsList.sort()
	metarList.sort()
	for line in stationsList:
		fileHandleStations.write(line)
	for line in metarList:
		fileHandleMetar.write(line)


# <tr><td><a href="A302.TXT">A302.TXT</a></td><td align="right">09-Sep-2011 16:09  </td><td align="right">329 </td></tr>
# https://developers.google.com/edu/python/regular-expressions
def processTgftpObs(data):
	#print(data)
	for line in data.splitlines():
		matchSite = patternSite.search(line)
		matchTime = patternTime.search(line)
		if matchSite and matchTime:
    			#print(matchSite.group(1))
			#print(matchTime.group(1))
			metarData.append(MetarData(matchSite.group(1), matchTime.group(1)))
		
file="stations.txt"
url="https://tgftp.nws.noaa.gov/data/observations/metar/decoded/"
patternSite = re.compile(r'href=.([A-Z0-9]{4}).TXT')
patternTime = re.compile(r'>([0-9]{2}-[A-Za-z]{3}-[0-9]{4} [0-9]{2}:[0-9]{2})  <')
metarSites = []
metarSitesByIcao = {}
metarData = []
thirtyDaysAgo = datetime.now() - timedelta(days=30)

#
# open two files for writing
#
fileHandleStations = open("stations_us4.txt", 'w')
fileHandleMetar = open("us_metar3.txt", 'w')
stationsList = []
metarList = []

with open(file) as fp:
	for line in fp:
		data = line.rstrip()
		if len(data) > 64 and data[62]=="X":
			state = data[0:2]   # State
			name = data[3:19]  # Name
			icao = data[20:24] # ICAO
			latitudeString = data[39:44]    # LAT
			longitudeString = data[47:53]    # LON
			lat = convertToDegrees(latitudeString)
			lon = convertToDegrees(longitudeString)
			localSite = MetarSite(state, name, icao, lat, lon)
			metarSites.append(localSite)
			metarSitesByIcao[icao] = localSite
	fp.close()


#print(len(metarSites))
#for site in metarSites:
#	print(site)

response = urllib2.urlopen(url)
data = response.read()
processTgftpObs(data)

activeSites = 0
for site in metarData:
	if site.isCurrent:
		#print(site.site)
		if site.site in metarSitesByIcao.keys():
			#print(metarSitesByIcao[site.site])
			writeSiteToFiles(site.site)
		#else:
			#print("NOT FOUND: " + site.site)

		activeSites += 1

printToTwoFiles()
#print("TGFTP sites: " + str(len(metarData)))
#print("TGFTP active sites: " + str(activeSites))
