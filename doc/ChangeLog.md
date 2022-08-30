```
// [FIX] chromeOS: text size for observations in nexrad
// [FIX] storm reports - having location follow gps breaks the location marker
// [ADD] 'getter for defaultDisplay: Display!' is deprecated. Deprecated in Java
// [ADD] 'getMetrics(DisplayMetrics!): Unit' is deprecated. Deprecated in Java
// [REF] migrate all to Future* (BackgroundFetch, Nexrad)
// [REF] replace String.format with stuff in to.kt
// [ADD] user request for metar homescreen widget
// [ADD] user request for dawn/dusk, look to migrate to: https://github.com/phototime/solarized-android
// [ADD] user request for multiple nexrad widgets of different sites (products?)
// [FIX] SPC HREF radar stuff
// [REF] IntentService is deprecated (AudioService* and others) https://stackoverflow.com/questions/62138507/intentservice-is-deprecated-how-do-i-replace-it-with-jobintentservice
// [ADD] handle deprecations in UtilityUI https://stackoverflow.com/questions/62577645/android-view-view-systemuivisibility-deprecated-what-is-the-replacement
// [FIX] nexrad invoked from alert will not keep site when jump to multipane
// [FIX] USAlerts state count is not accurate
// [FIX] dual/quad pane radar don't take up the entire screen on pixel 6a
// [FIX] deprecated startActivityForResult onActivityResult (see example in SettingsNotificationsActivity.kt)
// [REF] After Oct 2022 - raise targetSdkVersion to Android 13 API 33 from API 32
// [REF] ObjectNhc (activity) and UtilityNhc (notification) should share code
// [REF] consolidate single and multipane as much as possible
// [REF] look for methods that take top/bottom toolbars and can instead pass custom activity
// [FIX] nexrad multipane change tilt and then tap ref icon, changes back to lowest tilt (unlike single pan)
// [FIX] nexrad single and multi use differents way to adjust menus for tilt and tdwr
// [ADD] nexrad - show map, phone off, onrestart observations show with map still up
// [FIX] multipane with no locked panes, long press on non-selected radar will select it but not change in title
// [ADD] debug option with circular log for downloads
// [FIX] look for other Route.* that do not need Array as arg
```
[[_TOC_]]

## 55688 2022_08_30

## 55687 2022_08_30
* [REF] misc refactor
* [ADD] In activity to add or edit location, place cursor at end of text in label field

## 55686 2022_08_29
* [FIX] nexrad widget warning colors broke in recent refactor
* [REF] refactor warning handling in UtilityCanvas for NexradWidget

## 55685 2022_08_29
* [REF] misc refactor - SettingsLocationGenericActivity.kt
* [FIX] In activity to add/modify location, if you delete location, add new location - lat/lon is not blank and shows for the deleted location
* [ADD] Settings -> location add # of locations in title
* [FIX] settings -> location truncates actual stored lat / lon too much - add a few more characters

-        classpath 'com.android.tools.build:gradle:7.2.1'
-        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21"
+        classpath 'com.android.tools.build:gradle:7.2.2'
+        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10"

## 55684 2022_08_28
* [REF] nexrad widget, refactor and re-order some of the drawing
* [REF] remove unused ProjectionType.WX_OGL_48
* [ADD] Settings->Radar has been split it two activities. The line/marker (tvs/hail) size settings can now be accessed
        via a second activity accessible from the "Line / Marker sizes" button 3rd from the top
* [FIX] SPC SREF - when using left/right button to move through model images, image was download twice instead of just once

## 55683 2022_08_28
* [REF] misc refactor - cleanup in canvas nexrad and projection type

## 55682 2022_08_27
* [REF] misc refactor - remove Route.image that fills background to white (test NHC)
* [ADD] In US Alerts submenu, show Alaska instead of AK (and same for HI)
* [FIX] In nexrad widget, draw county line below state-line
* [ADD] use enum for fav mgmt

## 55681 2022_08_27
* [REF] misc refactor
* [ADD] Nexrad radar widget now respects geographic line size setting as used for the main program Nexrad

## 55680 2022_08_27
* [REF] misc refactor
* [ADD] long press radar status message shows nearest radar to where long press occurred - not radar currently selected
* [FIX] handle RSM (radar status message) better for terminal radars
* [FIX] SPC SWO Day X State graphic - don't like AK/HI in menu as SPC only covers CONUS
* [FIX] Revert this change as it breaks the radar on the main screen when "center on location" is enabled: location dot on main screen will continue to be dot with circle around it (for now)

## 55679 2022_08_24
* [REF] misc refactor

## 55678 2022_08_23
* [FIX] nexrad autorefresh fix related to onresume/pause or restart (use onresume and not both)
* [ADD] nexrad multipane "ctrl-a" will now toggle an animation on/off (similar to how single pane has been)
* [ADD] nexrad multipane "ctrl-r" (ref) and "ctrl-v" (vel) added (similar to how single pane has been) (use alt-? on main screen and nexrad to show shortcuts)
* [ADD] additional helper Nexrad* classes

## 55677 2022_08_22
* [REF] misc refactor
* [FIX] nexrad autorefresh fix related to onresume/pause or restart

## 55676 2022_08_22
* [REF] misc refactor
* [FIX] In single pane nexrad, if animating, switching radars via the map was not working
* [FIX] nexrad help in submenu needed to be freshened up a bit
* [FIX] The main screen will no longer show the primary "location dot" surrounded by a circle as this shape is reserved from the dedicated
         nexrad viewer which if configured does use active GPS location
* [ADD] NexradLongPressMenu and changes in NexradState* and NexradArguments* to make it happen

## 55675 2022_08_21
* [REF] NexradDraw, reduce arg count with NexradState
* [REF] VideoRecordActivity.kt, remove obsolete SDK checks after going to 23
* [REF] nexrad convert locXCurrent to double
* [ADD] Nexrad long press - shorten (since large text goes off screen) and make verbiage consistent with iOS port wXL23
* [FIX] adhoc location forecast activity (via nexrad long-press) had to many digits for lat/lon

## 55674 2022_08_20
* [REF] more work on NexradState for LocationFragment
* [REF] LocationFragment, remove paneList and SDK check 23 related to activity

## 55673 2022_08_20
* [REF] more work on NexradState
* [ADD] Nexrad multipane, if you tap on the text in the upper left that lists radar site/product it will take you to severe dashboard (similar to single pane)
* [ADD] Nexrad multipane, if warnings(tor/ffw/tst) enabled, show in top toolbar similar to single pane
* [REF] Nexrad multipane, remove idxIntAl, it is redundant to NexradState.curRadar

## 55672 2022_08_20
* [REF] use NexradState in multi pane Nexrad

## 55671 2022_08_19
* [REF] nexrad - migrate away from paneList
* [FIX] tap on any NHC notification would cause a crash (user reported)
* [FIX] NHC graphics for ATL storms
* [FIX] wxglrender     @Synchronized fun constructWarningLines(polygonWarningType: PolygonWarningType) {
* [REF] use NexradState in single pane Nexrad

## 55670 2022_08_19
* [REF] nexrad work, objectImageMap, getContent, remove: restartedZoom, oglInView, firstRun, mapShown, restarted, tiltOption, animRan

## 55669 2022_08_19
* [ADD] additional download threads to "Severe Dashboard"
* [ADD] nexrad multi-pane, remove single threaded getContent, use @Synchronize for spotters if needed (or other)
* [REF] move getImageForShare in nexrad* into utilRadarUI

## 55668 2022_08_18
* [ADD] severe dashboard - warnings/watch/mcd/mpd are downloaded in parallel
* [REF] WXGLRender.constructStiLines - use constructLines and not constructLinesShort (and change in NexradLayerDownload)
* [REF] WXGLRender - changes in wind barbs layout
* [REF] Nexrad - remove some archiveMode used at granular level
* [ADD] to meet the Oct 2022 announcement - raise minSDK to Android 6 API 23 from API 21
* [REF] nexrad remove var touchImage: TouchImageView2
* [ADD] NexradArguments - dedicated class to handle nexrad arguments

## 55667 2022_08_17
* [REF] remove some dependence on PolygonWarningType
* [FIX] Day 8-14 Hazard Outlook is to large to display via default options
* [FIX] in Color Palette Editor remove the unimplemented "help" option
* [FIX] in Color Palette Editor remove the "load from file" option - please use copy/paste instead. It is not worth the complexity to leave this in place
* [ADD] in settings->radar WPC Fronts line size is now a configurable item

## 55666 2022_08_17
* [REF] LocationFragment.kt: val cardViews = mutableListOf<CardView>() -> val cards = mutableListOf<Card>()
* [ADD] Us Alerts - if in landscape do 2 columns
* [ADD] Nexrad radar has had a large chunk of it re-written to modernize the code and to help improve performance
*       by doing more work in parallel. Please do email me if any bugs are encountered.
* [ADD] For new NWS radar mosaic, tile icon in MISC tab will show the last viewed location. Main submenu will continue to show nearest image.
* [REF] add RadarGeomInfo and revamp RadarGeometry
* [ADD] in Nexrad single pane some settings changes that previously required restarting the nexrad view no longer require (like adding CA/MX borders)

## 55665 2022_08_16
* [ADD] WPC Fronts to PolygonType
* [ADD] wxglrender remove deconstruct stuff
* [REF] remove checks in NexradLayerDownload: if (!wxglRender.product.startsWith("2")) {

## 55664 2022_08_15
* [REF] radarPref - remove leading "radar" in vars

## 55663 2022_08_15
* [REF] misc refactor
* [FIX] wpc mpd as viewed in playlist has HTML tags showing (changed WpcTextProductsActivity.kt)
* [REF] move SPC SWO, Obs, Windbarbs, tvs, hi, sti, and spotters into NexradLayerDownload
* [ADD] wxglrender - ondraw, check if enabled before drawing

## 55662 2022_08_15
* [REF] misc refactor
* [ADD] Nexrad on main screen and multipane nexrad now show SPC Convective Outlooks if configured to do so
* [ADD] NexradDraw and move content from UtilityRadarUI
* [FIX] Nexrad long press observation dialogue was not respecting the text size setting
* [FIX] Nexrad new - wpc fronts exceptions (@Synchronized fun constructWpcFronts())

## 55661 2022_08_14
* [REF] misc refactor
* [FIX] new radar was not launching via static shortcut
* [FIX] nexrad - long press and select the "Beam Height" entry changed the radar site, it should do nothing

## 55660 2022_08_14
* [REF] misc refactor
* [FIX] MCD/MPD/Watch viewer won't crash if text product is not yet available
* [FIX] SevereDashboard will now only show "Done" in title if all downloads are done
* [ADD] Move new Nexrad Radar file into place

## 55659 2022_08_14
* [REF] misc refactor
* [ADD] move from UtilityDownloadWarnings to ObjectPolygonWarning for TOR/TST/FFW (will need a few weeks of testing)

## 55658 2022_08_14
* [REF] misc refactor
* [ADD] Nexrad radar option "Show radar during a pan/drag motion" which is enabled by default to mimic historic behavior
* [FIX] download warnings in NexradLayerDownload which is now used by LocationFragment, Multipane, and Nexrad new

## 55657 2022_08_13
* [REF] misc refactor
* [REF] RadarPreferences - change var naming radarColor* to color*
* [FIX] SPC SREF - remove help from submenu as it doesn't add value
* [FIX] SPC SREF - correct formatting for status of current model run in submenu
* [ADD] Multi-pane nexrad now shows WPC Fronts if configured

## 55656 2022_08_13
* [REF] wxglrender use maps for geo buffers
* [REF] RID.kt takes 3 arguments with no defaults
* [ADD] Spc Storm Reports drawer with states used to filter is now sorted
* [FIX] date picker in spc storm reports for some themes
* [REF] add to Route class
* [FIX] if user chose to not use main screen radar floating action button and also chose to use a navigation drawer, the radar FAB would not show
* [FIX] adhoc forecast via long press in nexrad was not showing correct sunrise/sunset data
* [REF] add SunRiseCard and use in LocationFragment and ForecastActivity
* [REF] WXGLRadarActivityMultiPane.kt refactor getContent to use FutureVoid
* [FIX] If audio was previously paused, going into another activity with text to speech controls shows text "stop button" instead of icon

## 55655 2022_08_06
* [REF] misc refactor

## 55654 2022_08_05
* [FIX] TouchImageView2.java, add try/catch in onDraw to handle images that are to large. Show nothing but don't crash

## 55653 2022_08_05
* [REF] add ObjectToolbar
* [FIX] WPC Rainfall discussion, hide radar icon as it serves no purpose in this activity

## 55652 2022_08_04
* [ADD] Material3 "You" attempt for all themes

## 55651 2022_08_04
* [ADD] change SeekBar to AppCompatSeekBar in ObjectNumberPicker
* [ADD] change Spinner to AppCompatSpinner in ObjectSpinner
* [ADD] change Spinner to AppCompatSpinner in ObjectSpinner
* [ADD] change TextView to MaterialTextView in CardText
* [FIX] how FAB color is handled
* [ADD] Material3 "You" attempt for blue theme

## 55650 2022_08_03
* [REF] misc refactor
* [ADD] Material3 "You" attempt for default theme

## 55649 2022_08_03
* [REF] misc refactor

## 55648 2022_08_03
* [ADD] ImageMap.java - change base class from android.widget.ImageView to androidx.appcompat.widget.AppCompatImageView
* [FIX] ObjectCurrentConditions.kt - if the heat index and the temp (both rounded) are equal, don't show heat index
* [ADD] SPC MCD/Watch summary icons in SPC Tab now show only images regardless of how many Watch or MCD there are. This resolves one bug
          and makes the interface more predictable. Please note that the "Severe Dashboard" accessible on the main screen via octagon icon
          is the preferred method to see MCD/Watch. When the Navigation drawer is configured as compared to Tabs they are not even visible.
* [ADD] SPC MCD/Watch summary - if no watch or mcd is present use a bigger font with a "high light" color
* [ADD] SPC MCD/Watch summary - add timer and onrestart
* [ADD] SPC MCD/Watch single - add onrestart
* [REF] ObjectDateTime migration
* [ADD] In severe dashboard if no warnings/watch/mcd/mpd are present show "None" in subtitle

## 55647 2022_08_02
* [REF] misc refactor (radar incl)
* [FIX] deprecations in UtilityUI.immersiveMode, Nexrad related to transparent status bar

## 55646 2022_08_01
* [REF] misc refactor
* [ADD] In Settings -> Homescreen the menu option "Set to default (Canada)" is being removed in preparation for CA content removal by end of next year.
* [ADD] SettingsLocationGenericActivity.kt in onRequestPermissionsResult add super call, remove unused VR code
* [ADD] Settings -> celsius to fahrenheit table was rounding to integer but still showing with ".0" at the end - remove
* [FIX] in response to pixel 6a having main screen nav drawer truncating top entry made these two changes:
* [FIX] in nav_header_main.xml change the main LinearLayout from android:layout_height="@dimen/nav_header_height" to android:layout_height="match_parent"
* [FIX] in nav_header_main.xml change the main LinearLayout from android:gravity="bottom" to android:gravity="center_vertical"
* [FIX] in activity_main_drawer.xml and app/src/main/res/layout/activity_main_drawer_right.xml change NavigationView at bottom to android:fitsSystemWindows="false"
* [ADD] refactor Settings* to match desktop ports
* [ADD] restructure (MCD/MPD/Watch viewer) SpcMcdWatchShowActivity to download text and image in parallel
* [REF] ObjectCardCurrentConditions.kt, remove time arg for updateContent, rename to  update
* [REF] ObjectCardCurrentConditions.kt, rename "updateContent" to "update"
* [REF] ObjectCardCurrentConditions.kt, remove "bitmap: Bitmap" as first arg to "update"
* [REF] ObjectCard7Day taking url instead of bitmap as 2nd arg
* [REF] rename ObjectCard7Day to SevenDayCard
* [REF] fix deprecated startActivityForResult, onActivityResult in SettingsNotificationsActivity.kt, CommonActionBarFragment.kt
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

## 55645 2022_07_29
* [ADD] ObjectAnimate.kt to be used in RadarMosaicNwsActivity/SPC Meso/Vis
* [REF] cut-over WATCH to ObjectPolygonWatch
* [ADD] Vis/SPC Meso/Radar Mosaic now have a play/stop/pause bottom for animations that is more appropriate to the circumstance and similar to Nexrad
* [ADD] In GOES/SPC Meso animate icon now uses the number of frames (default 10 - can be change in settings->UI), similar to nexrad
        a submenu in the main menu will allow access to the other frame count choices


## 55644 2022_07_27
* [FIX] locfrag continued attempts to fix crash on app launch on older versions

## 55643 2022_07_27
* [ADD] locfrag.getRadar - move to FutureVoid. (ran into issues on initial attempt, not able to replicate failures in google pre-launch but did find emulators with older APIs to replicate)
        ended up using MyApplication.appContext instead of activityReference in several spots
* [REF] cut-over MCD to ObjectPolygonWatch

## 55642 2022_07_27
* [REF] misc refactor
* [FIX] revert locfrag change

## 55641 2022_07_27
* [REF] misc refactor
* [REF] locfrag, move to FutureVoid

## 55640 2022_07_26
* [REF] misc refactor
* [REF] readPref -> readPrefInt
* [REF] cut-over MPD to ObjectPolygonWatch, put framework for Mcd/Watch in place as well

## 55639 2022_07_26
* [REF] misc refactor
* [REF] ExternalGeodeticCalculator -> convert from class to object since all static
* [REF] writePref -> writePrefInt
* [REF] writePref -> writePrefFloat
* [REF] writePref -> writePrefLong
* [REF] readPref -> readPrefFloat
* [REF] readPref -> readPrefLong
* [REF] re-org colorpal* to match desktop ports

## 55638 2022_07_25
* [REF] misc refactor
* [ADD] @Synchronized to 2 methods in WXGLRender.kt

## 55637 2022_07_25
* [REF] misc refactor
* [ADD] If device is in landscape some activities will show 3 images across instead of just 2 (SPC Convective outlook, fire, wpc rainfall, etc)

## 55636 2022_07_25
* [REF] misc refactor

## 55635 2022_07_25
* [REF] misc refactor
* [ADD] ObjectDatePicker for SpcStormReportsActivity
* [ADD] SpcStormReportsActivity - add subtitle indicating you can tape the image to open a date picker

## 55634 2022_07_24
* [REF] misc refactor

## 55633 2022_07_23
* [REF] misc refactor

## 55632 2022_07_23
* [REF] misc refactor
* [FIX] 7day not honoring C/F setting
* [REF] ObjectTouchImageView -> TouchImage
* [REF] DisplayDataNoSpinner and WpcImagesActivity were not using TouchImage (using raw TouchImageView2)

## 55631 2022_07_22
* [REF] misc refactor

## 55630 2022_07_22
* [REF] misc refactor

## 55629 2022_07_22
* [FIX] slowness in LocationEdit search icon appearing by loading cities before the activity is displayed
* [FIX] SPC HREF - some images not working, more work is needed (non-production model service)
* [ADD] Settings->Notification: alphabetize with logical groups
* [ADD] Settings: alphabetize
* [FIX] In usalerts for sps (special weather statement) and other alerts - don't show radar button if no polygon data is present
* [REF] rename ObjectCardAlertSummaryItem to ObjectCardAlertDetail, move button connections into card

## 55628 2022_07_21
* [REF] add ObjectLocation
* [REF] move MyApplication.locations to Location

## 55627 2022_07_20
* [FIX] Nexrad: if non-default option to use hi-res State lines data, not all lines were showing

## 55626 2022_07_20
* [ADD] In US Alerts - if you touch blank space in the top toolbar or the label to the left it will open the navigation drawer
* [REF] In all activities when a context is needed replace "this@ForecastActivity" with "this" (example)

## 55625 2022_07_20
* [ADD] In GOES, AWC, and NWS radar mosaic if you touch blank space in the top toolbar or the label to the left it will open the navigation drawer

## 55624 2022_07_19
* [REF] misc refactor

## 55623 2022_07_19
* [REF] misc refactor - UI

## 55622 2022_07_19
* [REF] misc refactor

## 55621 2022_07_18
* [REF] remove ./app/src/main/java/joshuatee/wx/util/AnimatedGifEncoderExternal.java
* [REF] replace UtilityStringExternal.truncate with String.take
* [FIX] remove deprecated option in settings->widgets (show warnings in radar mosaic)
* [ADD] minor alphabetization in settings->widgets
* [ADD] ObjectPolygonWatch and use simple framework (just DataStorage directly)

## 55620 2022_07_17
* [REF] remove CreateAnimatedGifService.kt
* [REF] simplify okhttp setup in myapp
* [FIX] status bar icon for location specific radar notification

## 55619 2022_07_17
* [REF] remove animated gif share which is not working and is considered deprecated within the program

## 55618 2022_07_17
* [ADD] Nexrad radar long press - shorten verbiage (con't)
* [REF] UtilityRadarUI/WXGLSurfaceView change from MutableList to List in some areas
* [FIX] new NWS Radar Mosaic - remove menu with products that aren't available
* [FIX] NHC now shows complete summary information and text products for Central Pacific "CP" hurricanes
* [REF] continue moving content out of MyApp and into UIPreferences and others

## 55616 2022_07_16
* [ADD] Radar settings - mostly alphabetized now with common options listed at top
* [REF] update copyright notice

## 55615 2022_07_16
* [ADD] NWS Radar Mosaic - play icon toggles to stop and add ability to stop animation via button
* [ADD] Nexrad radar long press - shorten verbiage

## 55614 2022_07_15
* [FIX] attempted fix for screen recorder in RecordingSession by adding delay for getMediaProjection
* [FIX] partial - For NHC central pacific storms

## 55613 2022_07_15
* [REF] continue moving content out of MyApp and into UIPreferences and others
* [REF] ObjectImagesCollection - remove the need to call init func

## 55612 2022_07_15
* [REF] continue moving content out of MyApp and into UIPreferences - *fav (favorites for misc activities)

## 55611 2022_07_15
* [REF] continue moving content out of MyApp and into GlobalVariables and NotificationPreferences

## 55610 2022_07_14
* [ADD] Switch to ViewPager2 and use FragmentStateAdapter
* [REF] continue moving content out of MyApp and into GlobalVariables and RadarPreferences

## 55609 2022_07_14
* [REF] continue moving content out of MyApp and into GlobalVariables and RadarPreferences

## 55608 2022_07_14
* [REF] complete task "deprecate SettingsPlaylistAutodownloadActivity" by doing the following
  - remove UtilityPlayListAutoDownload
  - remove DownloadPlaylistService
  - remove DownloadPlaylistService from manifest
* [ADD] Option to use new NWS Radar Mosaic (restart required). This is a temporary option as it appears likely the default mosaic will switch to NWS (away from AWC) later this year
* [REF] move patterns in RegExp that are only used in one class back into those classes
* [REF] Change Level2 data site back to nomads after maintenance in Apr 2022
* [ADD] radar.RadarGeometry and move items from MyApplication to it (ongoing)
* [REF] remove MyApplication.radarHwEnh, git rm ./app/src/main/res/raw/hw (option has been gone for some time)
* [REF] move numerous URLs and other val String into GlobalVariables (myapp is too big)

## 55607 2022_07_05
* [ADD] framework for new NWS Mosaic (to replace AWC later this year), main menu, widget, homescreen widget
* [FIX] NHC notifications will now alert for ATL or EPAC depending on which is selected
* [ADD] In the US Alert viewer, show warning types and states in sorted manner

## 55606 2022_07_04
* [ADD] refine labels in Settings -> UI
* [ADD] remove option "Reduce size of tile images" TILE_IMAGE_DOWNSIZE
* [ADD] remove option "Show VR button on main screen" VR_BUTTON
* [ADD] Settings -> UI alphabetize

## 55605 2022_07_04
* [REF] UtilityNws -> UtilityForecastIcon, ForecastIcon

## 55604 2022_07_01
* [REF] following upgrades in build.gradle
```
-        targetSdkVersion 31
+        targetSdkVersion 32

-    implementation "com.squareup.okhttp3:okhttp:4.9.3"
+    implementation "com.squareup.okhttp3:okhttp:4.10.0"

-    compileSdkVersion 31
+    compileSdkVersion 32
```
## 55603 2022_06_28
* [FIX] terminal radar TICH (Wichita, KS) was not properly coded (was TICT), it is now available for use

## 55602 2022_06_10
* [FIX] prevent nexrad radar from showing stale SWO data (earlier fix was not good)

## 55601 2022_06_08
* [FIX] prevent nexrad radar from showing stale SWO data
* software updates as follows
```
-    implementation 'com.google.android.material:material:1.6.0'
+    implementation 'com.google.android.material:material:1.6.1'

-        classpath 'com.android.tools.build:gradle:7.2.0'
+        classpath 'com.android.tools.build:gradle:7.2.1'
```

## 55600 2022_05_13
* SPC Meso additions
  - under Thermodynamics add "Skew-T Maps" skewt
  - under "Winter Weather" add "Winter Skew-T Maps" skewt-winter
* In addition to Android Studio update (and the various updates it brings along), update the following:
```
-    implementation 'com.google.android.material:material:1.5.0'
+    implementation 'com.google.android.material:material:1.6.0'

-    implementation 'androidx.media:media:1.5.0'
+    implementation 'androidx.media:media:1.6.0'
```

## 55599 2022_04_06
* [FIX] more robustness for Hourly using old NWS API

## 55598 2022_04_01
* [REF] whitespace cleanup particularly at bottom of file, not consistent
* [ADD] Nexrad Level2: in response to 24+ hr maint on 2022-04-19 to nomands, change URL to backup
  - [https://www.weather.gov/media/notification/pdf2/scn22-35_nomads_outage_apr_aaa.pdf](https://www.weather.gov/media/notification/pdf2/scn22-35_nomads_outage_apr_aaa.pdf)
  - A [reminder](https://gitlab.com/joshua.tee/wxl23/-/blob/master/doc/FAQ.md#why-is-level-2-radar-not-the-default) on Level 2 support within wX


## 55597 2022_03_24
* [REF] whitespace cleanup particularly at bottom of file, not consistent
* coroutine update
```
-    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2'
-    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2'
+    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0'
+    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0'
```

## 55596 2022_03_24
* [ADD] access to settings via hourly in case app won't load (can access via 4x4 blue widget -> hourly -> settings)

## 55595 2022_03_20
* [ADD] update about copyright and remove link to wxl23 ios version
* [ADD] SPC Meso - new "layer" county boundaries. Turn on from submenu -> Layers (NOTE: the state line boundary provided for the Radar Mosaic does not quite match up with the county layer. This can be observed from the SPC Website as well)
* [ADD] SPC Meso - new param: "Hodograph Map", access from "Wind Shear" submenu
* [ADD] The default hourly data provided (the new NWS API) does not reliably return results on the first download, added a retry mechanism.

## 55594 2022_03_06
* NWS is deprecating this website on 3/22 (substitute NWS observation point, etc): https://www.wrh.noaa.gov/mesowest/timeseries.php?sid=KCAR
  in favor of: https://www.weather.gov/wrh/timeseries?site=KCAR
  Thus updated the activity accessed through the "MISC" tab to reflect this

## 55593 2022_03_05
* [FIX] GOES GLM (lightning) animation was not working

## 55592 2022_02_26
* [ADD] (main screen) if closest observation point has data older then 2 hours, show data for the 2nd closest obs point

## 55590 2022_02_23
* [FIX] prune the list of Observation points by removing 57 sites that had not updated in the past 28 days
        This pruning will occur more frequently in the future to avoid a bad user experience
        In the future as the new NWS API stabilizes there might be a less manual (but still performant) way to handle this
* [ADD] software upgrades that wX uses (in this case to just keep the linter happy)
```
-    implementation 'androidx.preference:preference:1.2.0'
+    implementation 'androidx.preference:preference-ktx:1.2.0'
```
## 55589 2022_02_23
* [ADD] software upgrades that wX uses
```
-    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0'
-    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.6'
+    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2'
+    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2'

-    implementation 'com.google.android.material:material:1.4.0'
+    implementation 'com.google.android.material:material:1.5.0'

-    implementation 'androidx.media:media:1.4.3'
-    implementation 'androidx.preference:preference:1.1.1'

+    implementation 'androidx.media:media:1.5.0'
+    implementation 'androidx.preference:preference:1.2.0'

-    implementation "com.squareup.okhttp3:okhttp:4.9.2"
+    implementation "com.squareup.okhttp3:okhttp:4.9.3"
```
## 55588 2022_02_21
* [ADD] disable new super-res products as NWS has not fully reployed
* [FIX] remove the following weather obs point in `stations_us4.txt` and `us_metar3.txt` as user reported it has not updated since Jan 26
```
IL,ROMEOVILLE/CHI  ,KLOT
KLOT 41.6 -88.1
```

## 55587 2022_02_12
* [ADD] nexrad: force 2153/2154 to use Kotlin and avoid native since we shouldn't need to use native anymore
* [REF] native c code format cleanup

## 55586 2022_02_12 (release should not be used for production yet due to N0B/N0G integration)
* [ADD] work in native C code (via JNI) to support super-res products

## 55585 2022_02_10 (release should not be used for production yet due to N0B/N0G integration)
* [ADD] NXB and NXG framework, Level3 super-res
  - https://www.weather.gov/media/notification/pdf2/scn21-96_sbn_super-res.pdf
  - only at KRAX so far https://tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.00n1b/ missing lowest tilt
  - changes in the following to accommodate:
	modified:   app/src/main/java/joshuatee/wx/ColorPalettes.kt
	modified:   app/src/main/java/joshuatee/wx/GlobalDictionaries.kt
	modified:   app/src/main/java/joshuatee/wx/radar/ObjectOglRadarBuffers.kt
	modified:   app/src/main/java/joshuatee/wx/radar/UtilityWXOGLPerf.kt
	modified:   app/src/main/java/joshuatee/wx/radar/WXGLNexrad.kt
	modified:   app/src/main/java/joshuatee/wx/radar/WXGLNexradLevel3.kt
	modified:   app/src/main/java/joshuatee/wx/radar/WXGLRadarActivity.kt
	modified:   app/src/main/java/joshuatee/wx/radar/WXGLRadarActivityMultiPane.kt
	modified:   app/src/main/jni/decode8BitAndGenRadials.c
	modified:   app/src/main/res/menu/uswxoglradar.xml
	modified:   app/src/main/res/menu/uswxoglradarmultipane.xml
	modified:   app/src/main/res/values/strings.xml


## 55584 2022_01_31
* [ADD] In Settings -> UI -> Navdrawer config, have top arrow respond in the same way that bottom arrow does when pressed
* [ADD] In Settings -> about, add navdrawer token string to assist in troubleshooting
* [ADD] com.android.tools.build:gradle:7.0.4 -> com.android.tools.build:gradle:7.1.0
* [FIX] remove observation point KSTF (Starkville, MS) as it's impacting users.
* [FIX] remove decomissioned text products
  - "mimpac: Marine Weather disc for N PAC Ocean"
  - "mimatn: Marine disc for N Atlantic Ocean"

## 55582 2022_01_15
* [ADD] SPC Meso in "Multi-Parameter Fields" add "Bulk Shear - Sfc-3km / Sfc-3km MLCAPE"
* [FIX] SPC Meso in "Upper Air" change ordering for "Sfc Frontogenesis" to match SPC website
* [FIX] Creating desktop shortcuts was not working on Android version 12
* [ADD] CONUS sector to NAM in NCEP Models

## 55581 2021_11_27
* [ADD] NDK 23 -> 23b
* [ADD] first build with Mac M1
* [ADD] switch to non-experimental WPC winter weather forecasts day 4-7

## 55580 2021_11_11
* release

## 55578 2021_11_01
* [REF] move imagemap xml file to multiple files to remove lint errors
* [REF] "com.squareup.okhttp3:okhttp:4.9.1" upgrade to "com.squareup.okhttp3:okhttp:4.9.2"
* [REF] changes in app/src/main/res/values/dimens.xml to reduce lint
* [REF] changes in app/src/main/java/joshuatee/wx/settings/Location.kt to reduce lint

## 55576 2021_10_31
* [REF] misc refactor/lint

## 55575 2021_10_30
* [FIX] remove unprintable chars in `app/src/main/res/raw/gaz_counties_national.txt`
* [REF] remove the following unused resources
```
	deleted:    app/src/main/res/drawable/ic_access_alarm_24dp.xml
	deleted:    app/src/main/res/drawable/ic_alarm_add_24dp.xml
	deleted:    app/src/main/res/drawable/ic_flash_on_24dp_black.xml
	deleted:    app/src/main/res/drawable/ic_info_outline_24dp_black.xml
	
	deleted:    app/src/main/res/mipmap-anydpi-v26/ic_launcher_new_round.xml
	deleted:    app/src/main/res/mipmap-hdpi/ic_launcher_new_round.png
	deleted:    app/src/main/res/mipmap-mdpi/ic_launcher_new_round.png
	deleted:    app/src/main/res/mipmap-xhdpi/ic_launcher_new_round.png
	deleted:    app/src/main/res/mipmap-xxhdpi/ic_launcher_new_round.png
	deleted:    app/src/main/res/mipmap-xxxhdpi/ic_launcher_new_round.png
```
## 55574 2021_10_29
* [ADD] in nexrad long press show how far away nearest observation point is
* [ADD] National Images - add "_conus" to end of filename for SNOW/ICE Day1-3 for better graphic
* [ADD] SPC HRRR - add back SCP/STP param
* [REF] remove SettingsPlaylistActivity.kt

## 55572 2021_10_26
* [REF] Lint fix especially move aware from hardcoded text in layout/menu
* [REF] Lint fix - for menu items change from always to ifRoom if it won't impact anything

## 55571 2021_10_23
* [FIX] GOES Viewer, eep Eastern East Pacific image was not working after NOAA changed image resolution
* [ADD] implementation 'androidx.media:media:1.4.2' -> implementation 'androidx.media:media:1.4.3'
* [REF] Lint fix especially move aware from hardcoded text in layout/menu
* [REF] remove external links in Color Palette editor

## 55570 2021_10_22
* [FIX] wx.kt nav draw lightning entry was not correct, now matches misc frag

## 55569 2021_10_21
* release to fdroid (tag)

## 55568 2021_10_21
* [FIX] NWS Icons not working after NWS html change

## 55567 2021_10_10
* [FIX] NHC - ordering of images was not consistent, implement fully parallel downloads
* [REF] ObjectPopupMessage instead of static method in UtilityUI

## 55566 2021_10_07
* Add additional GOES products FireTemperature, Dust, GLM
* Move ChangeLog and FAQ to Gitlab
* Settings -> About, open browser when viewing FAQ or ChangeLog

```
55565 2021_10_03    [REF] misc
55564 2021_10_02    [REF] migrate all to Future* (models/radar/LocationFragment) (continued)
                    [REF] misc lint
55563 2021_10_01    [ADD] Provide more data in Alert detail screen (vtec, storm motion, headline, max hail/wind, etc)
                    [REF] change override fun onClick(v: View) { myClickListener!!.onItemClick(adapterPosition) } to
                          override fun onClick(v: View) { myClickListener!!.onItemClick(layoutPosition) }
                          in ViewHolders per lint
55562 2021_09_28    [REF] convert multiple activities to use Future*
                    [FIX] fix icon size on main window navigation drawer if using that option
55561 2021_09_22    [REF] convert multiple activities to use Future*
55560 2021_09_22    [REF] convert multiple activities to use Future*
55559 2021_09_22    [FIX] remove CLI "Daily Climate Report" from Local text products since NWS no longer provides WFO to site mapping
                    [ADD] per https://developer.android.com/about/versions/12/approximate-location
                    if you request ACCESS_FINE_LOCATION you must also request ACCESS_COARSE_LOCATION
                    [REF] LsrByWfoActivity refactor to use Future* and parallel download threads
                    [REF] SpcMcdWatchShowActivity to use Future*
55558 2021_09_21    [ADD] classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21" to
                          classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31"

                          implementation 'androidx.media:media:1.4.0' to
                          implementation 'androidx.media:media:1.4.2'

                    -    api "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.5.21"
                    -    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1' // was 1.3.6
                    +    api "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.5.31"
                    +    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0'

                    [FIX] comment out dexOptions in build.gradle (obsolete/ignored)
                    [REF] other misc lint fix

                    -    compileSdkVersion 30
                    -    buildToolsVersion '30.0.3'
                    +    compileSdkVersion 31
                    +    buildToolsVersion '31.0.0'

                    -        targetSdkVersion 30
                    +        targetSdkVersion 31

                    Android NDK android-ndk-r22 -> android-ndk-r23

55557 2021_09_18  tag  [FIX] add to UtilityMetarConditions "Patches Of Fog"
                    [ADD] classpath 'com.android.tools.build:gradle:7.0.1' to classpath 'com.android.tools.build:gradle:7.0.2'
55556 2021_08_15    [FIX] nav drawer was not respecting new lighting GOES GLM option
55555 2021_08_15    [FIX] nav draw hide items was not working
                    [ADD] more work on GLM
55554 2021_08_13    [ADD] settings->UI "Use GOES GLM for lightning" option to control lightning activity in misc tab
55553 2021_08_12    [FIX] NCEP - rename WW3 to GFS-WAVE and add additional sectors/products
                    [FIX] NCEP - remove params that aren't working
                    [FIX] NCEP - correct 2 sectors in ESTOFS and sort list
                    [FIX] SPC SREF - navigation drawer colors were not matching
                    [ADD] Adhoc forecast (long press nexrad) - add multiple download threads to speed up data display
                    [ADD] US Alerts - add multiple download threads to speed up data display
                    [ADD] Spc Storm Reports - add multiple download threads to speed up data display
                    [FIX] url had changed in OPC for Alaska/Arctic Analysis Latest SST/Ice Edge Analysis
                    [ADD] OPC - add 72 hour charts for most products
                    [ADD] OPC - add lightning strike density - 15min for multiple sectors (bottom of list)
                    [ADD] "org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10" -> "org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21"
                    [FIX] National Images, revise Labels for WPC Fronts so that start time is at left in case of truncation of header text
                    [FIX] remove 4 outdated images in National Images
                            "https://www.weather.gov/images/cle/ICE/dist9_concentration.jpg",
                            "https://www.weather.gov/images/cle/ICE/dist9_thickness.jpg",
                            "https://www.weather.gov/images/cle/ICE/egg_west.jpg",
                            "https://www.weather.gov/images/cle/ICE/egg_east.jpg",
55552 2021_08_02    [FIX] Aviation images don't work in Nation Images
                    [FIX] more work on SPC HRRR
55551 2021_08_02    [ADD] org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.5.0 -> 1.5.10
                          com.google.android.material:material:1.3.0 -> 1.4.0
                            this required adding "!!" after notification builders in UtilityNotification.kt (needs testing as emulator was not sufficient)
                    [FIX] SPC HRRR is now working again
                    [ADD] NSSL WRF_3KM has been discontinued by the upstream provider https://cams.nssl.noaa.gov/
55550 2021_08_01    [ADD] update ESRL HRRR/RAP after NOAA changes
                    [ADD] androidx.recyclerview:recyclerview:1.2.0 -> 1.2.1
                          androidx.media:media:1.3.1 -> 1.4.0
55549 2021_07_18    [FIX] WPC Forecast chart day1-3 URL has changed
                    [ADD] NAMER domain to RAP Model in NCEP
                    [ADD] Alask domain to HRRR Model in NCEP
                    [ADD] NAMER domain to RAP Model in NCEP
                    [ADD] Alask domain to HRRR Model in NCEP
                    [ADD] Accumulated Maximum Updraft Helicity (accu_max_updraft_hlcy) to HRRR model (NCEP)
                    [ADD] prob_cref_40dbz and prob_max_hlcy_75 to HREF model (NCEP)
55548 2021_07_16    [ADD] refactor to use Future*, more threads in various activities
55547 2021_07_15    [ADD] more download threads in SevereDashboard
                    [ADD] add Future* classes to mimic desktop ports, test in Hourly
                    [ADD] NHC Storm - cloud icon for GOES imagery of storm
55546 2021_06_07    [ADD] NCEP MAG (main NWS production model display) has replaced HRW-NMMB with HRW-FV3. This is accessible via MISC tab, top left corner
55545 2021_05_20    [FIX] SPC Thunderstorm outlook broke after NWS change (possible move to AWS?)
55544 2021_05_16    [ADD] NOAA has made changes to GLCFS https://www.glerl.noaa.gov/res/glcfs which required changes to wX
                    [FIX] Hourly graph was not clearing old data on reload
55543 2021_05_14    [REF] misc lint 'B'.toByte() -> 'B'.code.toByte()
                    [REF] remove unused resources related to canada radar removal
                    [REF] 'com.android.tools.build:gradle:4.2.0' -> 'com.android.tools.build:gradle:4.2.1'
55542 2021_05_13    [REF] utilUs cleanup, toUpperCase -> uppercase per link
55541 2021_05_09    [FIX] SPC SREF was not showing old runs after SPC made a minor change to their HTML code
                    [REF] refactor getClosestRadar in CapAlert and ObjectWarning
55540 2021_05_05    [ADD] Android Studio 4.2 update incl various gradle upgrades, etc (below)
                    [REF] WFO/WPC Text toUpperCase -> uppercase per link
                    [FIX] remove Canada radar (and supporting files) as EC no longer provides static images
    gradle-6.5-all.zip -> gradle-6.7.1-all.zip
    api "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.4.31" -> api "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.5.0"
    classpath 'com.android.tools.build:gradle:4.1.2' -> classpath 'com.android.tools.build:gradle:4.2.0'
    implementation 'androidx.media:media:1.2.1' -> implementation 'androidx.media:media:1.3.1'
    implementation 'androidx.recyclerview:recyclerview:1.1.0' -> implementation 'androidx.recyclerview:recyclerview:1.2.0'
55539 2021_05_01    [FIX] US Tornado notification - check vtec for old alerts before notifying
                    [FIX] SevereWarning - remove unused code that was causing a crash in SevereDashboard when warning had no vtec
                    [FIX] SevereWarning - remove unused code in general
55538 2021_04_07    [FIX] add try for java.nio.BufferOverflowException: joshuatee.wx.radar.UtilityWXOGLPerf.colorGen (UtilityWXOGLPerf.kt:589)
                    [REF] readability
55537 2021_04_06    [ADD] ObjectWarning
55536 2021_04_04    [FIX] settings->homescreen, force web option to submenu as it broke when "icons evenly spaced" was enabled
                    [FIX] fix time translation in old API hourly
                    [FIX] SPC Meso - the following image products were not working correctly "temp_chg" "dwpt_chg" "mixr_chg" "thte_chg"
55535 2021_03_14    [ADD] additional layout changes like the last revision in the following files:
                        res/layout/activity_models_* (except HREF)
                        use relative color xml files for icons in ObjectCardAlertSummaryItem
                        SettingsLocationGenericActivity - add logic for search drop down color on black theme
                        Settings notifications dialogs - additional them for dialog
55534 2021_03_14    [ADD] activity_image_show_navdrawer.xml, activity_linear_layout_show_navdrawer_bottom_toolbar.xml
                          activity_spcmeso.xml, activity_linear_layout_show_navdrawer.xml
                            change ListView android:background="@color/primary_dark_blue"
                            to android:background="?attr/colorPrimary" in support of allBlack theme, minor impact to other themes
55533 2021_03_10    [ADD] option to not use new NWS API for hourly data
55532 2021_03_10    [FIX] some forecasts icons during generation were showing "0%" instead of nothing
                    [ADD] option to not use new NWS API for hourly data - WIP
                    [ADD] all black theme (no gray) - WIP
55531 2021_03_04    [REF] remove ObjectImpactGraphic.kt and other files no longer needed
                    [REF] lint
                    [ADD] placeholders for Android 12 (SDK31) testing
                    [ADD] upgrade NDK to r22 from r21d
                    [ADD] add 2nd flag for pending intents FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE (per Android 12 API 31 need )
55530 2021_03_03    [ADD] AK/Hawaii/Carib sectors for AWC Radar Mosaic
                    [REF] remove warning impact and old mosaic code no longer needed
                    [REF] various formatting/code cleanup
                    [FIX] Radar Mosaic widget when tapping on image was not opening correct mosaic activity
55529 2021_03_02    [FIX] for target SDK 30, widgets with images not working on Android 11 devices. Needed manifest additions queries/intent
55527 2021_03_02    [FIX] change compile target SDK from 30 to 29, looks like latest SDK breaks image based widgets for those running latest Android
55526 2021_03_02    [FIX] prevent location that is not supported (such as over water) from crashing
                    [REF] various code formatting cleanup
55525 2021_02_27    [FIX] lint
                    [FIX] crash in severedash when tapping on alert
55524 2021_02_26    [FIX] UtilityWXOGL, CapAlert, ObjectAlertDetail, UtilityVtec.kt to make use of the new API URL for alerts starting with urn:oid
55523 2021_02_26    [FIX] add bounds checks in utilUS and code cleanup
55522 2021_02_25    [FIX] NWS has changed URLs for some alerts which broke display in some cases
55521 2021_02_24    [ADD] more work for icon generation 7day old api
55520 2021_02_24    [FIX] make AWC radar mosaic the only option for radar mosaic as NWS has removed a timeframe for the traditional radar mosaic
                    [FIX] make the older NWS API the default for 7 day forecast
55519 2021_02_20    [FIX] legacy large widget had icons missing, they are now there but colors are a mix of white/gray
55518 2021_02_18    [FIX] 7 day widget when using old NWS API
55517 2021_02_18    [ADD] framework for using old 7 day forecast data (nasty 2014 code), toggle in settings -> UI
55516 2021_02_18    [ADD] don't use accept header for NWS 7day similar to iOS port
55515 2021_02_17    [FIX] web widget tweak
55514 2021_02_17    [FIX] headers excluded for XML
55513 2021_02_17    [ADD] Accept headers and correct in one spot
                    [ADD] change URL and use ObjectCard for webview on main screen
                    [ADD] tweak webview display
55512 2021_02_17    [ADD] upgrade org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8 to 1.4.1
                    [ADD] experimental Web based 7 day card for homescreen for those impacted by NWS API flakiness
55511 2021_02_07    [ADD] misc lint and deprecations (Handler() in nexrad)
                    [ADD] SPC Meso - move VTP Violent Tornado from Beta into Composite Indices to match SPC website
                    [ADD] SPC Meso - add Tornadic 0-1km EHI to Beta to match SPC website
                    [ADD] SPC Meso - add Tornadic Tilting & Stretching to Beta to match SPC website
                    [ADD] SPC Meso - remove Enhanced EHI from Beta as it's no longer present on the SPC website
                    [ADD] update implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7' to 1.3.8
                    [ADD] update api "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.4.21" to 1.4.30
                    [ADD] update implementation 'com.google.android.material:material:1.2.1' to 1.3.0
55510 2021_02_05    [REF] remove kotlin-android-extensions in build.gradle
                    [ADD] minor correction in SPC Tstorm URL
55509 2021_01_31    [ADD] start removing import kotlinx.android.synthetic.main.activity_hourly.*, switch to findViewById (not interested in View Binding at this time)
55508 2021_01_30    [FIX] misc lint (redundant SAM constructor)
                    [ADD] update "com.squareup.okhttp3:okhttp:4.6.0" to "com.squareup.okhttp3:okhttp:4.9.0"
55507 2021_01_23    [ADD] upgrade ndk from r21c to r21d
                    [FIX] in location - edit, help text for notifications has spelling mistake for notifications
                    [FIX] remove "pmdhmd: Model Diagnostics Discussion" from National Text Products as the NWS no longer provides this product
                    [FIX] corrected URLs for 2 Space Weather images in National Images
                    [FIX] in nexrad, long press and select top two entries "miles from location", etc - should not do anything (user reported bug)
                    [ADD] change compileSdkVersion from 29 to 30
                    [ADD] change buildToolsVersion from 29.0.3 to 30.0.3
                    [ADD] change targetSdkVersion from 29 to 30
                    [ADD] update androidx.media:media:1.2.0 to androidx.media:media:1.2.1
                    [FIX] misc lint (redundant SAM constructor)
                    [FIX] USAlerts - remove warning impact graphics as NWS no longer has a public interface to this data (that I could find)
55506 2020_08_24    [REF] com.google.android.material:material:1.2.0 (from 1.1.0) org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.0 (from 1.3.72), and others
                    [FIX] give build process more memory
55505 2020_07_10    [FIX] NHC storm graphics not working for storms IDs over 5
55504 2020_06_24    [FIX] icon for WFO text notif
                    [ADD] update kotlin plugin to 1.3.72-release-Studio4.0-5
55503 2020_06_12    [ADD] GOES products DayCloudPhase NightMicrophysics, see https://www.star.nesdis.noaa.gov/GOES/index.php for description
                    [FIX] share legacy bitmap from nexrad single pane when level3 text products like storm motion are enabled
                    [FIX] in settingsNavDrawer on exit restart app regardless
55502 2020_06_11    [ADD] storm reports show filter in header text
                    [REF] misc
                    [FIX] if GPS is on but location marker is not, GPS circle will not have dot in center
                    [ADD] WIP - SettingsNavDrawerActivity - make items in main nav drawer be configurable (to hide)
55501 2020_06_11    [ADD] if using nav drawer on main screen, with the top items in dark background you can now tap on anywhere in the row even blank space to the right of the text to activate the item
55500 2020_06_10    try #2 for all activities in 498
55499 2020_06_10    try #2 for SPC SWO
55498 2020_06_10    [FIX] (did not work) remove HTML formatting for SPC SWO share text/WFO Text/Text Screen/WPC Text (Utility.fromHtml(html) to html)
55497 2020_06_08    [FIX] MCD/MPD/WAT viewer radar icon was not working for watch
55496 2020_06_08    [REF] refactor to some code added earlier today
55495 2020_06_08    [ADD] for MCD/WAT/MPD/Alert - don't include tdwr in closest radar computation
55494 2020_06_08    [ADD] for MCD/WAT/MPD, use center of polygon to detect radar site
                    [ADD] in severe dashboard use better radar site selection when tapping on radar icon for warning
55493 2020_06_08    [ADD] show radar icon in SPC MCD, WPC MPD, and SPC Watch (not optimal radar selection but close, will be improved)
55492 2020_06_08    [ADD] show radar icon in US Alerts if polygon present
55491 2020_06_08    [FIX] NHC notifications were not sending at every update interval
55490 2020_06_07    [FIX] NHC notifications were not sending at every update interval
                    [ADD] NHC Storm detail additional graphic for flash flood risk
55489 2020_06_07    [REF] misc
                    [ADD] org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.71 to org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.72
                    [ADD] onrestart in NHC
                    [ADD] In Image Viewer move custom title to subtitle to fit more text
                    [ADD] additional local wfo text products 'HLS', 'MWW', 'CFW', 'FFA'
55488 2020_06_01    [REF] misc
                    [ADD] additional NHC text products that require fixed width
                    [ADD] SPC HREF 24-hr STP Calibrated: Tornado
                    [ADD] 1-hr HREF Calibrated: Thunder
                    [ADD] 24-hr HREF Calibrated: Thunder
                    [ADD] settings -> Color - make background all black
55487 2020_05_30    [ADD] increase font size in settings main screen
55486 2020_05_30    [ADD] move "delete old radar files" from Settings into Settings -> About. You should never need this command.
55485 2020_05_30    [ADD] NHC text products MIATWSAT / EP are now fixed-width font with newlines not stripped
                    [ADD] new SPC Meso param SR Helicity - Sfc-500m (srh5)
                    [ADD] new SPC Meso param Sgfnt Tornado (0-500m SRH) (stpc5)
                    [ADD] move all config options in settings (top level) into settings - UI
55484 2020_05_30    [REF] misc
                    [FIX] single pane radar was not at correct zoom level if position not remembered
55483 2020_05_29    [REF] misc
55482 2020_05_29    [REF] misc
                    [FIX] Spc hrrr/href no sector by default showing
                    [FIX] Spc hrrr/href/sref default model time was not optimal
                    [REF] rationalize fav management, move to common method for setup starting with wpc text
55481 2020_05_28    [ADD] Android Studio update to 4.0 with associated gradle updates
                    [ADD] update ndk from r21 to r21c
55477 2020_05_28    [REF] misc
                    [FIX] one typo in help - thanks to Travis for pointing out
                    [ADD] more verbose text prod labels for some canadian text products
55476 2020_05_27    [REF] misc
                    [REF] remove roaming location feature
                    [FIX] implement separate timer for SevereDashboard and Radar. If warnings disabled they can essentially be overriden
                    by going into the radar activity before severe dashboard
                    [REF] rename layout IDs for spotter report card
55475 2020_05_26    [FIX] possible fix joshuatee.wx.notifications.UtilityNotificationSpc.sendMcdLocationNotifications (UtilityNotificationSpc.kt:182) java.lang.ArrayIndexOutOfBoundsException:
                    [REF] remove unused xml layout and drawables
                    [REF] remove useless relative layouts in xml: hourly, spc swo state, cardview_spotter
                    [REF] change menu option label from Pin to Add shortcut to home screen
                    [FIX] (needs test) Android share text does not preserve line breaks
55474 2020_05_25    [REF] misc
55473 2020_05_24    [FIX] add try/catch to at joshuatee.wx.radarcolorpalettes.ObjectColorPalette.putInt
55472 2020_05_24    [FIX] if in settings -> UI , toggle the new nav drawer and use the top arrow instead of the bottom, it will crash
55471 2020_05_24    [FIX] VR button missing needs themed color ic_mic_24dp
                    [FIX] add conditional TTS init in MyApp if notifications use TTS
55470 2020_05_24    [ADD] ref colormap "nws" which has been in iOS version for some time
                    [ADD] title/subtitle in SPC Meso
                    [FIX] nexrad: animate and then long press to switch site does not stop animate
                    [FIX] nexrad: enter single pane radar, zoom out, tape dual pane, exit - use upper right selector to switch site
                    [FIX] nexrad Kotlin hide map on getcontent
55469 2020_05_23    [REF] misc
55468 2020_05_23    [FIX] pre SDK26 vis crashes after change in prior release
55467 2020_05_23    [ADD] GOES, remove bottom toolbar
55466 2020_05_23    [REF] significant refactor in colormap
55465 2020_05_23    [FIX] wfo notification filter all white
                    [FIX] all white Canada legal disclaimer
                    [FIX] Twitter state selector
                    [ADD] obs site move to top toolbar, if no last used, use closest
55464 2020_05_23    [ADD] remove spinner from CA radar
55463 2020_05_22    [REF] misc
55462 2020_05_22    [FIX] SPC Meso swipe left/right when no favs
                    [FIX] redo SPC Meso fav management to not use labels
55461 2020_05_22    [REF] misc
55460 2020_05_22    [ADD] in model switch to invalidate for top toolbar textual items
55459 2020_05_22    [FIX] SPC Meso, show param instead of label due to space constraints
                    [FIX] SPC Meso, add back swipe left/right
                    [FIX] nexrad single pane, only show radar ID due to lack of room in top toolbar
55458 2020_05_21    [FIX] nexrad reset view in mapswitch
55457 2020_05_21    [ADD] WFOText, SPC SWO State, Sounding, LSR by WFO, SPC Meso, WPC Text, Nexrad to dialog
55456 2020_05_21    [REF] dialogue functions in model code using lambda (move to common ui code, pass context)
55455 2020_05_21    [FIX] ctrl j/k in models needs to be reversed
                    [REF] for various dictionaries (myapp) related to color pal, use Int instead of String for key. Impacts colormap editor stuff as well
55454 2020_05_21    [FIX] Ncep prior runs not showing up
55453 2020_05_21    [FIX] NCEP GFS change one sector to "WEST-ATL"
                    [FIX] NCEP models , after change model reset time to 1
                    [FIX] NCEP models does not show time pretty label after switch model or product
55452 2020_05_21    [FIX] NCEP models FABs need all white icons
                    [FIX] NCEP model swipe left/right not working
55451 2020_05_20    [FIX] correct obs icon on misc tab
                    [ADD] WIP - model rewrite (fixes)
                    [REF] colorpal, use Int instead of String for product code
55450 2020_05_20    [ADD] WIP - model rewrite
55449 2020_05_20    [ADD] WIP - model rewrite (not SPC SREF yet)
55448 2020_05_20    [FIX] revert changes in SPC HRRR/HREF/SREF
55447 2020_05_19    [ADD] allWhite theme - SPC HREF/SREF fix
                    [FIX] obs icon not correct in new nav drawer
55446 2020_05_19    [ADD] allWhite theme - Tts pause icon is yellow when activated
                    [ADD] allWhite theme - drawer background
                    [ADD] allWhite theme - Icons us alerts/severe dash
                    [ADD] allWhite theme - SPC HRRR fix
                    [FIX] Android warning share text formatting
55445 2020_05_19    [ADD] nhc storm 3 wide if tablet
                    [REF] utilNws icon
                    [ADD] allWhite theme - bottom sheet support
                    [ADD] allWhite theme - alert dialogue
55444 2020_05_18    [ADD] allWhite theme
55443 2020_05_18    [ADD] allWhite theme
                    [FIX] nexrad toolbar for all white theme
                    [FIX] LSR by WFO via SPC Storm reports formatting was corrected
55442 2020_05_18    [ADD] allWhite theme
                    [ADD] in multiple activities move playlist off main toolbar to submenu
55441 2020_05_18    [ADD] allWhite theme - wip
55440 2020_05_17    [ADD] NHC Storm - QPF graphic
                    [ADD] remove bottom toolbar in WPC Images and settings about
55439 2020_05_17    [ADD] Radar mosaic - remove bottom toolbars
55438 2020_05_17    [ADD] NHC Storm move to json data source per https://www.weather.gov/media/notification/scn20-25tropical_javascrpt.pdf
                    [ADD] remove un-needed toolbar options in NHC and NHC Storm and remove bottom toolbar
55437 2020_05_16    [FIX] disable WPC Fronts in multipane due to crash, lower pressure centers still show
55436 2020_05_16    [REF] misc
                    [ADD] Freeze Warning to WFO warning filter list
                    [ADD] NHC Storm use intent.getSerializableExtra("StormData") as ObjectNhcStormDetails
                    [ADD] NHC Storm - if select additional text products from submenu, invoke WPC Text viewer
                    [FIX] at joshuatee.wx.radar.UtilityRadarUI.addItemsToLongPress (UtilityRadarUI.kt:106)
                           out of bounds with insert extension
55435 2020_05_16    [REF] misc
                    [ADD] 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3' to 1.3.6 (and 2nd one)
55434 2020_05_15    [ADD] option in settings UI to have main screen nav drawer open from right side
                    [FIX] prevent leak in TTS for AudioPlayActivity
55433 2020_05_13    [ADD] onrestart to settings - about
                    [REF] delete UtilityHttp.java
                    [ADD] in Spotter activity remove phone number/email as they are not available on https://www.spotternetwork.org/ w/o member login
55432 2020_05_13    [REF] misc
55431 2020_05_12    [ADD] in radar settings if location dot follows GPS also keep screen on and perform refresh
55430 2020_05_11    [FIX] AWC radar mosaic sectors were off by one
                    [ADD] if notif interval set to 121, cancel all background processing (if Android 7.0 or higher - API 24)
                    [REF] mv AfdActivity.kt WfoTextActivity.kt
55429 2020_05_11    [REF] move WXGLDownload to static
55428 2020_05_09    [FIX] in httpInterceptor in myApp, close response within loop per bug report okhttp
55427 2020_05_09    [FIX] nexrad images via share not working
                    [ADD] if settings - notification - interval is set to 121 don't set service on program start
55426 2020_05_09    [REF] wip - utilDownload - move base get to utilNetworkIO
                    [FIX] android keyboard shortcut for stop animate not working
55425 2020_05_09    [ADD] migrate "com.squareup.okhttp3:okhttp:3.14.8"  to "com.squareup.okhttp3:okhttp:4.6.0"
55424 2020_05_09    [REF] remove legacy png for icons in folders like app/src/main/res/drawable-xhdpi
55423 2020_05_09    [REF] remove Obsolete SDK_INT Version Check
                    [REF] move values-v21/styles.xml to values/styles.xml
55422 2020_05_09    [ADD] In settings location, condense the number of lines, make the top line blue, indicate active alerts with text "*Alerts" at end of name
                    [REF] BREAKING MAJOR CHANGE move minSdkVersion 16 -> 21
                    [REF] move "com.squareup.okhttp3:okhttp:3.12.8" to "com.squareup.okhttp3:okhttp:3.14.8" (with a quick plan to move to 4.6 soon)
55421 2020_05_08    [FIX] TTS bug when starting voice in one activity and moving to another and starting a new voice (do not check ttsInit in initTts)
55420 2020_05_07    [ADD] test then backout update to minsdk to 21 and okhttp to 3.14.x / 4.6.0
                    [FIX] if radar center on location, text labels are not in the correct spot
55419 2020_05_06    [ADD] Java8 support for Java/Kotlin needed for upcoming okhttp update
55418 2020_05_06    [FIX] nexrad long press was not showing warnings if only SPS - add generic count method in utilVtec
                    [FIX] nexrad multipane remove cntrl - up arrow/down arrow as it's not working correctly
                    [ADD] NHC for homescreen (2/5 day ATL,EPAC,CPAC)
                    [ADD] GOES-17 cak / sea - central SE AK
55417 2020_05_05    [REF] misc
                    [ADD] nexrad long press - don't show warnings text entry if no warnings present
                    [ADD] NCEP MAG models - add Composite Radar Reflectivity into a number of models
55416 2020_04_30    [ADD] move initTts out of MyApp and into AudioPlayActivity
55415 2020_04_30    [FIX] nexrad controls keyboard zoom in/out in multipane - attempt #2
55414 2020_04_30    [REF] misc
                    [ADD] androidx.preference:preference:1.1.0 to 1.1.1
                    [FIX] revert as didn't work: [ADD] nexrad radar zoom-out/in keyboard controls switch from ctrl to alt (up/down arrows)
55412 2020_04_29    [ADD] nexrad radar zoom-out/in keyboard controls switch from ctrl to alt (up/down arrows)
                    [ADD] warning/watch stats in nav drawer header if configured
55411 2020_04_29    [REF] misc
                    [FIX] wxglradar - fix issue when switching via map to new location
55410 2020_04_28    [REF] ObjectIntent
55409 2020_04_28    [ADD] remove bottom toolbar to save real estate in lightning
                    [FIX] nexrad multipane keyboard zoom out/in needs scale factor tied to number of panes
                    [FIX] WFO text viewer - product by state formatting was not optimal
55408 2020_04_27    [ADD] WIP - new UI option (ObjectIntent optimizations)
                    [FIX] NCEP MAG GFS Composite Radar Reflectivity was not working
                    [ADD] remove bottom toolbar to save real estate in OPC/Goes Global/WPC Rainfall summary/Observations/SPC Compmap
                          SPC Convective Outlook summaries/SPC Fire summaries/SPC thunderstorm summaries/us alerts
55407 2020_04_27    [FIX] SPC Convective outlook - title is more descriptive when image shown alone
	 	            [ADD] theme support for new navbar
55406 2020_04_26    [FIX] storm reports - having location follow gps breaks the location marker
55405 2020_04_26    [FIX] remove location in save in US Alerts as OSM is not going to work anymore
                    [FIX] remove all services dependant on OSM: MCD location save, VR location save, fallback location search
55404 2020_04_26    [ADD] WIP - new UI option
55403 2020_04_26    [ADD] WIP - new UI option
55402 2020_04_26    [ADD] better formatting in NHC Storm detail
                    [ADD] onrestart to NHC Storm detail (refresh data when resume)
                    [ADD] some keyboard commands to multipane radar
55401 2020_04_25    [FIX] NHC EPAC storms were not showing
55400 2020_04_25    [REF] misc
                    [FIX] NCEP MAG HRRR animation was not working
                    [FIX] NCEP MAG - remove WW3-ENP WW3-WNA
55399 2020_04_25    [REF] misc
55398 2020_04_25    [REF] misc
55397 2020_04_19    [ADD] Android Studio/gradle update
                    [ADD] long press radar - only show selections if count is active ( like flutter )
                    [ADD] simplify settings radar verbiages
55396 2020_04_17    [REF] translate ExternalGeodeticCalculator to kotlin
55395 2020_04_16    [REF] misc for readability
                    [FIX] WIP TTS speech speed was not working (requires app restart after change)
                    [ADD] kotlin plugin 1.3.71 -> 1.3.72
55394 2020_04_13    [REF] misc for readability
55393 2020_04_12    [REF] misc for readability
                    [FIX] crash and off by one issue in radar icon in severe dashboard
                    [REF] remove LatLonStr
                    [FIX] Canada radar - if zoomed in on local site and then choose mosaic, reset zoom/position
                    [ADD] hourly - remove bottom toolbar, move share icon to top
                    [FIX] AFD - prior text products not formatting well
                    [ADD] enhance dynamic line size changes in nexrad radar for warning, watch, SWO
55392 2020_04_07    [REF] misc for readability
55391 2020_04_06    [FIX] remove line breaks in WPC Excessive rainfall text products
55390 2020_04_05    [REF] misc for readability
                    [ADD] nexrad radar - line size for wind barbs, storm tracks, and convective outlines now configurable
55389 2020_04_03    [REF] misc for readability
55388 2020_04_01    [REF] misc for readability
                    [ADD] severedashboard and uswarning - add radar/details button in warning cards
55387 2020_03_29    [REF] misc for readability
55386 2020_03_29    [REF] misc for readability
                    [FIX] java.lang.IndexOutOfBoundsException: at joshuatee.wx.radar.WXGLRender.constructWpcFronts (WXGLRender.kt:1187)
                    [FIX] kotlin.KotlinNullPointerException: at joshuatee.wx.fragments.LocationFragment$getRadar$1$5.invokeSuspend (LocationFragment.kt:463)
55385 2020_03_27    [REF] misc for readability
55384 2020_03_26    [REF] misc for readability - notifications dir
55383 2020_03_26    [REF] misc for readability
                    [FIX] typo - base spectrum width label changed to NSW - was NWS
55382 2020_03_25    [REF] make use of ObjectImageSummary in SPC Fire/tstorm
                    [REF] misc for readability
                    [ADD] monospace for certain national text products
55381 2020_03_24    [REF] misc for readability
                    [FIX] in nexrad with transparent toolbars, show toolbar background when map is shown
                    [ADD] minor padding change in current conditions card
                    [ADD] minor formatting changes in warning impact graphics
                    [ADD] kotlin plugin 1.3.70 -> 1.3.71
                    [FIX] crash in radar mosaic when radar warnings enabled under some circumstances
55380 2020_03_20    [REF] misc for readability
                    [ADD] minor formatting enhancement in spotter reports (light gray for description)
                    [ADD] In SevereDashboard, octagon icon main screen - you can now press and hold on a warning for a menu, menu allows one to show nexrad radar
55379 2020_03_18    [REF] misc for readability
                    [ADD] Canadian locations - have alerts act like US on front screen
                    [ADD] add save location button in adhoc location accessed via nexrad long press
                    [FIX] Canadian Warnings detailed formatting minor improvement
                    [REF] minor optimizations in MyApp
                    [REF] minor optimizations in SPC Storm reports
                    [ADD] playlist now uses WPCText instead of textviewer when viewing items
55378 2020_03_15    [REF] misc
                    [ADD] prp for future move to targetSdkVersion to 30 from 29 in build.gradle
                    [ADD] remove MPD summary link from WPC Text, users should go to severe dashboard
                    [ADD] in WPC Text, move add to playlist to submenu above add notification - similar to WFO text
                    [REF] remove WpcMpdShowSummaryActivity.kt
55377 2020_03_10    [ADD] forecast zone to settings -> About
55376 2020_03_07    [FIX] Canadian warnings formatting improvements
                    [FIX] more minor tweaks for issues after updating to MDC 1.1.0
                    [REF] misc cleanup/refactor
                    [FIX] nexrad radar, button titles were not changing when animating
55375 2020_03_05    [FIX] crash in severedashboard when attempting to share when MCDs are present
                    [FIX] playList onrestart was not working
                    [ADD] playList show more text in preview
55374 2020_03_04    [ADD] 'com.google.android.material:material:1.0.0' to 1.1.0
55373 2020_03_03    [ADD] gradle plugin update
                    [REF] misc lint
                    [FIX] playlist allows duplicate entry
                    [FIX] remove LocalBroadcastManager in playlist
                    [ADD] alerts to us alerts in main submenu
                    [ADD] framework for ObjectImageSummary
                    [ADD] 'com.google.android.material:material:1.0.0' to 1.1.0 (backed-out)
                    [ADD] "org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61" to 1.3.70
55372 2020_02_27    [FIX] Canadian hourly
                    [ADD] main menu change from Hourly to Hourly Forecast
                    [FIX] whitespace issue in some text products
55371 2020_02_25    [ADD] in UIPreferences use flutter/dart blue for highlight (14, 71, 161)
55370 2020_02_25    [ADD] android studio update to 3.6
                    [ADD] in UIPreferences use flutter/dart blue for highlight (88, 121, 169)
55369 2020_02_18    [FIX] text product notifications were dependent on "<BR>"
55368 2020_02_17    [FIX] attempt for hourly onrestart needs to reset graph
                    [FIX] text product notifications were dependent on "<BR>"
55367 2020_02_15    [ADD] remove help option from main menu in favor of offline documentation and FAQ
55366 2020_02_15    [ADD] for multipane SPC Meso/Models if in landscape have images side by side
55365 2020_02_15    [FIX] fire weather header
                    [ADD] SPC SWO for landscape ( 4 wide )
55364 2020_02_15    [FIX] settings -> about crash when hit share icon (set share text to about)
                    [REF] rename SpcFireOutlookActivity to SpcFireOutlookSummaryActivity
                    [ADD] dedicated SpcFireOutlookActivity activity to show graphic and text
                    [REF] objectNhc refactor
                    [ADD] enhance WPC Rainfall forecast, support landscape, use dedicate image/text viewer for each day
                    [ADD] SPC Summary support landscape and use generic LinearLayout code
55363 2020_02_14    [FIX] SpcSwoSummaryActivity - onrestart don't remove views until new images are downloaded
                    [ADD] WIP - not done - SPC Storm reports optimize for large landscape
                    [REF] remove redundant code in UtilityTimeSunMoon
                    [ADD] text product by state - temp and precip table, example RTPWI
                    [ADD] increase max text size in settings -> radar from 20 to 40
55362 2020_02_13    [ADD] SPC Thunder Storm outlook - multiple images per row (and tap on image to enter dedicated image viewer)
                    [ADD] SPC Fire weather outlook - multiple images per row
55361 2020_02_12    [FIX] locfrag radar warn/watch - check for activityReferenceWithNull != null before proceed
                    [FIX] OnSwipeTouchListener(java) check for nulls in onFling
55360 2020_02_10    [FIX] removeLineBreaks was changing newline to nothing instead of single space
55359 2020_02_09    [REF] lint
                    [ADD] CPC 3 month temp/precip outlook in WPC Images
                    [ADD] NHC change format to severe dashboard including tap on image to open in dedicated fashion
                    [ADD] re-architect Severe Dashboard image handling, if Landscape show 3 images per row
55358 2020_02_08    [ADD] substantial revamp of utilDownload, WFO text, national text with regards to data download and presentation
55357 2020_02_08    [REF] lint (utilDownload)
                    [ADD] substantial revamp of utilDownload, WFO text, national text with regards to data download and presentation
55356 2020_02_08    [REF] migrate off Web screenABModels
                    [FIX] use alternative sources for text products: WFO: AFD, SPC Convective Outlooks, Short Range disc
55355 2020_02_06    [FIX] hourly now has quotes around temp
                    [ADD] keyboard shortcut for location selector on main screen
55354 2020_02_06    [ADD] have WebView respect TextSize setting
55353 2020_02_05    [REF] misc refactor
                    [ADD] SpcMcdWatchShowActivity - if tablet in landscape change orientation of linearLayout
55352 2020_02_04    [FIX] settings -> radar -> text size, default value was changing
                    [ADD] settings -> UI -> text size - size of text in card changes as you move along slider
                    [FIX] don't show - show warnings - in radar long press unless warnings are enabled
                    [FIX] LocationFragment - don't pass context when querying radar time
55351 2020_02_04    [FIX] WPCImage widget had to have launch updated to use flags (would crash otherwise)
55350 2020_02_04    [FIX] update google doc links to point to published versions in settings -> about
                    [FIX] in SettingsLocationRecyclerViewActivity remove broadcastReceiver that is not needed
                    [REF] bottom sheet refactor to move more stuff into constructor
55349 2020_02_03    [REF] utilFav/SettingsHomeScreen refactoring
                    [REF] lint cleanup
55348 2020_02_02    [REF] utilFav/SettingsHomeScreen refactoring
55347 2020_02_01    [ADD] move to normal twitter w/o tokens
55346 2020_02_01    [REF] remove un-needed pref storage in util pref
                    [FIX] twitter activity wasn't going to state of current location
55345 2020_02_01    [REF] remove un-needed pref storage in util pref
55344 2020_02_01    [REF] remove un-needed pref storage in util pref
55343 2020_02_01    [ADD] in nexrad radar if show warnings show count in upper left, tap on count goes to severe dashboard - WIP
                    [FIX] non tst/tor/ffw warnings were not being downloaded outside of backgroundFetch
                    [REF] WIP - move off rid/wfo/sounding lat/lon/name currently in pref should be in objects (like flutter/iOS)
                    [REF] remove un-needed pref storage in util pref
55342 2020_01_31    [FIX] prelaunch report: at joshuatee.wx.models.UtilityModels.setSubtitleRestoreIMGXYZOOM(UtilityModels.kt:292)
                        java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
                    [FIX] prelaunch report: at joshuatee.wx.models.UtilityModelEsrlInputOutput.getAnimation(UtilityModelEsrlInputOutput.kt:185)
                        java.lang.IndexOutOfBoundsException: Invalid index 37, size is 37 a
55341 2020_01_30    [ADD] additional graphics for SPC Convective Outlook Day 2
55340 2020_01_30    [REF] use methods in WXGLNexrad to read/write radar info in pref
                    [FIX] FragmentPagerAdapter deprecated in ViewPagerAdapter.kt
                    [REF] lint cleanup
55339 2020_01_29    [FIX] on homescreen if more then one nexrad timestemp in cc box is not correct
55338 2020_01_28    [ADD] in settings -> location if choose Canada location via map return to location screen after tapping on map
                    [REF] various code cleanup
                    [ADD] minor visual enhancements in location edit
                    [ADD] keyboard shortcut for chromebook refresh button on main screen
                    [ADD] enable textual observations on main screen nexrad radar tied to location (not additional nexrad via settings -> homescreen)
55337 2020_01_28    [ADD] RadarFileGeometry (wip)
                    [FIX] add @Synchronized to readMetarData() in UtilityMetar - BackgroundFetch is using along wit main screen
55336 2020_01_27    [REF] lint cleanup
                    [ADD] fix radar legend pnd - LIFR was not pink
                    [ADD] RadarFileGeometry (wip)
                    [FIX] Obs not working multipane
55335 2020_01_27    [FIX] hardcore hourly graph background
55334 2020_01_26    [REF] various code cleanup and var rename for readability
                    [ADD] change Hourly graph to a black line
                    [ADD] code cleanup and comments
                    [ADD] UtilityMetar - start to use a persistent metar data var after reading in from file - WIP
                    [ADD] enhance radar obs to support multipane
55333 2020_01_25    [FIX] in hourly have text card request focus (chromebook)
                    [ADD] com.squareup.okhttp3:okhttp:3.12.3 -> com.squareup.okhttp3:okhttp:3.12.8
55332 2020_01_25    [FIX] In national text activity combine the first two subsections into one section as they are basically the same
                    [FIX] reconcile  utilPref2 for items that don't need to be set here - WIP
                    [REF] various variable camelCase/rename in MyApp
55331 2020_01_25    [FIX] on tablet reduce padding around location label on main screen
                    [ADD] CLI product in WFO (significant due to subsites per WFO)
55330 2020_01_25    [ADD] convert WFO Text viewer to use navigation drawer for products and submenu for actions
                    [ADD] add keyboard shortcuts for WFO Viewer - "ctrl-?" to see
55329 2020_01_24    [ADD] slightly decrease default text size on tablet
                    [ADD] RWR text product in WFO text viewer ( circle around i )
                    [ADD] in WFO text viewer do monospace for RTP and RWR
                    [ADD] usalerts tap image for dedicated image viewer
                    [ADD] change animation interval (default) from 15 to 8
                    [FIX] three PMD text products that were not working 30/90 day CPC, etc
                    [FIX] clarify that PMDTHR is Day 8-14 US Hazards Outlook
                    [ADD] https://www.wpc.ncep.noaa.gov/threats/threats.php US Day 3-7 Hazards Outlook
55328 2020_01_23    [REF] lint cleanup
                    [FIX] when radar center on GPS is enabled the x/y was getting goofed up, most apparent when cities shown
55327 2020_01_23    [FIX] onrestart wpc rainfall was not clearing views
                    [FIX] wpc rainfall discussion formatting
                    [ADD] update kbd shortcuts legend in radar
55326 2020_01_22    [FIX] bugfix in recent adds
55325 2020_01_22    [FIX] crash in wpc fronts - one off in array bounds
                    [ADD] KEYCODE_REFRESH main screen and in radar
                    [ADD] radar controls with arrow keys
55324 2020_01_22    [FIX] NCEP make GFS default
                    [ADD] keyCode diag in about -> wX
                    [ADD] ctrl-m for menu in main screen, radar, location edit
55323 2020_01_22    [FIX] for help on main screen and in radar use alt-? (chromeOS wants shift-?)
                    [ADD] change UI_ANIM_ICON_FRAMES from 6 to 10
                    [FIX] in radar keyboard shortcut for animation was not stopping
55322 2020_01_21    [FIX] add explicit sound=null for CC and radar notification
                    [ADD] remove setting wxoglSize in UtilPref2, change default to 13 and then 8 for tablet
                    [ADD] keyboard shortcuts ctrl-j / k in all model activities
55321 2020_01_21    [ADD] keyboard shortcuts in radar
                    [ADD] ctrl-? on main screen shows shortcuts
                    [FIX] remove mention of ROAMING in settings -> location -> edit unless feature is fixed
55320 2020_01_21    [FIX] change NCEP shortcut from N to M as Chrome OS owns N (new chrome window)
                    [FIX] change NHC shortcut from T to O as Chrome OS owns it
                    [FIX] remove shortcut for ctrl-W (warnings) (chomeOS owns it)
55319 2020_01_20    [ADD] keyboard shortcuts for dual/quad pane radar (ctrl-2 and ctrl-4)
                    [ADD] list of keyboard shortcuts in settings -> about
                    [FIX] change a few shortcuts to match iOS/swift port
                    [FIX] bottom sheet is not fully expanded in some screens (force expand)
55318 2020_01_20    [FIX] if radar alert is enabled for local alerts, if you tap on radar alert and hit back button it now takes to main screen
55317 2020_01_19    [REF] lint and code cleanup
                    [FIX] obs that show "XX Inches of Snow on Ground"
                    [ADD] SingleTextAdapterList used by ObjectRecyclerView now uses dynamic font size - nws obs, fav add/change
55316 2020_01_19    [FIX] spotter report label does not clear when single/double click - does for pan
                    [REF] better method names in radar text class
                    [FIX] nexrad raster was not showing on first load
55315 2020_01_19    [FIX] if edit current location name and return to main screen label was not updated
                    [FIX] add more vertical padding in object dialogue
                    [ADD] wpc fronts - only show if zoomed out
                    [ADD] make snackbar text size respect text size setting
55314 2020_01_18    [REF] various refactor
                    [FIX] remove currentLoc from Locfrag, not needed and was causing a bug
                    [ADD] more space in object diaglogue
                    [FIX] radar color palette text size
                    [FIX] hourly font size
55313 2020_01_18    [ADD] for tablet have default radar sizes be smaller: locdot, spotter, aviation dot
                    [ADD] playlist - when adding item, download it
                    [ADD] On main screen replace spinner with ObjectTextCard and ObjectDialogue
55312 2020_01_18    [FIX] some UI cards (spotter) such as playlist don't respect text size
                    [FIX] public interface OnTouchImageViewListener { (needs to stay despite lint)
55311 2020_01_18    [ADD] release notes button in settings -> about
                    [ADD] test code to see if tablet
                    [ADD] if tablet adjust tiles per row, NWS image size, text size to something more appropriate
55310 2020_01_18    [FIX] wpc front crash with stationary front in some circumstances
                    [FIX] wpc storm summary text product was not working
                    [FIX] lint cleanup
                    [ADD] hourly homescreen widget is now monospaced
                    [ADD] enhanced "about wX" with FAQ viewer and email developer button
                    [ADD] in location add, update lat/lon hint to indicate that you are not required to enter
55309 2020_01_17    [FIX] disable wpc fronts on main screen until more stable
55308 2020_01_17    [ADD] change wpc fronts to draw lines instead of polygons
                    [FIX] main screen current conditions text size was not correct after dynamic change support
55307 2020_01_16    [REF] convert from System.currentTimeMillis() to UtilityTime.currentTimeMillis()
                    [REF] prune comments
55306 2020_01_15    [ADD] android-ndk-r20b to android-ndk-r21
                    [ADD] convert settings location edit to use cityall instead of old DB so now 29k cities instead of 400
                    [ADD] formatting enhancement in USAlertsDetail
55305 2020_01_15    [ADD] update ext city doco, insert new cityall file with states
                    [ADD] use city ext database in settings -> location search function
55304 2020_01_14    [REF] lint cleanup
		            [FIX] WPC Fronts - add array bounds check in high/low
		            [FIX] WPC Fronts - handle case where LOW/HIGH spans 3 lines
		            [ADD] after adding new location automatically switch to it on main screen
		            [ADD] in Playlist download all when activity is started and onrestart, remove Download All icon
		                  from bottom toolbar
		            [FIX] cityall.txt, UtilityCities - change detroit to something that works
		            [ADD] wpc day 1-3 image + text - add to MISC tab and remove from WPC Text submenu
		            [FIX] Move the following from the National text product viewer submenu to the National Image viewer
		                  in the first section: "Significant Surface Low Tracks", "Low Tracks and Clusters",
55303 2020_01_11    [FIX] onrestart for spc conv sum was not working
                    [ADD] WPC Fronts support in homescreen nexrad and multipane
                    [ADD] DownloadTimer ( to be used in UtilityDownload* mcd/wat/warn/swo/spotter etc )
                    [ADD] DownloadTimer use for WPC Fronts, Wat, MCD, MPD
                    [ADD] add warn/wat data refresh if radars used on main screen now that timer is in place to prevent concurrent download
55302 2020_01_10    [ADD] reset download timers for warnings/wat/mcd/mpd etc if changes made in settings -> radar
55301 2020_01_10    [FIX] remove from global arrays: "qpfpfd: QPF Discussion" (no longer an active text product)
                    [REF] replace GlobalArrays.nwsTextProducts with UtilityWpcText.labels
55300 2020_01_09    [ADD] wpc mpd can tap on image to show imageviewer
                    [ADD] more help data for new WPC Fronts option
                    [ADD] onrestart in numerous activities
                    [ADD] when selecting a canadian city go back to edit screen and immediatelty save
                    [ADD] WPC Rainfall disc enhance
55299 2020_01_07    [FIX] wpc fronts - trof is now dashed line
55298 2020_01_06    [ADD] in settings homescreen and playlist close dialog after tapping on item
                    [FIX] WPC Fronts - cold front make 0,127,255 not 0,0,255
                    [ADD] hourly homescreen widget is now monospaced and uses day of week
55297 2020_01_06    [FIX] wpc fronts - few for loop bugs on cold/warm front creation
55296 2020_01_06    [FIX] decrease line width for wpc fronts to match watch/mcd
                    [FIX] wpc fronts - bug fix on loop math in render
55295 2020_01_05    [FIX] lint and wpc not updating on every radar change
                    [REF] add 2020 in copyright
55294 2020_01_05    [FIX] better font size transitions on main screen after changing in settings UI - part 1
                    [ADD] WPC Fronts framework for nexrad radar
55293 2019_12_30    [FIX] Fulldisk global viewer - remove first two images
55292 2019_12_29    [ADD] USWarn in Severe Dash and as homescreen option
55291 2019_12_29    [ADD] additional WPC Forecast maps in National Images and in homescreen
		            [FIX] for WPC images on homescreen, tapping on them now opens National images and display image tapped on
		            [FIX] National images, show label as subtitle since they are so long in certain cases
55290 2019_12_23    [REF] lint cleanup
                    [ADD] use 4bit colorpal init like flutter (add framework)
                    [FIX] retire colormap for 41/57/78/80
                    [REF] Cleanup in SevereDashboard
55289 2019_12_22    [ADD] update android-ndk-r20 to android-ndk-r20b
                    [ADD] update org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.50 to org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.3.61
                    [ADD] update org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.1 to org.jetbrains.kotlinx:kotlinx-coroutines-core:1.2.1
                    [ADD] update org.jetbrains.kotlinx:kotlinx-coroutines-android:1.1.1 to org.jetbrains.kotlinx:kotlinx-coroutines-android:1.1.1
                    [ADD] WFO warnings image to homescreen
                    [ADD] better obs data in us_metar3.txt stations_us4.txt via getObs.py
55288 2019_12_17    [FIX] anim for TZ0 was not working
55287 2019_12_17    [REF] lint and code cleanup
		            [FIX] radar mosaic was showing expired warnings
		            [ADD] TZ0 replaces TR0 in TDWR (16 to 256 bits for color)
55286 2019_12_03    [FIX] AWC mosaic animations not working for all products
                    [ADD] "Sandwich" to GOES activity
55285 2019_12_01    [FIX] Canada forecasts were showing null when no wind data, port code from iOS/Swift
55284 2019_12_01    [FIX] Canada text product FXCN01 was not working, not broken down into 4 text products east/west, d1-3 and d4-7
                    [FIX] Canada radar animation not working
                    [ADD] WPC Text - add CA text products to match iOS version
                    [ADD] obs map in obs site activity
55283 2019_11_29    [ADD] lint cleanup
55282 2019_11_29    [ADD] kotlin updates
                    [FIX] remove Sun/Moon data as upstream content provider website is no longer even defined in DNS
55281 2019_10_31    [FIX] San Fran text label not correct - 37.787239,-122.4581,805235
		            [ADD] SPC HREF update part 1
55280 2019_10_28    [FIX] CC notif duplicate caused by non constant ID
55279 2019_10_16    [FIX] repackage
55278 2019_10_16    [FIX] in some files not using highlight color needed in dark theme
55277 2019_10_05    [ADD] AS 3.5 -> 3.5.1
                    [ADD] remove TDWR products 78,80 per https://www.weather.gov/media/notification/scn19-82tdwr_terminations.pdf
55276 2019_09_29    [ADD] SPC Meso pw3k PW * 3kmRH
                    [ADD] option to center radar on location
55275 2019_09_28    [ADD] update kotlin coroutine and androidx stuff and make 2 code changes in LocationFragment and ViewPageAdapter that were not compiling as a result
		    [ADD] modern severe dash
55274 2019_08_31    [FIX] settings playlist bottomsheetfragment had location flag set causing entries to be invisible
                    [ADD] NHC - add more verbiage in storm card
                    [ADD] NHC Storm - move path grapgic to top, add qpf and arrival graphics
55273 2019_08_24    [ADD] kotlin plugin to 1.3.50, lint fixes
55272 2019_08_21    [FIX] update to Android Studio 3.5 which includes the usual Android Gradle Plugin update
                    [FIX] level 3 was broke with errant skip bytes after vcp change
55271 2019_08_20    [FIX] homescreen text objects were not working after recent change to strip out html
55270 2019_08_19    [ADD] vcp,op-mode extraction in level3 decoder
55269 2019_08_19    [ADD] WPC_ANALYSIS as homescreen option
                    [ADD] update okhttp from 3.12.0 to 3.12.3 (thanks TacoTheDank for the suggestion)
                    [FIX] remove maven {url "https://maven.google.com"}, redundant. Add commented out compile option for Java8 (thanks TacoTheDank for the suggestion)
                    [FIX] UtilityWXOGL.showTextProducts filter out SPS with no polygon
55268 2019_08_06    [FIX] few formatting enhancements in USalerts
                    [FIX] typo in about wX
                    [FIX] static shortcut for SPC Meso and SPC HREF
55267 2019_08_04    [FIX] locfrag - strip html for all text products added on homescreen per user comment
55266 2019_08_03    [FIX] redo uswarn to match iOS/Flutter to fix hang bug suspected of PR data
55265 2019_08_02    [ADD] update to buildToolsVersion '29.0.1' and NDC from r19c to r20
55264 2019_07_29    [ADD] comp ref to multipane radar
                    [ADD] diag info in about wX
                    [ADD] slightly better support for NDFD in WPC Img
55263 2019_07_23    [FIX] better tilt menu management
55262 2019_07_21    [FIX] program was crashing if WFO filter was enabled and hazard card was present on homescreen
55261 2019_07_21    [ADD] TDWR to list of fixed radars in settings -> homescreen
                    [ADD] in location editor, OSM search now auto-saves
                    [FIX] various changes to support tilt working across TDWR and NEXRAD
55260 2019_07_20    [ADD] add 38 NCZ Composite Reflectivity 248nm
55259 2019_07_20    [ADD] add 37 NCR Composite Reflectivity 124nm
55258 2019_07_19    [ADD] Add dedicated TDWR menu in submenu when TDWR is selected, add tr0, n1p, ntp
55257 2019_07_14    [ADD] long press radar text size now takes into account text size in settings
55256 2019_07_12    [ADD] android studio update with associated plugin updates
55255 2019_07_12    [FIX] Nam-hires start at one not zero
                    [ADD] In NHC and NHC storm, break up the download/display of data into chunks so the user at least sees something sooner rather then later
55254 2019_07_05    [FIX] labels changes in GOES activity to better match submenu choices
55253 2019_07_04    [FIX] GOES EAST/WEST FD animations not working
55252 2019_07_04    [ADD] kotlin plugin 1.3.40 -> 1.3.41
                    [FIX] at joshuatee.wx.canada.UtilityCanada.getLocationHtml (UtilityCanada.kt:267) java.lang.IndexOutOfBoundsException:
                    [FIX] at joshuatee.wx.notifications.UtilityNotificationSpc.sendSwoNotification (UtilityNotificationSpc.kt:416) java.lang.IndexOutOfBoundsException:
55251 2019_07_03    [FIX] lint cleanup
55250 2019_07_02    [FIX] at joshuatee.wx.audio.UtilityTts.playMediaPlayerFile(UtilityTts.kt:320)  java.lang.IllegalStateException
                    [FIX] NCEP HRW* change from 0..49 to 1..48
55249 2019_07_02    [FIX] Remove text scaling in CC due to issues of text truncation and not scaling back to size
                    [FIX] NHC storm - move title to subtitle as text is usuall to long
55248 2019_07_01    [FIX] USAlertsDetail was not using ObjectTextView and thus not respecting text size in UI -> settings (thanks Jim)
55247 2019_06_30    [FIX] ObjectMetar init had unecessary call to NWS API
                    [FIX] lint cleanup
55246 2019_06_29    [FIX] severe dashboard was not downloading vtec warnings if warnings not configured elsewhere
55245 2019_06_29    [FIX] severe dashboard linear layout was causing crash
55244 2019_06_29    [ADD] SPC SWO Summary - images 2 per row like iOS
                    [FIX] lint cleanup including remove ExternalGeodeticCurve.kt ExternalGlobalPosition.kt
                    [ADD] Severe dashboard - download images in chunks and display as it downloads
                    [FIX] add heat index via formula
55243 2019_06_28    [FIX] make touchimageview listener public for multipane model
55242 2019_06_28    [FIX] lint cleanup, remove ExternalGeodeticMeasurement.java
55241 2019_06_28    [FIX] lint cleanup including remove SolarEvent.kt, TouchImageViewOld.java
                    [ADD] SPC SWO - images 2 per row like iOS
55240 2019_06_25    [ADD] update TouchImageView2 from https://github.com/MikeOrtiz/TouchImageView ( version 2.3.0 )
                    [REF] lint cleanup
55239 2019_06_24    [FIX] lint cleanup
                    [ADD] SPC SWO to free up URLs
                    [REF] Remove SunCalc.kt
55238 2019_06_21    [ADD] kotlin plugin 1.3.31 -> 1.3.40
                    [FIX] at joshuatee.wx.models.UtilityModels.setSubtitleRestoreIMGXYZOOM(UtilityModels.kt:294)
                            java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
                            ```
