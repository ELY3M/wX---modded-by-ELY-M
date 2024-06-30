# wX ChangeLog for users

Service outages with NWS data will be posted in
the [FAQ](https://gitlab.com/joshua.tee/wxl23/-/tree/master/doc/FAQ.md) as I become aware of them.
FAQ can be accessed via Settings -> About

Please also
review [Upcoming changes](https://gitlab.com/joshua.tee/wxl23/-/blob/master/doc/UPCOMING_CHANGES.md)
impacting all or some users.

## 55897 2024_06_24 (BETA)

* [FIX] icons for NWS API were broken due to NWS issue, add workaround

## 55896 2024_06_16 (BETA)

* [ADD] using the new NWS API for forecast data is now the default since NWS is deprecating the
  older format
* [ADD] 2nd retry for forecast download with new API
* [ADD] add 1 sec sleep in 2nd retry for hourly download with new API
* [REF] URL Fix in response to **SCN24-19: The National Centers for Environmental Prediction (NCEP)
  Climate
  Prediction Center (CPC) will Change the Depiction and Output Formats of Several Ultraviolet
  Index (UVI) Product Graphics on or about May 25, 2024**
* [FIX] one crash report

## 55890 2024_05_05 (BETA)

* [FIX] 2 crashes only seen in Pre-launch report: UtilityTts.kt and UtilityUI.kt

## 55888 2024_05_03 (BETA)

* [FIX] National Text now shows play/pause button similar to other text viewing activities
* [ADD] per user request add the following to notification filters

```
"Coastal Flood Warning",
"Coastal Flood Watch",
"Coastal Flood Advisory"
```

* [ADD] In support of **SCN24-02: New Forecast Product “Offshore Waters Forecast for SW N Atlantic
  Ocean”
  Will Start on March 26, 2024**
  add `offnt5` and rename title for `offnt3`. These products are accessed via "National Text"
  activity.
* [FIX] 2 crashes seen via crash reporter
* [REF] simplify code in nexrad layout and text to speech

## 55877 2024_04_03

* [ADD] add support for Nexrad "KHDC" for Service Change Notice 24-11 Hammond, LA WSR-88D (KHDC)
  to Begin NEXRAD Level III Product Dissemination on or around March 31, 2024.
* [ADD] per upcoming changes: wX Android screen recording (**but not** associated drawing tools)
  will not be available. Please use native screen recording and screen shot
  capabilities instead.
  This existing functionality does not fall within Google's accepted "Foreground Service Type" once
  the app targets API34 (Android 14) which is required by sometime later in 2024.
* [ADD] Target the latest version of Android (API 34, this is periodically required to be compliant
  with Google Play Store)
* [ADD] NCEP MAG updates [MAG 5.0 - March 2024](https://mag.ncep.noaa.gov/version_updates.php)

```
Added the following new domain to Global Forecast System (GFS):
  Pacific (PAC-REGION)
Renamed the following products to HREF:
  pmm_refd_1km → pmm_refd_1km_emsl
  pmm_refd_max → pmm_refd_max_emsl

 Added the following products to HREF:
    Ensemble Agreement Scale probability of 0.01” rain in 1 hour (eas_prob_1h_rain_0.01in)
    Ensemble Agreement Scale probability of 0.25” rain in 1 hour (eas_prob_1h_rain_0.25in)
    Ensemble Agreement Scale probability of 0.50” rain in 1 hour (eas_prob_1h_rain_0.50in)
    Ensemble Agreement Scale probability of 0.01” rain in 3 hours (eas_prob_3h_rain_0.01in)
    Ensemble Agreement Scale probability of 0.25” rain in 3 hours (eas_prob_3h_rain_0.25in)
    Ensemble Agreement Scale probability of 0.50” rain in 3 hours (eas_prob_3h_rain_0.50in)
    Ensemble Agreement Scale probability of 0.1” snow in 1 hour (eas_prob_1h_snow_0.1in)
    Ensemble Agreement Scale probability of 0.3” snow in 1 hour (eas_prob_1h_snow_0.3in)
    Ensemble Agreement Scale probability of 0.1” snow in 3 hours (eas_prob_3h_snow_0.1in)
    Ensemble Agreement Scale probability of 0.3” snow in 3 hours (eas_prob_1h_snow_0.1in)
    Localized probability matched mean mean precip 1 hour plot (lpmm_mean_precip_p01)
    Localized probability matched mean mean precip 3 hour plot (lpmm_mean_precip_p03)
    Localized probability matched mean mean precip total hour plot (lpmm_mean_precip_ptot)
    prob_lowIFR_IFR
    pmm_refd_1km
    pmm_refd_max

Added the following products to NAEFS:
    10th_percentile_10m_wnd
    50th_percentile_10m_wnd
    90th_percentile_10m_wnd
    extreme_index_10m_wnd
    10th_percentile_2m_temp
    50th_percentile_2m_temp
    90th_percentile_2m_temp
    extreme_index_2m_temp
    extreme_index_mslp
```

## 55873 2024_03_20

* [FIX] update okhttp library to newest alpha version to fix ipv6 issue seen with GOES NESDIS site (
  thanks to "ski warz" for the suggestion)
* [FIX] "Aviation only AFD" was not working (thanks to "ski warz" for noticing and providing fix)

## 55869 2024_02_13

* [FIX] WPC Rainfall Outlook Day 1 - 5, Title was truncated, move parts of Title to Sub-title
* [ADD] Previously, the graph in the hourly activity was set to a fixed size height.
  On laptops (ie ChromeOS) this led to the graph being very narrow. The graph is now set to
  slightly less then the width and the height is a fixed fraction of that width.
* [ADD] ChromeOS keyboard shortcut for main screen "Ctrl - s" (SPC SWO Summary)
* [ADD] ChromeOS keyboard shortcut for main screen "Ctrl - g" (Rainfall Outlook Summary)
* [FIX] In GOES, move share icon to submenu as when animating pause icon was not showing on some
  devices
* [FIX] In GOES, Animate button was not working as intended. if tap Stop once animating, it should
  show latest image
* [FIX] SPC SREF - time menu was not showing up in bottom toolbar
* [FIX] SPC HREF - when change sector, reset zoom level on image(s)
* [FIX] SPC HREF - remove first time "00" in series which is not valid for this model
* [FIX] NCEP MAG Models - when change sector, reset zoom level on image(s)
* [ADD] change keyboard shortcuts (ie for ChromeOS) to more closely match desktop ports, SPC Meso (
  cltr-Z), Nat Text (cltr-T), Settings (cltr-P)
* [FIX] Orange theme still had blue FAB
* [ADD] Main screen: make location label and hazards bold
* [FIX] Nexrad radar city textual labels and observation labels were not working well on ChromeOS,
  change the way size is computed.
* [FIX] Rainfall Outlook Summary - swap title and subtitle as title was too long
* [ADD] as mentioned in "Upcoming Changes" since Aug 2022:
  The option **Icons evenly spaced** will be removed in **Settings->UI**.
  This was meant to be a bridge from Android 4.4 to Android 5.0 back in Fall 2014.
  It goes against modern Android design and has caused issues in the past for users who have
  unknowingly enabled it.
* [ADD] SPC SWO Summary - similar to Severe Dashboard, move "pin to homescreen" icon to submenu so
  it's clear what this is doing

## 55855 2023_12_08

* [FIX] KLIX (LA, New Orleans) nexrad radar is being physically moved.
  This update prevents it from being used as an active radar in long press radar
  selection or if adding a new location.
* [FIX] WPC US Hazards Outlook Days 3-7: product discontinued via SCN23-101: Termination of the
  Weather Prediction Center Day 3-7
  Hazards Outlook Discussion Effective November 15, 2023
* [ADD] NHC storm detail - force to mph for wind intensity and add "mph" label
* [ADD] Nexrad radar widget - load geometry data locally in an attempt to make widget more reliable
* [FIX] remove 3 obsolete Canadian text products in the National Text Viewer (MISC Tab, 2nd row,
  left)
* [FIX] NWS changed some of the URLs for the SST images accessed in the NHC activity
* [FIX] Window sizing for dual/quad pane has been problematic across various devices, Android OS
  versions, and setting combinations. This release attempts to simplify and fix this issue. If you
  still experience dual/quad pane window sizing issues please email me with screenshot, device type,
  Android OS version, etc. thanks
* [REF] Deprecated Media notification controller has been removed. It is unlikely most users were
  even aware of this feature related to text to speech.
* [REF] Also communicated in "Upcoming changes", after support for Android 7.1 was removed, the
  option
  Settings->Radar->Launch app directly to radar has been removed since Android 8.0 and higher
  supports static pinned launchers
  i.e. if you long press on the app icon in the android home screen you can launch the radar
  directly and also setup another icon to do so.

## 55836 2023_10_17

* [FIX] Nexrad windbarbs and observations in response to NWS planned AWC website
  upgrade: https://aviationweather.gov/
* Add the following in the national image viewer (MISC Tab, 3nd Row, Right)

```
   "WPC Day 4 Excessive Rainfall Outlook" (Under QPF)
   "WPC Day 5 Excessive Rainfall Outlook" (Under QPF)
   "Space Weather Overview" (Under Space Weather)
```

* Remove the following text products as they are no longer available in the national text product
  viewer (MISC Tab, 2nd Row, Left)

```
    "offn11: Navtex Marine fcst for Kodiak, AK (SE)",
    "offn12: Navtex Marine fcst for Kodiak, AK (N Gulf)",
    "offn13: Navtex Marine fcst for Kodiak, AK (West)",

    "fxcn01_d1-3_west: Days 1 to 3 Significant Weather Discussion - West",
    "fxcn01_d4-7_west: Days 4 to 7 Significant Weather Discussion - West",
    "fxcn01_d1-3_east: Days 1 to 3 Significant Weather Discussion - East",
    "fxcn01_d4-7_east: Days 4 to 7 Significant Weather Discussion - East",
```

* [ADD] All model activities (ie SPC SREF/HREF/HRRR, ESRL, NCEP, etc) - use floating buttons (
  similar to single pane) for back/forward
* [ADD] move setting from UI into settings Developer as they are discouraged to change and will
  likely be deprecated in the future
  "Radar with transparent toolbars"
  "Radar with transparent status bar"
* [ADD] Android prior to Android 7.1 is no longer supported
* [FIX] In Settings->Color, button text color is now dark instead of set to the default color for
  that particular item. If the preference is a light color it was hard to see.
* [ADD] In NHC Storm, add titles so that the image viewer (screen shown when tapping on an image)
  has something in subtitle and when sharing

## 55826 2023_09_09

* [NOTE] versions of Android prior to Android 7.1 (API Level 24 and below) will no longer supported
  after 2023-10-01.
* [FIX] crash in Alert detail (that does not have polygons present) caused by recent change
* [ADD] as mentioned in "Upcoming Changes" deprecated option "Prevent accidental exit" as this
  interferes with Google's
  long term strategy for the back button
* [ADD] Settings Playlist: use Floating button with text
* [ADD] VAD Wind Profile text now shows as fixed width
* [ADD] option "Models: use FAB" is removed as mentioned in Upcoming changes (added 2022-08-17)
* [ADD] Settings->User Interface. Improve option label and in some cases help text (accessed by
  tapping on label)
* [ADD] Move the "Celsius to fahrenheit table" to Settings->About instead of the main Settings
  screen.
* [ADD] Settings->Radar. Improve option labels and move 3 settings to Settings->About->Developer as
  they are not
  relevant or recommended to most users:
  "Multipurpose radar icons" "Counties use high resolution data" "States use high resolution data" "
  Black background"
* [NOTE] setting "Prevent accidental exit" will be removed the settings where you need to hit the
  back button twice
  from the main screen (in Settings->UI) will be retired in the next release due to Google's long
  term strategy
  (this was added to upcoming changes on 2022-09-01)
* [ADD] Radar Mosaics are no longer pulled from the AWC Website which is due to be upgraded on Sep
  12, 2023.
  It appears the mosaic graphics are no longer going to be provided so the default is to now use the
  graphics
  used prior to using AWC. (this was added to upcoming changes on 2022-04-02)
* [ADD] Observations activity: remove submenu and show "RTMA" button directly in toolbar
* [ADD] Alerts detail: use floating action button to access radar instead of icon in toolbar
* [ADD] Settings->Location: use Button with text for adding a new location
* [ADD] Settings->Location->Add: use Button with text for saving a new location
* [ADD] Spotters: use Button with text for navigating to spotter reports
* [ADD] Settings->Colors: Add subtitle to screen to indicate how to change color
* [ADD] MCD, MPD, Watch display activity: add radar button instead of icon in toolbar
* [ADD] Settings->About: minor visual changes
* [ADD] In Settings->Radar about "Beta" to label for "Color Palettes". The code has not changed but
  in general this feature
  is not easy to use and it probably has more bugs then other parts of the program.
* [ADD] Settings->About->Developer Settings and move settings from other areas that are easy for the
  developer only, will be
  deprecated soon (soon upcoming changes doc linked from top of FAQ), or in general are not
  applicable to the
  majority of users. As a reminder you can tap on the textual label for settings to open a pop-up
  with more information.
* [FIX] in the NHC Storm activity do not show images that don't exist. For example storms staying
  over the ocean do not
  have QPF and excessive rainfall graphics.
* [FIX] SPC MCD and WPC MPD screen were not showing areas affected in subtitle
* [ADD] SPC Thunderstorm outlook will now show the image full width when there is only one graphic
  active later in the day
* [ADD] Radar Color Palette selection screen: after user chooses one show yellow border around image
* [ADD] Radar Color Palette selection screen: submenu had one entry, remove and show entry in
  toolbar ("Help")
* [FIX] Spc Fire weather and excessive rainfall were not working after recent change 55823
* [ADD] NHC notifications will now be sent (less often) when the advNum changes for publicAdvisory (
  as opposed to lastUpdate for the storm)
  FYI - data comes from: https://www.nhc.noaa.gov/CurrentStorms.json
* [FIX] NHC Storm rainfall graphics were not working after NWS URL change (not all NHC Storms will
  have these graphics and may show as blank images)
* [ADD] NHC Storm sharing now includes more content in the subject and text product included

## 55813 2023_08_27

* [FIX] CPAC long range graphic is now 7 days instead of 5 (as accessed via main NHC activity)
* [ADD] NWS has issued change notification: SCN23-79: Upgrade of Aviation Weather Center Website on
  September 12, 2023
  This will cause temporary breakage in certain parts of the program such as Windbarbs and
  Observations in Nexrad and static aviation graphics.
  App updates will be issued as soon as possible after NWS moves over to the new website.

## 55807 2023_07_12

* [ADD] Ability to view state level SPC Convective Outlooks for Days 4 - 8 (select "State graphics"
  from submenu in any Convective Outlook activity)
* [ADD] For all state level SPC Convective Outlooks show all products available. Reminder that you
  can tap on an image to view by itself or double tap to zoom in.
* [ADD] "Test Message" was added to the list of choices in Settings -> Notifications -> WFO
  Notification filter. This Settings allows you to not
  receive notifications if configured and to not show up on the main screen.

## 55807 2023_07_02 (BETA)

* [ADD] "Test Message" was added to the list of choices in Settings -> Notifications -> WFO
  Notification filter. This Settings allows you to not
  receive notifications if configured and to not show up on the main screen.

## 55805 2023_06_27 (BETA)

* [ADD] Ability to view state level SPC Convective Outlooks for Days 4 - 8 (select "State graphics"
  from submenu in any Convective Outlook activity)
* [ADD] For all state level SPC Convective Outlooks show all products available. Reminder that you
  can tap on an image to view by itself or double tap to zoom in.

## 55804 2023_06_22

* [ADD] SPC Mesoanalysis, add sectors Intermountain West and Great Lakes
* [FIX] Textual labels in Nexrad that were recently forced to one line to avoid edge wrap should not
  apply to detailed observations
* [FIX] crash in SPC SREF due to not handling empty list at
  joshuatee.wx.models.UtilityModelSpcSrefInputOutput.getRunTime (line 47 in
  UtilityModelSpcSrefInputOutput.kt
* [FIX] NSSL WRF model activity was not working at all
* [FIX] NSSL WRF model activity remove FV3 which is no longer a supported model at this upstream
  site
* [FIX] SPC Meso, selected image was not being saved when using back arrow or keyboard in chromeOS
* [ADD] NCEP MAG models (MISC Tab - upper left tile) update "MAG 4.0.0 - May 2023" described
  here [https://mag.ncep.noaa.gov/version_updates.php](https://mag.ncep.noaa.gov/version_updates.php)
* [FIX] NCEP MAG GFS-WAVE: corrections to some already existing labels
* [ADD] Continue to deprecate use of Canadian weather data as mentioned in upcoming changes.
* [ADD] Excessive Rainfall Outlook activity (MISC Tab) now shows a Day 4 and Day 5 image. No
  dedicated text
  product exists similar to Day1-Day3 and so discussion is included in the PMDEPD "Extended Forecast
  Discussion"
  more details here:
  [Service Change Notice 23-55](https://www.weather.gov/media/notification/pdf_2023_24/scn23-55_ero_days_4_5_t2o.pdf)
  and
  [NWS Product Description Document - PDD](https://www.weather.gov/media/notification/PDDs/PDD_ERO_Days_4_5_T2O.pdf)

* [FIX] for Android 13 (API 33), no need to request storage permission

## 55795 2023_05_28

* [FIX] Attempt to avoid text label (ie cities, counties) wrapping at edges in Nexrad radar
* [FIX] NHC - replace retired 5 day outlooks with new 7 day outlooks
* [FIX] As communicated in the "upcoming changes" document in April 2022,
  Canadian local forecast support is being removed.
  In support of this the ability to add new Canadian locations is being disabled.

## 55791 2023_05_24

* [FIX] Access to twitter content for state and tornado is no longer working and has been removed.
  Please access via your browser if needed.
* [FIX] in SPC Mesoanalysis, 850mb version 2 when selected from bottom toolbar was bringing old
  850mb product
* [FIX] In single pane nexrad with toolbar showing, rainfall products are not updating text in color
  legend dynamically

## 55787 2023_03_28

* [FIX] NWS SPC has changed the URL/format type for SPC MCD and thus code updates were required
* [FIX] SPC Meso Violent Tornado Parameter (VTP) was not working as SPC changed the product ID
* [ADD] Widget with weather conditions will fall back to 2nd closest observation point if primary
  obs point is not updating in the past 2 hours (similar to main screen / notification)
* [FIX] NWS Has removed static graphic for space weather: Estimated Planetary K index
  and replaced with a web accessible version for this product
  at https://www.swpc.noaa.gov/products/planetary-k-index
  if you use this data you could access via a browser, etc
* [FIX] SPC Storm reports: minor change in date picker accent color used in darker themes
* [FIX] crash in RTMA when network is down
* [ADD] NCEP Models (MISC Tab, upper left) has replaced model "ESTOFS" with "STOFS" (v1.1.1)
  per https://mag.ncep.noaa.gov/version_updates.php
* [ADD] Add more verbiage to tab label edit area in Settings -> UI
* [ADD] Tap on label "Tab 1 Label" (and 2,3) in Settings -> UI now shows popup with brief
  description.
* [ADD] In the location editor change the label from "Conditions" to "Current Conditions" to add
  clarity on what this notifications for.
  As an FYI, you can tap on the textual label for all notifications for a greater description.
* [ADD] In the location editor change the label from "Radar" to "Radar image with alert" to add
  clarity on what this notifications for.
* [ADD] In the location editor change the label from "Sound" to "Play sound for alert notification"
  to add clarity on what this notifications for.
* [ADD] Weather conditions notifications will fall back to 2nd closest observation point if primary
  obs point is not updating in the past 2 hours (similar to main screen)
* [FIX] date picker in SPC Storm reports had incorrect color when using dark themes
* [FIX] Settings -> HomeScreen , if one taps top back arrow after making changes it now restarts app
  properly similar to tapping bottom arrow

## 55758 2023_01_04

* [ADD] Text widgets AFD/HWO/National Text now match formatting as seen within the main app
* [FIX] SPC Storm report share was not including full detail
* [FIX] Settings -> Playlist: buttons were not aligned correctly
* [FIX] Nexrad - crash if enabling a geometry such as county lines
* [REF] DownloadText - remove QPFPFD, product is no longer offered and was removed from WpcText some
  time ago
* [FIX] Local text product viewer navigation drawer background color was not consistent with rest of
  app
* [ADD] RTMA_TEMP (Real-Time Mesoscale Analysis) as a Homescreen option in the generic images menu

        more details on the product: [https://nws.weather.gov/products/viewItem.php?selrow=539](https://nws.weather.gov/products/viewItem.php?selrow=539)

        graphics are from here: [https://mag.ncep.noaa.gov/observation-type-area.php](https://mag.ncep.noaa.gov/observation-type-area.php)
* [ADD] RTMA accessible via Observations (main submenu)
* [FIX] TVS was not working
* [ADD] Hourly using old API no longer shows the date, just weekday/hour similar to Hourly with new
  API (uses ObjectDateTime.translateTimeForHourly)
* [FIX] ESRL RAP model was not working correctly
* [FIX] NSSL WRF (and other NSSL models) run only once per day, not twice
* [ADD] NCEP HREF now goes out to 48 hours (was 36)
* [ADD] color and spacing changes for navigation drawer in SPC Meso, SPC SREF, WPC Images, National
  Text
* [ADD] Dawn/Dusk to sunrise card on main screen
* [ADD] SPC HREF now has image layers stored in the application bundle so response should be
  slightly better with less data downloaded.
* [ADD] bring back SPC HREF "Reflectivity: stamps" including proper background
* [ADD] remove option in Settings->UI, "GOES GLM for lightning (requires restart)". Lightning Maps
  is being phased out and is no longer an option.
* [ADD] back SPC HREF reflectivity under heading "member viewer" including an additional layer that
  shows the colormap and time
* [FIX] from the national text product viewer remove the following which retired some time ago: "
  pmdsa: South American Synoptic Discussion"
* [ADD] SPC Fire Sum, SPC Swo Sum/Day, NHC, and WPC Rainfall Sum - avoid unnecessary downloads when
  switching back from other apps or power on/off
* [FIX] NWS radar mosaic sector was not being properly detected for locations in scope of sector
  SOUTHMISSVLY

## 55721 2022_10_01

This releases has been impacted by a significant re-write (ie refactor) of many parts of the
program.
This was necessary to make the code base easier to understand/maintain and be more consistent with
newer versions such as the
desktop versions. As a result, in some areas the program should appear faster and be efficient with
respect data data download.
This especially holds true with Nexrad radar and the "Severe Dashboard". In addition, limitations
present for many years have been
fixed. As with any major work, new bugs might have been introduced.
Please email me if something seems not right.

For users who have upgraded to Android 13 (API33) or have a fresh install of wX onto a device with
Android 13 you'll need to understand
some potential changes. First, if your widgets stop working please just remove and re-add them. I
have researched, not sure why that would be
needed. Second, despite what I have read about permission to send notifications being carried over,
if you are using notifications you should make sure
wX is allowed to send notifications either via "App Info" in Android or just navigating to
Settings->Notifications. Upon entry to that screen if
Notifications are not allowed you can allow them. As mentioned in the "upcoming changes document"
for various reasons widgets and notifications
are at the bottom of the "service priority" list.

Further, the settings "Prevent accidental exit" (where you need to hit the back button twice from
the main screen) in Settings->UI will be retired
at some point next year due to Android's long term strategy for the back button
outline [here](https://developer.android.com/guide/navigation/predictive-back-gesture)

* [FIX] NHCStorm images 3,4 don't work in ImageViewer (filter causes some images to be skipped)
* [FIX] In the main nexrad viewer (lightning icon), if you access settings->radar->colors and change
  the nexrad background the change will be immediate
* [FIX] In multipane radar if animate and then long press to switch radar site, animation keeps
  playing old site after switch
* [FIX] Hourly will remain a small text size regardless of text size chosen in settings->UI (which
  can lead to an unusable display of text if too large)
* [FIX] attempted fix for quad pane nexrad frame sizing issue
* [ADD] Settings -> Playlist, if user adds a product that is already there, have the popup
  notification close after 3 sec instead of requiring user to close
* [ADD] Add location screen, save status popup now closes automatically after 3 seconds
* [ADD] bring back prior back button handling in web based activities
* [FIX] if multiple nexrad on homescreen in some situations radar label in top current conditions
  card was not fully correct
* [FIX] nexrad - not all storm tracks were being drawn if enabled
* [FIX] model animations now start from current time position (not start) till end
* [FIX] seven day notification and legacy cc/7day widget has errant newlines in 7 day listing
* [FIX] legacy widget timestamp had dark text making it mostly invisible
* [FIX] legacy widget icons are not the same color (white vs off-white)
* [FIX] update NWS Radar Mosaic URL in response
  to [PNS22-09](https://www.weather.gov/media/notification/pdf2/pns22-09_ridge_ii_public_local_standard_radar_pages_aaa.pdf)
* [ADD] The location based "7day" notification will now open a normal forecast view instead of a
  plain text 7 day listing
* [FIX] SPC Meso multipane not saving x/y/zoom on exit
* [FIX] nexrad invoked from alert will not keep site when jump to multipane
* [FIX] SPC Compmap navigation drawer on/off for each parameter was not showing correctly
* [FIX] SPC Meso - layers topo and county were not working together
* [ADD] SPC Meso - add more layers: Observations/Population (Topo and Population can't be shown
  together)
* [ADD] Settings -> UI "Navigation Drawer Configuration" will show regardless of whether or not
  drawer is enabled
* [ADD] Settings->Main, update Notifications label if App not allowed to send
* [FIX] SPC convective outlook notif icon was displaying as all black
* [FIX] SPC MPD / Watch notification action was not working
* [ADD] Add Location - add label that if notifications are blocked will be displayed. If on Android
  13 you can tap on it to open the perm dialogue
* [FIX] Nexrad radar - if map is shown and then app is switched or screen is turned off, when
  returning to nexrad, hide the map
* [ADD] If running Android 13, screen recording is regrettably no longer working. Updated Nexrad to
  only show drawing tools. Updated all other image based activities
  to offer normal image sharing. Updated FAQ and Upcoming Changes
* [FIX] In Settings->Homescreen remove the "Web" option from the menu. This is meant as a workaround
  when NWS 7 day is not working. However, the web widget appears to no longer be working either.
* [ADD] [Target Android 13](https://developer.android.com/google/play/requirements/target-sdk)
* [ADD] recent changes in how polygon data is being downloaded and cached no longer require the
  background job to fetch it
* [ADD] DownloadTimer set warnings to 3 min instead of based off a variable that doesn't necessarily
  relate to it
* [ADD] In activity to add or edit location, place cursor at end of text in label field
* [ADD] Settings -> location add # of locations in title
* [FIX] settings -> location truncates actual stored lat / lon too much - add a few more characters
* [ADD] Settings->Radar has been split it two activities. The line/marker (tvs/hail) size settings
  can now be accessed
  via a second activity accessible from the "Line / Marker sizes" button 3rd from the top
* [FIX] SPC SREF - when using left/right button to move through model images, image was download
  twice instead of just once
* [ADD] In US Alerts submenu, show Alaska instead of AK (and same for HI)
* [FIX] In nexrad widget, draw county line below state-line
* [ADD] long press radar status message shows nearest radar to where long press occurred - not radar
  currently selected
* [FIX] handle RSM (radar status message) better for terminal radars
* [FIX] SPC SWO Day X State graphic - don't like AK/HI in menu as SPC only covers CONUS
* [FIX] Revert this change as it breaks the radar on the main screen when "center on location" is
  enabled: location dot on main screen will continue to be dot with circle around it (for now)
* [ADD] Nexrad radar widget now respects geographic line size setting as used for the main program
  Nexrad
* [ADD] Nexrad multipane, if you tap on the text in the upper left that lists radar site/product it
  will take you to severe dashboard (similar to single pane)
* [ADD] Nexrad multipane, if warnings(tor/ffw/tst) enabled, show in top toolbar similar to single
  pane
* [FIX] tap on any NHC notification would cause a crash (user reported)
* [FIX] NHC graphics for ATL storms were not working
* [ADD] Nexrad long press - shorten (since large text goes off screen) and make verbiage consistent
  with iOS port wXL23
* [FIX] adhoc location forecast activity (via nexrad long-press) had to many digits for lat/lon
* [FIX] In single pane nexrad, if animating, switching radars via the map was not working
* [FIX] nexrad help in submenu needed to be freshened up a bit
* [FIX] nexrad autorefresh fix related to onresume/pause or restart
* [ADD] nexrad multipane "ctrl-a" will now toggle an animation on/off (similar to how single pane
  has been)
* [ADD] nexrad multipane "ctrl-r" (ref) and "ctrl-v" (vel) added (similar to how single pane has
  been) (use alt-? on main screen and nexrad to show shortcuts)
* [FIX] The main screen will no longer show the primary "location dot" surrounded by a circle as
  this shape is reserved from the dedicated
  nexrad viewer which if configured does use active GPS location
* [ADD] severe dashboard - warnings/watch/mcd/mpd are downloaded in parallel
* [ADD] to meet the Oct 2022 announcement - raise minSDK to Android 6 API 23 from API 21
* [FIX] Day 8-14 Hazard Outlook is to large to display via default options
* [FIX] in Color Palette Editor remove the unimplemented "help" option
* [FIX] in Color Palette Editor remove the "load from file" option - please use copy/paste instead.
  It is not worth the complexity to leave this in place
* [ADD] in settings->radar WPC Fronts line size is now a configurable item
* [ADD] Us Alerts - if in landscape do 2 columns
* [ADD] Nexrad radar has had a large chunk of it re-written to modernize the code and to help
  improve performance
*       by doing more work in parallel. Please do email me if any bugs are encountered.
* [ADD] For new NWS radar mosaic, tile icon in MISC tab will show the last viewed location. Main
  submenu will continue to show nearest image.
* [ADD] in Nexrad single pane some settings changes that previously required restarting the nexrad
  view no longer require (like adding CA/MX borders)
* [FIX] wpc mpd as viewed in playlist has HTML tags showing (changed WpcTextProductsActivity.kt)
* [ADD] Nexrad on main screen and multipane nexrad now show SPC Convective Outlooks if configured to
  do so
* [FIX] Nexrad long press observation dialogue was not respecting the text size setting
* [FIX] nexrad - long press and select the "Beam Height" entry changed the radar site, it should do
  nothing
* [FIX] MCD/MPD/Watch viewer won't crash if text product is not yet available
* [ADD] Nexrad radar option "Show radar during a pan/drag motion" which is enabled by default to
  mimic historic behavior
* [FIX] SPC SREF - remove help from submenu as it doesn't add value
* [FIX] SPC SREF - correct formatting for status of current model run in submenu
* [ADD] Multi-pane nexrad now shows WPC Fronts if configured
* [ADD] Spc Storm Reports drawer with states used to filter is now sorted
* [FIX] if user chose to not use main screen radar floating action button and also chose to use a
  navigation drawer, the radar FAB would not show
* [FIX] adhoc forecast via long press in nexrad was not showing correct sunrise/sunset data
* [FIX] If audio was previously paused, going into another activity with text to speech controls
  shows text "stop button" instead of icon
* [ADD] Material3 "You" partial implementation for all themes
* [FIX] TouchImageView2.java, add try/catch in onDraw to handle images that are to large. Show
  nothing but don't crash
* [FIX] WPC Rainfall discussion, hide radar icon as it serves no purpose in this activity
* [FIX] ObjectCurrentConditions.kt - if the heat index and the temp (both rounded) are equal, don't
  show heat index
* [ADD] SPC MCD/Watch summary icons in SPC Tab now show only images regardless of how many Watch or
  MCD there are. This resolves one bug and makes the interface more predictable. Please note that
  the "Severe Dashboard" accessible on the main screen via octagon icon is the preferred method to
  see MCD/Watch. When the Navigation drawer is configured as compared to Tabs they are not even
  visible.
* [ADD] SPC MCD/Watch summary - if no watch or mcd is present use a bigger font with a "high light"
  color
* [ADD] In severe dashboard if no warnings/watch/mcd/mpd are present show "None" in subtitle
* [ADD] In Settings -> Homescreen the menu option "Set to default (Canada)" is being removed in
  preparation for CA content removal by end of next year.
* [ADD] Settings -> celsius to fahrenheit table was rounding to integer but still showing with ".0"
  at the end - remove
* [ADD] new GOES sector: South Pacific
* [ADD] new GOES sector: Northern Atlantic
* [ADD] GOES activity - in Pac/Atl submenu - alphabetize entries
* [ADD] GOES activity - in Regional Views - alphabetize entries
* [ADD] GOES activity - add new submenu "North/South America" as "Regional Views" had too many
  entries
* [FIX] The following model is being removed from the program due to it's experimental nature and
  numerous breaking changes over the years:
  **it was accessible only via the NHC activity**: Great Lakes Coastal Forecasting System, GLCFS
  You can access it via a web browser here: https://www.glerl.noaa.gov/res/glcfs/
  As a reminder the best model interface in terms of stability continues to be MAG NCEP (MISC Tab -
  upper left)
  I believe all other models with interfaces provided are not considered true production services,
  please contact me if I am wrong

* [ADD] Vis/SPC Meso/Radar Mosaic now have a play/stop/pause bottom for animations that is more
  appropriate to the circumstance and similar to Nexrad
* [ADD] In GOES/SPC Meso animate icon now uses the number of frames (default 10 - can be change in
  settings->UI), similar to nexrad
  a submenu in the main menu will allow access to the other frame count choices
* [ADD] If device is in landscape some activities will show 3 images across instead of just 2 (SPC
  Convective outlook, fire, wpc rainfall, etc)
* [ADD] SpcStormReportsActivity - add subtitle indicating you can tap the image to open a date
  picker
* [FIX] 7day not honoring C/F setting
* [FIX] slowness in LocationEdit search icon appearing by loading cities before the activity is
  displayed
* [FIX] SPC HREF - some images not working, more work is needed (non-production model service)
* [ADD] Settings->Notification: alphabetize with logical groups
* [ADD] Settings: alphabetize
* [FIX] In usalerts for sps (special weather statement) and other alerts - don't show radar button
  if no polygon data is present
* [FIX] Nexrad: if non-default option to use hi-res State lines data, not all lines were showing
* [ADD] In GOES, AWC, and NWS radar mosaic if you touch blank space in the top toolbar or the label
  to the left it will open the navigation drawer
* [FIX] remove deprecated option in settings->widgets (show warnings in radar mosaic)
* [ADD] minor alphabetization in settings->widgets
* [FIX] status bar icon for location specific radar notification
* [REF] remove animated gif share which is not working and is considered deprecated within the
  program
* [ADD] Nexrad radar long press - shorten verbiage
* [FIX] new NWS Radar Mosaic - remove menu with products that aren't available
* [FIX] NHC now shows complete summary information and text products for Central Pacific "CP"
  hurricanes
* [ADD] Radar settings - mostly alphabetized now with common options listed at top
* [REF] Refactor some parts of the code base to make it more maintainable - you should not notice
  anything
* [ADD] (main screen tabs) Switch to ViewPager2 and use FragmentStateAdapter since Google deprecated
  ViewPager - you should not notice anything
* [ADD] Option to use new NWS Radar Mosaic (restart required). This is a temporary option as it
  appears likely the default mosaic will switch to NWS (away from AWC) later this year. Note the
  behavior may change slightly as the feature is refined
* [REF] Change Level2 data site back to nomads after maintenance in Apr 2022
* [REF] (remove unused assets to slightly reduce size of app) remove MyApplication.radarHwEnh, git
  rm ./app/src/main/res/raw/hw
* [ADD] NWS Radar Mosaic - play icon toggles to stop and add ability to stop animation via button
* [ADD] Nexrad radar long press - shorten verbiage
* [FIX] partial - For NHC central pacific storms (images show but not text product - work in
  progress)
* [FIX] NHC notifications will now alert for ATL or EPAC depending on which is selected
* [ADD] In the US Alert viewer, show warning types and states in sorted manner
* [ADD] Settings -> UI alphabetize
* [ADD] refine labels in Settings -> UI
* [ADD] remove option "Reduce size of tile images" TILE_IMAGE_DOWNSIZE
* [ADD] remove option "Show VR button on main screen" VR_BUTTON
* [ADD] Target the latest version of Android (this is periodically required to be compliant with
  Google Play Store)
* [ADD] Upgrade software component required by wX (okhttp)
* [FIX] terminal radar TICH (Wichita, KS) was not properly coded (was TICT), it is now available for
  use
* [FIX] prevent nexrad radar from showing stale SWO data (earlier fix was not good)
* [ADD] SPC Meso additions
    - under Thermodynamics add "Skew-T Maps" skewt
    - under "Winter Weather" add "Winter Skew-T Maps" skewt-winter

## 55599 2022_04_16

* [FIX] more robustness for Hourly using old NWS API
* [ADD] Please see URL above for a new page about upcoming changes to certain parts of wX
* [ADD] Nexrad Level2: in response to 24+ hr IT maintenance on 2022-04-19 to nomads NWS servers,
  change URL to backup
    - [https://www.weather.gov/media/notification/pdf2/scn22-35_nomads_outage_apr_aaa.pdf](https://www.weather.gov/media/notification/pdf2/scn22-35_nomads_outage_apr_aaa.pdf)
    -
  A [reminder](https://gitlab.com/joshua.tee/wxl23/-/blob/master/doc/FAQ.md#why-is-level-2-radar-not-the-default)
  on Level 2 support within wX
* [ADD] SPC Meso - new "layer" county boundaries. Turn on from submenu -> Layers (NOTE: the state
  line boundary provided for the Radar Mosaic does not quite match up with the county layer. This
  can be observed from the SPC Website as well)
* [ADD] SPC Meso - new param: "Hodograph Map", access from "Wind Shear" submenu. Only usable on
  region specific sectors, not CONUS.
* [ADD] The default hourly data provided (the new NWS API) does not reliably return results on the
  first download, added a retry mechanism.
* [ADD] access to settings via hourly in case app won't load (can access via 4x4 blue widget ->
  hourly -> settings)
* NWS is deprecating this website on 3/22 (substitute NWS observation point,
  etc): https://www.wrh.noaa.gov/mesowest/timeseries.php?sid=KCAR
  in favor of: https://www.weather.gov/wrh/timeseries?site=KCAR
  Thus updated the activity accessed through the "MISC" tab to reflect this
* [FIX] GOES GLM (lightning) animation was not working
* [ADD] (main screen) if closest observation point has data older then 2 hours, show data for the
  2nd closest obs point instead
* [FIX] prune the list of Observation points by removing 57 sites that had not updated in the past
  28 days
  This pruning will occur more frequently in the future to avoid a bad user experience
* [ADD] upgrade software that wX uses
* [FIX] remove the following weather obs point in `stations_us4.txt` and `us_metar3.txt` as user
  reported it has not updated since Jan 26

```
IL,ROMEOVILLE/CHI  ,KLOT
KLOT 41.6 -88.1
```

### version **55584** - released on 2022/02/10

* [ADD] In Settings -> UI -> Navdrawer config, have top arrow respond in the same way that bottom
  arrow does when pressed
* [ADD] In Settings -> about, add navdrawer token string to assist in troubleshooting
* [FIX] remove observation point KSTF (Starkville, MS) as it's impacting users.
* [FIX] remove decommissioned text products
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
* [FIX] GOES Viewer, eep Eastern East Pacific image was not working after NOAA changed image
  resolution
* [FIX] remove external links in Color Palette editor
* [FIX] remove unprintable chars in nexrad county labels

### version **55570** - released on 2021/10/22

* [FIX] NWS html format changed caused 7 day forecast icons to break
* [ADD] Additional GOES products FireTemperature, Dust, GLM
* [ADD] Move ChangeLog and FAQ to Gitlab
* [ADD] Settings -> About, open browser when viewing FAQ or ChangeLog
* [FIX] icon size on main window navigation drawer if using that option
* [FIX] remove CLI "Daily Climate Report" from Local text products since NWS no longer provides WFO
  to site mapping
* [ADD] per https://developer.android.com/about/versions/12/approximate-location
  if you request ACCESS_FINE_LOCATION you must also request ACCESS_COARSE_LOCATION
* [REF] LsrByWfoActivity refactor to use Future* and parallel download threads
* [FIX] add to UtilityMetarConditions "Patches Of Fog"
* [REF] Numerous changes to make the source code base easier to support in the long run and changes
  required by google to keep the app active on the Google Play Store (ie target a new version of
  Android,
  etc)

