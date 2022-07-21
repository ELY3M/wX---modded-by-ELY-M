
# wX ChangeLog

Service outages with NWS data will be posted in the [FAQ](https://gitlab.com/joshua.tee/wxl23/-/tree/master/doc/FAQ.md) as I become aware of them.
FAQ can be accessed via Settings -> About

Please also review [Upcoming changes](https://gitlab.com/joshua.tee/wxl23/-/blob/master/doc/UPCOMING_CHANGES.md) impacting all or some users.


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

