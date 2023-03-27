
# wX ChangeLog for users

Service outages with NWS data will be posted in the [FAQ](https://gitlab.com/joshua.tee/wxl23/-/tree/master/doc/FAQ.md) as I become aware of them.
FAQ can be accessed via Settings -> About

Please also review [Upcoming changes](https://gitlab.com/joshua.tee/wxl23/-/blob/master/doc/UPCOMING_CHANGES.md) impacting all or some users.

## 55787 (BETA) 2023_03_25
* [FIX] NWS SPC has changed the URL/format type for SPC MCD and thus code updates were required
* [FIX] SPC Meso Violent Tornado Parameter (VTP) was not working as SPC changed the product ID
* [ADD] Widget with weather conditions will fall back to 2nd closest observation point if primary obs point is not updating in the past 2 hours (similar to main screen / notification)
* [FIX] NWS Has removed static graphic for space weather: Estimated Planetary K index
  and replaced with a web accessible version for this product at https://www.swpc.noaa.gov/products/planetary-k-index
  if you use this data you could access via a browser, etc

## 55782 (BETA) 2023_01_23
* [FIX] SPC Storm reports: minor change in date picker accent color used in darker themes
* [FIX] crash in RTMA when network is down
* [ADD] NCEP Models (MISC Tab, upper left) has replaced model "ESTOFS" with "STOFS" (v1.1.1) per https://mag.ncep.noaa.gov/version_updates.php

## 55780 (BETA) 2023_01_16
* [ADD] Add more verbiage to tab label edit area in Settings -> UI
* [ADD] Tap on label "Tab 1 Label" (and 2,3) in Settings -> UI now shows popup with brief description.
* [ADD] In the location editor change the label from "Conditions" to "Current Conditions" to add clarity on what this notifications for.
  As an FYI, you can tap on the textual label for all notifications for a greater description.
* [ADD] In the location editor change the label from "Radar" to "Radar image with alert" to add clarity on what this notifications for.
* [ADD] In the location editor change the label from "Sound" to "Play sound for alert notification" to add clarity on what this notifications for.
* [ADD] Weather conditions notifications will fall back to 2nd closest observation point if primary obs point is not updating in the past 2 hours (similar to main screen)
* [FIX] date picker in SPC Storm reports had incorrect color when using dark themes
* [FIX] Settings -> HomeScreen , if one taps top back arrow after making changes it now restarts app properly similar to tapping bottom arrow

## 55758 2023_01_04
* [ADD] Text widgets AFD/HWO/National Text now match formatting as seen within the main app
* [FIX] SPC Storm report share was not including full detail
* [FIX] Settings -> Playlist: buttons were not aligned correctly
* [FIX] Nexrad - crash if enabling a geometry such as county lines
* [REF] DownloadText - remove QPFPFD, product is no longer offered and was removed from WpcText some time ago
* [FIX] Local text product viewer navigation drawer background color was not consistent with rest of app
* [ADD] RTMA_TEMP (Real-Time Mesoscale Analysis) as a Homescreen option in the generic images menu

        more details on the product: [https://nws.weather.gov/products/viewItem.php?selrow=539](https://nws.weather.gov/products/viewItem.php?selrow=539)

        graphics are from here: [https://mag.ncep.noaa.gov/observation-type-area.php](https://mag.ncep.noaa.gov/observation-type-area.php)
* [ADD] RTMA accessible via Observations (main submenu)
* [FIX] TVS was not working
* [ADD] Hourly using old API no longer shows the date, just weekday/hour similar to Hourly with new API (uses ObjectDateTime.translateTimeForHourly)
* [FIX] ESRL RAP model was not working correctly
* [FIX] NSSL WRF (and other NSSL models) run only once per day, not twice
* [ADD] NCEP HREF now goes out to 48 hours (was 36)
* [ADD] color and spacing changes for navigation drawer in SPC Meso, SPC SREF, WPC Images, National Text
* [ADD] Dawn/Dusk to sunrise card on main screen
* [ADD] SPC HREF now has image layers stored in the application bundle so response should be slightly better with less data downloaded.
* [ADD] bring back SPC HREF "Reflectivity: stamps" including proper background
* [ADD] remove option in Settings->UI, "GOES GLM for lightning (requires restart)". Lightning Maps is being phased out and is no longer an option.
* [ADD] back SPC HREF reflectivity under heading "member viewer" including an additional layer that shows the colormap and time
* [FIX] from the national text product viewer remove the following which retired some time ago: "pmdsa: South American Synoptic Discussion"
* [ADD] SPC Fire Sum, SPC Swo Sum/Day, NHC, and WPC Rainfall Sum - avoid unnecessary downloads when switching back from other apps or power on/off
* [FIX] NWS radar mosaic sector was not being properly detected for locations in scope of sector SOUTHMISSVLY

## 55721 2022_10_01
This releases has been impacted by a significant re-write (ie refactor) of many parts of the program.
This was necessary to make the code base easier to understand/maintain and be more consistent with newer versions such as the
desktop versions. As a result, in some areas the program should appear faster and be efficient with respect data data download.
This especially holds true with Nexrad radar and the "Severe Dashboard". In addition, limitations present for many years have been
fixed. As with any major work, new bugs might have been introduced.
Please email me if something seems not right.

For users who have upgraded to Android 13 (API33) or have a fresh install of wX onto a device with Android 13 you'll need to understand
some potential changes. First, if your widgets stop working please just remove and re-add them. I have researched, not sure why that would be
needed. Second, despite what I have read about permission to send notifications being carried over, if you are using notifications you should make sure
wX is allowed to send notifications either via "App Info" in Android or just navigating to Settings->Notifications. Upon entry to that screen if
Notifications are not allowed you can allow them. As mentioned in the "upcoming changes document" for various reasons widgets and notifications
are at the bottom of the "service priority" list.

Further, the settings "Prevent accidental exit" (where you need to hit the back button twice from the main screen) in Settings->UI will be retired
at some point next year due to Android's long term strategy for the back button outline [here](https://developer.android.com/guide/navigation/predictive-back-gesture)

* [FIX] NHCStorm images 3,4 don't work in ImageViewer (filter causes some images to be skipped)
* [FIX] In the main nexrad viewer (lightning icon), if you access settings->radar->colors and change the nexrad background the change will be immediate
* [FIX] In multipane radar if animate and then long press to switch radar site, animation keeps playing old site after switch
* [FIX] Hourly will remain a small text size regardless of text size chosen in settings->UI (which can lead to an unusable display of text if too large)
* [FIX] attempted fix for quad pane nexrad frame sizing issue
* [ADD] Settings -> Playlist, if user adds a product that is already there, have the popup notification close after 3 sec instead of requiring user to close
* [ADD] Add location screen, save status popup now closes automatically after 3 seconds
* [ADD] bring back prior back button handling in web based activities
* [FIX] if multiple nexrad on homescreen in some situations radar label in top current conditions card was not fully correct
* [FIX] nexrad - not all storm tracks were being drawn if enabled
* [FIX] model animations now start from current time position (not start) till end
* [FIX] seven day notification and legacy cc/7day widget has errant newlines in 7 day listing
* [FIX] legacy widget timestamp had dark text making it mostly invisible
* [FIX] legacy widget icons are not the same color (white vs off-white)
* [FIX] update NWS Radar Mosaic URL in response to [PNS22-09](https://www.weather.gov/media/notification/pdf2/pns22-09_ridge_ii_public_local_standard_radar_pages_aaa.pdf)
* [ADD] The location based "7day" notification will now open a normal forecast view instead of a plain text 7 day listing
* [FIX] SPC Meso multipane not saving x/y/zoom on exit
* [FIX] nexrad invoked from alert will not keep site when jump to multipane
* [FIX] SPC Compmap navigation drawer on/off for each parameter was not showing correctly
* [FIX] SPC Meso - layers topo and county were not working together
* [ADD] SPC Meso - add more layers: Observations/Population (Topo and Population can't be shown together)
* [ADD] Settings -> UI "Navigation Drawer Configuration" will show regardless of whether or not drawer is enabled
* [ADD] Settings->Main, update Notifications label if App not allowed to send
* [FIX] SPC convective outlook notif icon was displaying as all black
* [FIX] SPC MPD / Watch notification action was not working
* [ADD] Add Location - add label that if notifications are blocked will be displayed. If on Android 13 you can tap on it to open the perm dialogue
* [FIX] Nexrad radar - if map is shown and then app is switched or screen is turned off, when returning to nexrad, hide the map
* [ADD] If running Android 13, screen recording is regrettably no longer working. Updated Nexrad to only show drawing tools. Updated all other image based activities
  to offer normal image sharing. Updated FAQ and Upcoming Changes
* [FIX] In Settings->Homescreen remove the "Web" option from the menu. This is meant as a workaround when NWS 7 day is not working. However, the web widget appears to no longer be working either.
* [ADD] [Target Android 13](https://developer.android.com/google/play/requirements/target-sdk)
* [ADD] recent changes in how polygon data is being downloaded and cached no longer require the background job to fetch it
* [ADD] DownloadTimer set warnings to 3 min instead of based off a variable that doesn't necessarily relate to it
* [ADD] In activity to add or edit location, place cursor at end of text in label field
* [ADD] Settings -> location add # of locations in title
* [FIX] settings -> location truncates actual stored lat / lon too much - add a few more characters
* [ADD] Settings->Radar has been split it two activities. The line/marker (tvs/hail) size settings can now be accessed
  via a second activity accessible from the "Line / Marker sizes" button 3rd from the top
* [FIX] SPC SREF - when using left/right button to move through model images, image was download twice instead of just once
* [ADD] In US Alerts submenu, show Alaska instead of AK (and same for HI)
* [FIX] In nexrad widget, draw county line below state-line
* [ADD] long press radar status message shows nearest radar to where long press occurred - not radar currently selected
* [FIX] handle RSM (radar status message) better for terminal radars
* [FIX] SPC SWO Day X State graphic - don't like AK/HI in menu as SPC only covers CONUS
* [FIX] Revert this change as it breaks the radar on the main screen when "center on location" is enabled: location dot on main screen will continue to be dot with circle around it (for now)
* [ADD] Nexrad radar widget now respects geographic line size setting as used for the main program Nexrad
* [ADD] Nexrad multipane, if you tap on the text in the upper left that lists radar site/product it will take you to severe dashboard (similar to single pane)
* [ADD] Nexrad multipane, if warnings(tor/ffw/tst) enabled, show in top toolbar similar to single pane
* [FIX] tap on any NHC notification would cause a crash (user reported)
* [FIX] NHC graphics for ATL storms were not working
* [ADD] Nexrad long press - shorten (since large text goes off screen) and make verbiage consistent with iOS port wXL23
* [FIX] adhoc location forecast activity (via nexrad long-press) had to many digits for lat/lon
* [FIX] In single pane nexrad, if animating, switching radars via the map was not working
* [FIX] nexrad help in submenu needed to be freshened up a bit
* [FIX] nexrad autorefresh fix related to onresume/pause or restart
* [ADD] nexrad multipane "ctrl-a" will now toggle an animation on/off (similar to how single pane has been)
* [ADD] nexrad multipane "ctrl-r" (ref) and "ctrl-v" (vel) added (similar to how single pane has been) (use alt-? on main screen and nexrad to show shortcuts)
* [FIX] The main screen will no longer show the primary "location dot" surrounded by a circle as this shape is reserved from the dedicated
    nexrad viewer which if configured does use active GPS location
* [ADD] severe dashboard - warnings/watch/mcd/mpd are downloaded in parallel
* [ADD] to meet the Oct 2022 announcement - raise minSDK to Android 6 API 23 from API 21
* [FIX] Day 8-14 Hazard Outlook is to large to display via default options
* [FIX] in Color Palette Editor remove the unimplemented "help" option
* [FIX] in Color Palette Editor remove the "load from file" option - please use copy/paste instead. It is not worth the complexity to leave this in place
* [ADD] in settings->radar WPC Fronts line size is now a configurable item
* [ADD] Us Alerts - if in landscape do 2 columns
* [ADD] Nexrad radar has had a large chunk of it re-written to modernize the code and to help improve performance
*       by doing more work in parallel. Please do email me if any bugs are encountered.
* [ADD] For new NWS radar mosaic, tile icon in MISC tab will show the last viewed location. Main submenu will continue to show nearest image.
* [ADD] in Nexrad single pane some settings changes that previously required restarting the nexrad view no longer require (like adding CA/MX borders)
* [FIX] wpc mpd as viewed in playlist has HTML tags showing (changed WpcTextProductsActivity.kt)
* [ADD] Nexrad on main screen and multipane nexrad now show SPC Convective Outlooks if configured to do so
* [FIX] Nexrad long press observation dialogue was not respecting the text size setting
* [FIX] nexrad - long press and select the "Beam Height" entry changed the radar site, it should do nothing
* [FIX] MCD/MPD/Watch viewer won't crash if text product is not yet available
* [ADD] Nexrad radar option "Show radar during a pan/drag motion" which is enabled by default to mimic historic behavior
* [FIX] SPC SREF - remove help from submenu as it doesn't add value
* [FIX] SPC SREF - correct formatting for status of current model run in submenu
* [ADD] Multi-pane nexrad now shows WPC Fronts if configured
* [ADD] Spc Storm Reports drawer with states used to filter is now sorted
* [FIX] if user chose to not use main screen radar floating action button and also chose to use a navigation drawer, the radar FAB would not show
* [FIX] adhoc forecast via long press in nexrad was not showing correct sunrise/sunset data
* [FIX] If audio was previously paused, going into another activity with text to speech controls shows text "stop button" instead of icon
* [ADD] Material3 "You" partial implementation for all themes
* [FIX] TouchImageView2.java, add try/catch in onDraw to handle images that are to large. Show nothing but don't crash
* [FIX] WPC Rainfall discussion, hide radar icon as it serves no purpose in this activity
* [FIX] ObjectCurrentConditions.kt - if the heat index and the temp (both rounded) are equal, don't show heat index
* [ADD] SPC MCD/Watch summary icons in SPC Tab now show only images regardless of how many Watch or MCD there are. This resolves one bug and makes the interface more predictable. Please note that the "Severe Dashboard" accessible on the main screen via octagon icon is the preferred method to see MCD/Watch. When the Navigation drawer is configured as compared to Tabs they are not even visible.
* [ADD] SPC MCD/Watch summary - if no watch or mcd is present use a bigger font with a "high light" color
* [ADD] In severe dashboard if no warnings/watch/mcd/mpd are present show "None" in subtitle
* [ADD] In Settings -> Homescreen the menu option "Set to default (Canada)" is being removed in preparation for CA content removal by end of next year.
* [ADD] Settings -> celsius to fahrenheit table was rounding to integer but still showing with ".0" at the end - remove
* [ADD] new GOES sector: South Pacific
* [ADD] new GOES sector: Northern Atlantic
* [ADD] GOES activity - in Pac/Atl submenu - alphabetize entries
* [ADD] GOES activity - in Regional Views - alphabetize entries
* [ADD] GOES activity - add new submenu "North/South America" as "Regional Views" had too many entries
* [FIX] The following model is being removed from the program due to it's experimental nature and numerous breaking changes over the years:
        **it was accessible only via the NHC activity**: Great Lakes Coastal Forecasting System, GLCFS
        You can access it via a web browser here: https://www.glerl.noaa.gov/res/glcfs/
        As a reminder the best model interface in terms of stability continues to be MAG NCEP (MISC Tab - upper left)
        I believe all other models with interfaces provided are not considered true production services, please contact me if I am wrong

* [ADD] Vis/SPC Meso/Radar Mosaic now have a play/stop/pause bottom for animations that is more appropriate to the circumstance and similar to Nexrad
* [ADD] In GOES/SPC Meso animate icon now uses the number of frames (default 10 - can be change in settings->UI), similar to nexrad
        a submenu in the main menu will allow access to the other frame count choices
* [ADD] If device is in landscape some activities will show 3 images across instead of just 2 (SPC Convective outlook, fire, wpc rainfall, etc)
* [ADD] SpcStormReportsActivity - add subtitle indicating you can tap the image to open a date picker
* [FIX] 7day not honoring C/F setting
* [FIX] slowness in LocationEdit search icon appearing by loading cities before the activity is displayed
* [FIX] SPC HREF - some images not working, more work is needed (non-production model service)
* [ADD] Settings->Notification: alphabetize with logical groups
* [ADD] Settings: alphabetize
* [FIX] In usalerts for sps (special weather statement) and other alerts - don't show radar button if no polygon data is present
* [FIX] Nexrad: if non-default option to use hi-res State lines data, not all lines were showing
* [ADD] In GOES, AWC, and NWS radar mosaic if you touch blank space in the top toolbar or the label to the left it will open the navigation drawer
* [FIX] remove deprecated option in settings->widgets (show warnings in radar mosaic)
* [ADD] minor alphabetization in settings->widgets
* [FIX] status bar icon for location specific radar notification
* [REF] remove animated gif share which is not working and is considered deprecated within the program
* [ADD] Nexrad radar long press - shorten verbiage
* [FIX] new NWS Radar Mosaic - remove menu with products that aren't available
* [FIX] NHC now shows complete summary information and text products for Central Pacific "CP" hurricanes
* [ADD] Radar settings - mostly alphabetized now with common options listed at top
* [REF] Refactor some parts of the code base to make it more maintainable - you should not notice anything
* [ADD] (main screen tabs) Switch to ViewPager2 and use FragmentStateAdapter since Google deprecated ViewPager - you should not notice anything
* [ADD] Option to use new NWS Radar Mosaic (restart required). This is a temporary option as it appears likely the default mosaic will switch to NWS (away from AWC) later this year. Note the behavior may change slightly as the feature is refined
* [REF] Change Level2 data site back to nomads after maintenance in Apr 2022
* [REF] (remove unused assets to slightly reduce size of app) remove MyApplication.radarHwEnh, git rm ./app/src/main/res/raw/hw
* [ADD] NWS Radar Mosaic - play icon toggles to stop and add ability to stop animation via button
* [ADD] Nexrad radar long press - shorten verbiage
* [FIX] partial - For NHC central pacific storms (images show but not text product - work in progress)
* [FIX] NHC notifications will now alert for ATL or EPAC depending on which is selected
* [ADD] In the US Alert viewer, show warning types and states in sorted manner
* [ADD] Settings -> UI alphabetize
* [ADD] refine labels in Settings -> UI
* [ADD] remove option "Reduce size of tile images" TILE_IMAGE_DOWNSIZE
* [ADD] remove option "Show VR button on main screen" VR_BUTTON
* [ADD] Target the latest version of Android (this is periodically required to be compliant with Google Play Store)
* [ADD] Upgrade software component required by wX (okhttp)
* [FIX] terminal radar TICH (Wichita, KS) was not properly coded (was TICT), it is now available for use
* [FIX] prevent nexrad radar from showing stale SWO data (earlier fix was not good)
* [ADD] SPC Meso additions
  - under Thermodynamics add "Skew-T Maps" skewt
  - under "Winter Weather" add "Winter Skew-T Maps" skewt-winter

## 55599 2022_04_16
* [FIX] more robustness for Hourly using old NWS API
* [ADD] Please see URL above for a new page about upcoming changes to certain parts of wX
* [ADD] Nexrad Level2: in response to 24+ hr IT maintenance on 2022-04-19 to nomads NWS servers, change URL to backup
  - [https://www.weather.gov/media/notification/pdf2/scn22-35_nomads_outage_apr_aaa.pdf](https://www.weather.gov/media/notification/pdf2/scn22-35_nomads_outage_apr_aaa.pdf)
  - A [reminder](https://gitlab.com/joshua.tee/wxl23/-/blob/master/doc/FAQ.md#why-is-level-2-radar-not-the-default) on Level 2 support within wX
* [ADD] SPC Meso - new "layer" county boundaries. Turn on from submenu -> Layers (NOTE: the state line boundary provided for the Radar Mosaic does not quite match up with the county layer. This can be observed from the SPC Website as well)
* [ADD] SPC Meso - new param: "Hodograph Map", access from "Wind Shear" submenu. Only usable on region specific sectors, not CONUS.
* [ADD] The default hourly data provided (the new NWS API) does not reliably return results on the first download, added a retry mechanism.
* [ADD] access to settings via hourly in case app won't load (can access via 4x4 blue widget -> hourly -> settings)
* NWS is deprecating this website on 3/22 (substitute NWS observation point, etc): https://www.wrh.noaa.gov/mesowest/timeseries.php?sid=KCAR
  in favor of: https://www.weather.gov/wrh/timeseries?site=KCAR
  Thus updated the activity accessed through the "MISC" tab to reflect this
* [FIX] GOES GLM (lightning) animation was not working
* [ADD] (main screen) if closest observation point has data older then 2 hours, show data for the 2nd closest obs point instead
* [FIX] prune the list of Observation points by removing 57 sites that had not updated in the past 28 days
        This pruning will occur more frequently in the future to avoid a bad user experience
* [ADD] upgrade software that wX uses
* [FIX] remove the following weather obs point in `stations_us4.txt` and `us_metar3.txt` as user reported it has not updated since Jan 26
```
IL,ROMEOVILLE/CHI  ,KLOT
KLOT 41.6 -88.1
```

### version **55584** - released on 2022/02/10
* [ADD] In Settings -> UI -> Navdrawer config, have top arrow respond in the same way that bottom arrow does when pressed
* [ADD] In Settings -> about, add navdrawer token string to assist in troubleshooting
* [FIX] remove observation point KSTF (Starkville, MS) as it's impacting users.
* [FIX] remove decomissioned text products
  - "mimpac: Marine Weather disc for N PAC Ocean"
  - "mimatn: Marine disc for N Atlantic Ocean"
* [ADD] SPC Meso in "Multi-Parameter Fields" add "Bulk Shear - Sfc-3km / Sfc-3km MLCAPE"
* [FIX] SPC Meso in "Upper Air" change ordering for "Sfc Frontogenesis" to match SPC website
* [FIX] Creating desktop shortcuts was not working on Android version 12
* [ADD] CONUS sector to NAM in NCEP Models
* [ADD] switch to non-experimental WPC winter weather forecasts day 4-7
* [ADD] in nexrad long press show how far away nearest observation point is
* [ADD] National Images - add "_conus" to end of filename for SNOW/ICE Day1-3 for better graphic
* [ADD] SPC HRRR - add back SCP/STP param
* [FIX] GOES Viewer, eep Eastern East Pacific image was not working after NOAA changed image resolution
* [FIX] remove external links in Color Palette editor
* [FIX] remove unprintable chars in nexrad county labels

### version **55570** - released on 2021/10/22
* [FIX] NWS html format changed caused 7 day forecast icons to break
* [ADD] Additional GOES products FireTemperature, Dust, GLM
* [ADD] Move ChangeLog and FAQ to Gitlab
* [ADD] Settings -> About, open browser when viewing FAQ or ChangeLog
* [FIX] icon size on main window navigation drawer if using that option
* [FIX] remove CLI "Daily Climate Report" from Local text products since NWS no longer provides WFO to site mapping
* [ADD] per https://developer.android.com/about/versions/12/approximate-location
if you request ACCESS_FINE_LOCATION you must also request ACCESS_COARSE_LOCATION
* [REF] LsrByWfoActivity refactor to use Future* and parallel download threads
* [FIX] add to UtilityMetarConditions "Patches Of Fog"
* [REF] Numerous changes to make the source code base easier to support in the long run and changes required by google to keep the app active on the playstore (ie target a new version of Android, etc)

