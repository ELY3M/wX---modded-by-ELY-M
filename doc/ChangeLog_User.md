
# wX ChangeLog

Service outages with NWS data will be posted in the [FAQ](https://gitlab.com/joshua.tee/wxl23/-/tree/master/doc/FAQ.md) as I become aware of them.
FAQ can be accessed via Settings -> About

Please also review [Upcoming changes](https://gitlab.com/joshua.tee/wxl23/-/blob/master/doc/UPCOMING_CHANGES.md) impacting all or some users.

## 55XXX 2022_0X_XX (BETA)
This beta is the 2nd beta release that has been impacted by a significant re-write (ie refactor) of many parts of the program.
This was necessary to make the code base easier to understand/maintain and be more consistent with newer versions such as the
desktop versions. As a result, in some areas the program should appear faster and be efficient with respect data data download.
This especially holds true with Nexrad radar and the "Severe Dashboard". In addition, limitations present for many years have been
fixed. As with any major work, new bugs might have been introduced.
Please email me if something seems not right.
-- updates through 55674:
* [ADD] Nexrad multipane, if you tap on the text in the upper left that lists radar site/product it will take you to severe dashboard (similar to single pane)
* [ADD] Nexrad multipane, if warnings(tor/ffw/tst) enabled, show in top toolbar similar to single pane
* [FIX] tap on any NHC notification would cause a crash (user reported)
* [FIX] NHC graphics for ATL storms were not working
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

## 55655 2022_08_11 (BETA)
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

## 55627 2022_07_20 (BETA)
* [FIX] Nexrad: if non-default option to use hi-res State lines data, not all lines were showing

## 55625 2022_07_20 (BETA)
* [ADD] In GOES, AWC, and NWS radar mosaic if you touch blank space in the top toolbar or the label to the left it will open the navigation drawer
* [FIX] remove deprecated option in settings->widgets (show warnings in radar mosaic)
* [ADD] minor alphabetization in settings->widgets
* [FIX] status bar icon for location specific radar notification
* [REF] remove animated gif share which is not working and is considered deprecated within the program
* [ADD] Nexrad radar long press - shorten verbiage
* [FIX] new NWS Radar Mosaic - remove menu with products that aren't available
* [FIX] NHC now shows complete summary information and text products for Central Pacific "CP" hurricanes
* [ADD] Radar settings - mostly alphabetized now with common options listed at top

## 55615 2022_07_16 (BETA)
* [REF] Refactor some parts of the code base to make it more maintainable - you should not notice anything
* [ADD] (main screen tabs) Switch to ViewPager2 and use FragmentStateAdapter since Google deprecated ViewPager - you should not notice anything
* [ADD] Option to use new NWS Radar Mosaic (restart required). This is a temporary option as it appears likely the default mosaic will switch to NWS (away from AWC) later this year. Note the behavior may change slightly as the feature is refined
* [REF] Change Level2 data site back to nomads after maintenance in Apr 2022
* [REF] (remove unused assets to slightly reduce size of app) remove MyApplication.radarHwEnh, git rm ./app/src/main/res/raw/hw
* [ADD] NWS Radar Mosaic - play icon toggles to stop and add ability to stop animation via button
* [ADD] Nexrad radar long press - shorten verbiage
* [FIX] partial - For NHC central pacific storms (images show but not text product - work in progress)

## 55607 2022_07_09 (BETA)
* [FIX] NHC notifications will now alert for ATL or EPAC depending on which is selected
* [ADD] In the US Alert viewer, show warning types and states in sorted manner
* [ADD] Settings -> UI alphabetize
* [ADD] refine labels in Settings -> UI
* [ADD] remove option "Reduce size of tile images" TILE_IMAGE_DOWNSIZE
* [ADD] remove option "Show VR button on main screen" VR_BUTTON
* [ADD] Target the latest version of Android (this is periodically required to be compliant with Google Play Store)
* [ADD] Upgrade software component required by wX (okhttp)

## 55603 2022_06_29 (BETA)
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

