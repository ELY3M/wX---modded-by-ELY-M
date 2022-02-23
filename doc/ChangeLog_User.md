
# wX ChangeLog

Service outages with NWS data will be posted in the [FAQ](https://gitlab.com/joshua.tee/wxl23/-/tree/master/doc/FAQ.md) as I become aware of them.
FAQ can be accessed via Settings -> About

## 55588 2021_02_22 (BETA)
* [FIX] remove the following weather obs point in `stations_us4.txt` and `us_metar3.txt` as user reported it has not updated since Jan 26
```
IL,ROMEOVILLE/CHI  ,KLOT
KLOT 41.6 -88.1
```
NOTE: I'm going to audit the 2400+ obs sites and remove stale entries for the next planned release.
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

