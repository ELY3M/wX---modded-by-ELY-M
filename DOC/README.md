wX is a free and Open Source ( GPL3 ) Android application that is geared towards storm chasers, meteorologists, and weather enthusiasts but can be used by anyone with ease. The application is for the contiguous 48 states in the US however Alaska and Hawaii are partially supported. Additionally Puero Rico and Guam now have limited support as documented in "BUGS"

Canada is now partially supported as of version 3.3+.

Features include:

- NWS current conditions and 7 day forecast for unlimited locations. Click on current conditions and you will get the SPC sounding/hodograph image
  for that NWS office. Everything on the first screen is clickable and usually expands or shows more data.
- Action bar icons provide direct access to AFD ( area forecast discussion ), NEXRAD interface for your location,
  Visible/IR imagery, statewide observations, and a graphic of nearby weather alerts.
- SPC tab with access to most SPC products include Severe weather outlooks, storm reports, Mesoanalysis, thunderstorm outlook, MCD
   ( mesoscale discussions ), watches, and SPC SREF. Interfaces are not raw web pages but instead use native Android widget thus providing a mobile optimized and enhanced interface. For example, on any given MCD you can long-press on the image and add the locations ( specified by ATTM in the MCD text ) to the location manager for further investigation.
- MISC tab with access to the COD Model interface data with a native Android interface (including GFS,NAM,ECMWF,RAP,HRRR,NAMK4KM,SREF,CFSv2),
  NWS Radar mosaics, and twitter widgets with the 20 most recent entries for state searches ( example: #miwx ), #tornado, and weather underground. 
- Images tab with access to the COD mosaics ( Radar, vis, ir, wv ) and 1km/2km/sector/national levels with animations at 6,12, and 18 frame lengths.
- NEXRAD interface ( accessible from action bar ) with favorites, map selection, multiple products (br,bv,srm,vil,eet,1hr), and animation.
   You can choose from COD, NWS(IOWA), WX, WXOGL. The NWS images are actually NWS overlay with IOWA Mesonet providing the highres
   radar images for base reflectivity and base velocity. Additionally access to 1km,2km,sector, and continent radar/vis/ir/wv is also
   accessible from within the NEXRAD interface and all with 6,12,18 frame animation. WX and WXOGL download and decode radar data locally and
   render to bitmap ( WX ) or OpenGL ( WXOGL ). WXOGL is the default.
- Interface to view current severe weather alerts in the US. For example The tile icon by default will  show any Tornado or severe Tstorm warning.
- Text to speach for many text based activities
- Voice regonition for basic operations from the main screen.
- Notifications including the following:
* severe weather alerts for locations
* optionally when an alert occurs provide an Android BigPicture notification containing the current base reflectivity from your radar site
* current conditions notification ( for quick access )
* 7 day forecast ( for quick access )
* Any tornado warning in the country
* SPC MCD
* SPC Watch
* slight risk or higher for D1-3 for SPC SWO
* SPC MCD for a specific location
* SPC SWO ( D1-8 ) for a specific location
* SPC Fire weather outlook ( D1-2 ) for a specific location

Most images support double tap for zoom. 

NOTIFICATION NOTE: All notifications are persistent. They will appear for the life of the event and if dismissed will return. This can be changed under
Settings -> Nofications -> Alert only once
If sound is configured a sound will be given only when the notification first appears. For weather alerts, zone based alerts instead of polygon
are used for now.

OPENING SCREEN NOTE: Everything on the opening screen is clickable. Clicking on most text will expand it or take you somewhere else.
Current conditions at top starts with temp ( heat index or wind chill if present ) / dew point ( relative humidity ).

SPC MESO,  US Alerts, and MODELS NOTE: Each activity uses a "navigation drawer" so swipe from the left border to the center of the
screen to access the drawer and to change model parameters ( or location ). Presence of the nav drawer is indicated by a different
ICON in the upper left corner ( not the arrow ).

STORM REPORTS NOTE: Click on an individual storm report to go to a map of roughly the location of the storm report.
Click on the image to select a different date. Long press on a storm report for a best attempt display of radar from that time/location
thanks to Iowa Mesonet archives.

NEXRAD NOTE: Items in the actionbar menu such as radar mosaic or vis/ir/wv can be selected multiple times to zoom out from 1km,2km,sector to US.
If using the map to select a radar site please note that it does require accuracy and you must get as close to the small circle as possible on touch.

LOCATION SETTINGS NOTE: To add a new location you can enter an address on the top text line and then click on the search icon.
If you get valid lat/lon back ( can verify with map button ) you can click the icon to the right of the map icon to save.
You can also use the rightmost location icon to use your current location for lat/lon.
Upon save a "toast" notification should display and you should see the NWS office and zone used for your location.
Notifications will not work for a location if the zone is not properly discovered and listed under Settings -> Locations.
For some coastal locations the lat/lon search will actually resolve to lat/lon that NWS views as a marine zone.
In this case adjust your lat/lon to get off the water ( use map icon to confirm ) - Tampa Bay is a good example of this.

LONG PRESS ( contextual menu ) NOTE:  You can also long press on the image in the mesoscale discussion activity to set a location to one
 of the NWS offices in that area. In US warnings you can long press on a specific alert to access a variety of choices.
The status area on the main screen showing the location and update time for current conditions and be long pressed to edit the location and also
clicked on to reload conditions/forecast/radar.

Settings are documented in the file "SETTINGS".

Requirements: Android 4.2 or higher with 1GB of memory at a minimum ( recommended ). ARM 32bit based hardware ( Intel, etc has not been tested )

Access required: App needs access to Your Location, Network Communication,  Systems tools ( run at startup for alerts ).

Originally Released (3.0) Spring 2014 under the GNU GPL3 license.
