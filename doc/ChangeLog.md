[[_TOC_]]

# Developer ChangeLog

## 55943 2025_02_15

* [FIX] add Mist, Light Mist in UtilityMetarConditions
* [ADD] tooling update

```
-        classpath 'com.android.tools.build:gradle:8.8.0'
+        classpath 'com.android.tools.build:gradle:8.8.1'
```

## 55942 2025_02_04

* [FIX] activity_settings_location_generic.xml and activity_settings_color_palette_editor.xml were
  overlapping when gesture capable

## 55941 2025_02_04

* [FIX] activity_main.xml (tabs) needed one more mod for fab issue
* [FIX] statusbar background was inconsistent across many activities

## 55940 2025_02_03

* [REF] misc cleanup
* [FIX] more error handling in network io
* [FIX] scope issues in decode8BitAndGenRadials.c similar to NexradDecodeEightBit.kt
* [FIX] activity_main.xml activity_main_drawer_right.xml activity_main_drawer.xml FAB was
  overlapping when gesture capable
  device had 3 button mode
  enabled (needs more testing)

## 55939 2025_01_30

* [REF] misc cleanup

## 55938 2025_01_30

* [REF] misc cleanup

## 55937 2025_01_29

* [ADD] Start consolidation in UtilityNetworkIO.kt with requestToString
* [ADD] Correct Hazards to use proper lat/lon format for NWS API
* [FIX] in Sites.kt use sync

## 55936 2025_01_29

* [REF] move download functions out of UtilityDownloadNws.kt and into UtilityNetworkIO.kt
* [REF] simplify Hazards.getHazardsHtml and rename Hazards.getHtml
* [FIX] back button handling in WebView for API
  36 [https://developer.android.com/guide/navigation/navigation-custom-back](https://developer.android.com/guide/navigation/navigation-custom-back)
* [REF] update gradle in gradle/wrapper/gradle-wrapper.properties

```
-distributionUrl=https\://services.gradle.org/distributions/gradle-8.10.2-all.zip
+distributionUrl=https\://services.gradle.org/distributions/gradle-8.11.1-all.zip
```

* [ADD]

## 55935 2025_01_28

* [REF] code cleanup

## 55934 2025_01_25

* [ADD] In National Images remove "experimental" from the label for "Week 3-4 Outlooks -
  Temperature" and "Week 3-4 Outlooks - Precipitation" per *SCN24-104: The Experimental Weeks 3-4
  Precipitation Outlooks Will Become Operational on or about January 17, 2025*
* [ADD] "Prognostic Discussion for Week 3-4 Temperature and Precipitation Outlooks" in National Text
  Product Screen under **General Forecast Discussions**
* [ADD] NWS has discontinued the following per SCN24-108: Termination of the 72-Hour Low Tracks
  Graphic and the Non-Technical 72-Hour Low Tracks Graphic to be Replaced by Automated Low Clusters
  Forecast Tool: Effective January 20, 2025 , remove from National Images
* [FIX] In National Graphics the URL for **GLSEA Ice Analysis** had changed
* [REF] NexradDecodeEightBit change scope for key vars to be more local to enhance readability (
  originally created like this for performance reasons)

```
"Significant Surface Low Tracks",
"Low Tracks and Clusters",
```

* [ADD] "Automated Low Clusters" in National Images per SCN24-108: Termination of the 72-Hour Low
  Tracks
  Graphic and the Non-Technical 72-Hour Low Tracks Graphic to be Replaced by Automated Low Clusters
  Forecast Tool: Effective January 20, 2025

* [ADD] lib update

```
-    implementation 'androidx.recyclerview:recyclerview:1.3.2'
+    implementation 'androidx.recyclerview:recyclerview:1.4.0'
```

## 55933 2025_01_15

* [ADD] In Nexrad "long press" (press and hold), add Observation(Metar) site name next to the
  station code
* [REF] Add Metar.sites using the "Site/Sites" framework and modify most functions in this file,
  impacts
    - NwsObsSitesActivity
    - CanvasWindbarbs
    - NexradLayerDownload
    - NexradLongPressMenu
    - NexradRenderUI
    - ObjectMetar
    - CurrentConditions
    - UtilityUS
    - UtilityWidgetDownload

## 55932 2025_01_11

* [ADD] On homescreen if "Weather Story" is configured and you tap on it image is opened up in
  dedicated image viewer allowing you to share the image or zoom in with the full screen.
* [ADD] Similar to SPC Convective Outlook outlines as a preference in Nexrad Radar, make SPC Fire
  Weather Outlook outlines available as well (BETA - no dry thunderstorm support yet)
* [ADD] update from android-ndk-r27b to android-ndk-r27c

## 55931 2025_01_10

* [ADD] Better "Weather Story" handling for image in homescreen (if configured and if your WFO
  offers it or something similar)
* [ADD] geographic boundaries for Guam and the Commonwealth of the Northern Mariana Islands (CNMI) (
  required float size change in CanvasMain.kt and RadarGeomInfo.kt). NOTE: most functions do not
  work for Guam/CNMI due to insufficient LAT/LON adjustment, it's a WIP
* [ADD] 2 observations points in Guam/CNMI but they are not yet usable within the program, it's a
  WIP
* [ADD] tooling update

```
-        classpath 'com.android.tools.build:gradle:8.7.3'
+        classpath 'com.android.tools.build:gradle:8.8.0'

-distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-all.zip
+distributionUrl=https\://services.gradle.org/distributions/gradle-8.10.2-all.zip
```

## 55930 2025_01_05

* [ADD] geographic boundaries for US Virgin Islands and American Samoa
* [ADD] long press in nexrad will now show best vis sat image for Hawaii and areas around Puerto
  Rico

## 55929 2025_01_04

* [FIX] true root cause was fixed in UtilityNetworkIO.getBitmapFromUrl, should not have `!!` in such
  critical functions

missed one

```
+++ b/app/src/main/java/joshuatee/wx/util/UtilityNetworkIO.kt
@@ -86,6 +86,7 @@ object UtilityNetworkIO {
             ) ?: UtilityImg.getBlankBitmap()
         } else {
             BitmapFactory.decodeStream(BufferedInputStream(response.body.byteStream()))
+                ?: UtilityImg.getBlankBitmap()
```

## 55928 2025_01_04

* [FIX] true root cause was fixed in UtilityNetworkIO.getBitmapFromUrl, should not have `!!` in such
  critical functions

```
+++ b/app/src/main/java/joshuatee/wx/util/UtilityNetworkIO.kt
@@ -83,7 +83,7 @@ object UtilityNetworkIO {
                 BufferedInputStream(response.body.byteStream()),
                 null,
                 options
-            )!!
+            ) ?: UtilityImg.getBlankBitmap()
```

## 55927 2025_01_04

* [FIX] Pre-launch report for wX version 55926 captured multiple issues likely due to the newer
  kotlin version in use, replicated on physical as well. Crashes are in two areas but caused by the
  same thing. Not able to identify true root cause yet
  but the implication seems to be that UtilityNetworkIO.getBitmapFromUrl (called in extension
  function String.getImage()) which should return only
  Bitmap is in some cases returning null.
  Workaround for now is that in two functions which accepted Bitmap are not taking Bitmap? and then
  doing null check.
  Both crashes were seen in model activities in which the image was not present on the server (
  common with incomplete runs, etc or improper start times)

```
Process: joshuatee.wx, PID: 20099
      java.lang.NullPointerException: Parameter specified as non-null is null: method joshuatee.wx.util.UtilityImg.eraseBackground, parameter src
          at joshuatee.wx.util.UtilityImg.eraseBackground(Unknown Source:4)
          at joshuatee.wx.models.UtilityModelSpcHrrrInputOutput.getImage(UtilityModelSpcHrrrInputOutput.kt:69)
          at joshuatee.wx.models.ObjectModelGet.image(ObjectModelGet.kt:38)
          at joshuatee.wx.models.UtilityModels.getContent$lambda$1(UtilityModels.kt:56)
          at joshuatee.wx.models.UtilityModels.$r8$lambda$Jwn-Ydz84lI7xqKUTj5nF61jriw(Unknown Source:0)
          at joshuatee.wx.models.UtilityModels$$ExternalSyntheticLambda2.invoke(D8$$SyntheticClass:0)
          at joshuatee.wx.objects.FutureVoid$getContent$1$1.invokeSuspend(FutureVoid.kt:33)
          at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
          at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:100)
          at kotlinx.coroutines.internal.LimitedDispatcher$Worker.run(LimitedDispatcher.kt:113)
          at kotlinx.coroutines.scheduling.TaskImpl.run(Tasks.kt:89)
          at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:586)
          at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:820)
          at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:717)
          at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:704)
          Suppressed: kotlinx.coroutines.internal.DiagnosticCoroutineContextException: [StandaloneCoroutine{Cancelling}@dab4ae4, Dispatchers.Main]
```

```
@@ -88,13 +103,15 @@ class TouchImage {
     val scrollPosition: PointF?
         get() = img.scrollPosition
 
-    fun set(bitmap: Bitmap) {
-        img.setImageBitmap(bitmap)
-        imageLoaded = true
-        if (prefTokenIdx != "" && drw != null) {
-            Utility.writePrefInt(context, prefTokenIdx, drw!!.index)
+    fun set(bitmap: Bitmap?) {
+        if (bitmap != null) {
+            img.setImageBitmap(bitmap)
+            imageLoaded = true
+            if (prefTokenIdx != "" && drw != null) {
+                Utility.writePrefInt(context, prefTokenIdx, drw!!.index)
+            }
+            this.bitmap = bitmap
```

```
+++ b/app/src/main/java/joshuatee/wx/util/UtilityImg.kt
-    fun eraseBackground(src: Bitmap, color: Int): Bitmap {
+    fun eraseBackground(src: Bitmap?, color: Int): Bitmap {

```

## 55926 2025_01_04

* [ADD] add Metars for Puerto Rico and US Virgin Islands
* [ADD] Tool/lib update

```
-    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0'
-    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0'
+    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1'
+    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1'

-        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23"
+        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21"
```

## 55925 2024_12_26

* [ADD] In hourly, abbreviate "Drizzle" to "Dz"
* [ADD] updated lib

```
-    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.1.3'
+    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.1.4'
```

## 55924 2024_12_18

* [ADD] per **PNS24-66 Updated** NWS will implement a fix in advance of Dec 22 for the NWS API, thus
  7 day forecast is now configurable again and defaults to using the new NWS API
* [REF] refactor parts of color palette generators to match other ports
* [REF] add extensions getHtmlWithNewLineWithRetry and getNwsHtmlWithRetry and use for NWS API 7-day
  and Hourly
* [ADD] ObjectMetar - for getting current conditions for main screen do manual retry if needed (like
  7-day/hourly)
* [FIX] in report to the following crash report use a safeGet in one line of code

```
Exception java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0
  at joshuatee.wx.ui.SevenDayCollection.update (SevenDayCollection.kt:34)

```

## 55923 2024_12_03

* [ADD] In the adhoc forecast page accessible via "long press" in Nexrad radar, include nearest city
  in subtitle and if location is saved, a better name will be used.

## 55922 2024_12_02

* [ADD] In the main submenu under **Observations** add access to new images
  from [Unified Surface Analysis](https://ocean.weather.gov/unified_analysis.php), images jointly
  produced by NCEP, WPC, OPC, NHC, and HFO
* [ADD] to Settings -> About -> "View data provider: NWS" (information on where data used in this
  application is sourced from)
* [FIX] force 7 day forecast to use older NWS API due to expected outage per **PNS24-66: Potential
  API Gridded Forecast Data Outage from December 22, 2024 through January 1, 2025**
* [REF] Tool update

```
-        classpath 'com.android.tools.build:gradle:8.7.2'
+        classpath 'com.android.tools.build:gradle:8.7.3'
```

## 55921 2024_11_17

* [REF] evacuate radar lists from GlobalArrays and use functions in RadarSites
* [REF] radar mosaic cleanup

```
	renamed:    app/src/main/java/joshuatee/wx/radar/RadarMosaicNwsActivity.kt -> app/src/main/java/joshuatee/wx/radar/RadarMosaicActivity.kt
	renamed:    app/src/main/java/joshuatee/wx/radar/UtilityNwsRadarMosaic.kt -> app/src/main/java/joshuatee/wx/radar/UtilityRadarMosaic.kt
```

## 55920 2024_11_07

* [FIX] Nexrad "Long press" - meteogram URL had changed
* [ADD] Nexrad "Long press" - add show nearest SPC Meso sector
* [ADD] tool/lib update

```
-    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.1.2'
+    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.1.3'
```

## 55919 2024_11_06

* [ADD] remove unnecessary entries in WfoSites.kt and add sites
* [REF] remove ObjectLocation.state
* [REF] remove RID, migrate to Site
* [ADD] Nexrad "Long press" - add show nearest vis satellite image at bottom
* [ADD] Nexrad "Long press" - add show nearest AFD (area forecast discussion text product)

## 55918 2024_11_02

* [ADD] additional Soundings sites (especially AK/HI), remove some that were obsolete
* [ADD] Sites/Site framework to be used by Sounding sites and wfo/radar in the future
* [ADD] Nearest Sounding option to Nexrad "Long press" (press and hold) menu - added to bottom (
  includes direction)
* [ADD] Nexrad "Long press" now shows direction in addition to distance for closest radars and
  observation point
* [ADD] Nexrad "Long press" more concise verbiage: miles to mi (in 2 spots)
* [ADD] Nexrad "Long press" miles to location/radar site enhanced to show direction to
* [ADD] NHC Storm card - show bearing after direction
* [ADD] tool/lib updates

```
-        classpath 'com.android.tools.build:gradle:8.7.1'
+        classpath 'com.android.tools.build:gradle:8.7.2'

-    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6'
+    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7'

-    implementation 'androidx.core:core-ktx:1.13.1'
+    implementation 'androidx.core:core-ktx:1.15.0'
```

## 55917 2024_10_14

* [ADD] reinstate wX Android setting "Main screen radar button (requires restart)"
* [REF] tool update

```
-        classpath 'com.android.tools.build:gradle:8.7.0'
+        classpath 'com.android.tools.build:gradle:8.7.1'
```

## 55916 2024_10_12

* [ADD] NHC storm summary in NHC activity - show more headlines
* [ADD] NHC Storm activity title was not showing wind speed in mph

## 55915 2024_10_06

* [ADD] code changes to support changing how hourly is displayed to work better with larger fonts
* [ADD] In Hourly, abbreviate "Isolated" as "Iso"
* [FIX] SPC Compmap - remove product which is no longer available via the website: **3-hr surface
  pressure change**
* [FIX] SPC Compmap - change references from **HPC** to **WPC**
* [ADD] Tool update

```
-        classpath 'com.android.tools.build:gradle:8.6.1'
+        classpath 'com.android.tools.build:gradle:8.7.0'

-distributionUrl=https\://services.gradle.org/distributions/gradle-8.7-all.zip
+distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-all.zip
```

## 55914 2024_09_28

* [ADD] NHC Storm: add additional graphics "Peak Storm Surge Forecast" and "Cumulative Wind History"
* [ADD] monochrome icon for themed icon support

```
<monochrome android:drawable="@mipmap/ic_launcher_foreground"/>
in
app/src/main/res/mipmap-anydpi/ic_launcher_new.xml
```

* [REF] remove unused images due to API min upgrade

```
	deleted:    mipmap-hdpi/ic_launcher_new.png
	deleted:    mipmap-mdpi/ic_launcher_new.png
	deleted:    mipmap-xhdpi/ic_launcher_new.png
	deleted:    mipmap-xxhdpi/ic_launcher_new.png
	deleted:    mipmap-xxxhdpi/ic_launcher_new.png
```

## 55913 2024_09_22

* [ADD] link to Privacy Policy in Settings About which appears to be now required by Google

## 55912 2024_09_20

* [REF] cleanup from prior releases
* [REF] lint - remove obsolete SDK checks, including

```
	renamed:    mipmap-anydpi-v26/ic_launcher_new.xml -> mipmap-anydpi/ic_launcher_new.xml
```

* [ADD] convenience LatLon.empty()

## 55911 2024_09_19

* [ADD] (WIP) edge-to-edge workarounds
* [ADD] deprecate these options as mentioned
  in [upcoming changes document](https://gitlab.com/joshua.tee/wxl23/-/blob/master/doc/UPCOMING_CHANGES.md)

- (added 2023-09-10) wX Android setting "Radar with transparent toolbars" will be retired after
  2024-09-10
- (added 2023-09-10) wX Android setting "Radar with transparent status bar" will be retired after
  2024-09-10
- (added 2023-09-10) wX Android setting "Radar: immersive mode" will be retired after 2024-09-10
- (added 2023-09-10) wX Android setting "Main screen radar button (requires restart)" will be
  retired after 2024-09-10

```
files impacted - need to be cleaned up later

	modified:   app/src/main/java/joshuatee/wx/WX.kt
	modified:   app/src/main/java/joshuatee/wx/radar/NexradLongPressMenu.kt
	modified:   app/src/main/java/joshuatee/wx/radar/NexradRenderSurfaceView.kt
	modified:   app/src/main/java/joshuatee/wx/radar/NexradSubmenu.kt
	modified:   app/src/main/java/joshuatee/wx/radar/NexradUI.kt
	modified:   app/src/main/java/joshuatee/wx/radar/WXGLRadarActivity.kt
	modified:   app/src/main/java/joshuatee/wx/radar/WXGLRadarActivityMultiPane.kt
	modified:   app/src/main/java/joshuatee/wx/settings/SettingsDeveloperActivity.kt
	modified:   app/src/main/java/joshuatee/wx/settings/UIPreferences.kt
	modified:   app/src/main/java/joshuatee/wx/ui/ObjectDialogue.kt
	modified:   app/src/main/java/joshuatee/wx/ui/Switch.kt
	modified:   app/src/main/java/joshuatee/wx/ui/UtilityToolbar.kt
	modified:   app/src/main/java/joshuatee/wx/ui/UtilityUI.kt
```

* [ADD] lib update

```
-    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.5"
+    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6"
```

## 55910 2024_09_19

* [ADD] (WIP) edge-to-edge workarounds

## 55909 2024_09_18

* [ADD] (WIP) edge-to-edge workarounds

* [ADD] API 35 Support 16 KB page sizes
    - https://developer.android.com/guide/practices/page-sizes
    - changes in `app/src/main/jni/Application.mk` and `app/build.gradle`
* [ADD] (WIP) prelim work in WX (main screen), SPC Meso, Image Collection, Hourly to work around
  edge-to-edge

## 55908 2024_09_18

* [ADD] per upcoming changes, change minSdkVersion from 25 to 27
* [ADD] change targetSdkVersion/compileSdk from 34 to 35
* [ADD] tool update

```
-        classpath 'com.android.tools.build:gradle:8.6.0'
+        classpath 'com.android.tools.build:gradle:8.6.1'
```

## 55907 2024_09_15

* [ADD] more GOES "Full Disk" development
* [ADD] NDK update

```
-    def ndkVersion = "android-ndk-r26d"
+    def ndkVersion = "android-ndk-r27b"
```

* [ADD] dep updates

```
-    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1'
-    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1'
+    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0'
+    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0'
```

## 55906 2024_09_14

* [FIX] GOES "Full Disk" (Misc Tab) images location and product names have changed, animations are
  now available for all products. They have been
  moved here (there are many more Himawari images that have been added which are not yet
  incorporated):
    - [https://www.ospo.noaa.gov/products/imagery/meteosat.html](https://www.ospo.noaa.gov/products/imagery/meteosat.html)
    - [https://www.ospo.noaa.gov/products/imagery/meteosatio.html](https://www.ospo.noaa.gov/products/imagery/meteosatio.html)
    - [https://www.ospo.noaa.gov/products/imagery/fulldisk.html](https://www.ospo.noaa.gov/products/imagery/fulldisk.html)

## 55905 2024_09_05

* [ADD] lib/tool updates

```
-    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.1.0'
+    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.1.2'

-        classpath 'com.android.tools.build:gradle:8.5.2'
+        classpath 'com.android.tools.build:gradle:8.6.0'


-    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4"
+    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.5"

```

## 55904 2024_08_26

* [ADD] revert unsafe OkHttpClient as NWS seems to have fixed, leave code in place in case needed
  again
* [ADD] lib update

```
-    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.0.4'
+    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.1.0'
```

## 55903 2024_08_23

* [ADD] tool update

```
-        classpath 'com.android.tools.build:gradle:8.5.1'
+        classpath 'com.android.tools.build:gradle:8.5.2'
```

* [ADD] unsafe OkHttpClient that does not check certs in case NWS does not respond by early next
  week to cert renewal issue

## 55902 2024_07_31

* [ADD] NCEP MAG [v5.1](https://mag.ncep.noaa.gov/version_updates.php):
    - Added the following new domains to NAEFS and GEFS-MEAN-SPRD:
        - Continental United States (CONUS)
        - Alaska
    - Added the following new products to RTMA:
        - Ceiling (ceiling -- not included for Guam)
        - Visibility (vis)

## 55901 2024_07_26

* [FIX] per SCN24-66: Termination of the Day 3-7 Hemispheric Charts: Effective July 24, 2024, remove
  5 images in National Images -> Forecast Maps
* [FIX] move temp_*.png from res/drawable to res/drawable-nodpi (ideally there would be multiple
  sized images for mdpi,ldpi,hdpi,xhdpi)
* [REF] lib update

```
-    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3"
+    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4"
```

## 55900 2024_07_21

* [FIX] add @Synchronized to Metar.get
* [FIX] Exception java.lang.IndexOutOfBoundsException: Index 7 out of bounds for length 7
  at joshuatee.wx.notifications.NotificationMpd.sendLocation (NotificationMpd.kt:81) (do this in Mcd
  as well)

## 55899 2024_07_12

* [ADD] tool update

```
-        classpath 'com.android.tools.build:gradle:8.5.0'
+        classpath 'com.android.tools.build:gradle:8.5.1'
```

* [FIX] in response to crash report **Exception java.lang.ArrayIndexOutOfBoundsException: length=0;
  index=0 at joshuatee.wx.radar.NexradRenderUtilities.genTriangleUp (NexradRenderUtilities.kt:139)**

```
// likely a rare timing issue but should not crash on this just not show data since there is none
// make 2 changes around this line like
buffers.yList[index]
// change to
(buffers.yList.getOrNull(index) ?: 0.0)
```

## 55898 2024_07_11

* [FIX] URL for **Weeks 2-3 Global Tropics Hazards Outlook (GTH)** had changed, this is accessed
  under **National Images -> CPC**
* [FIX] The following text product was not working: High Seas Forecasts - SE Pacific
* [FIX] JobScheduler (for background processing) - cancel any existing before starting new (needs
  testing)
* [ADD] lib update

```
-    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2"
+    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3"
```

## 55897 2024_06_20

* [FIX] icons for NWS API were broken due to NWS issue, add
  workaround https://github.com/weather-gov/api/discussions/733

## 55896 2024_06_14

* [ADD] back ability to use NWS API or old for forecast/hourly since NWS has changed due date from
  end of June to TBD. But, API is now the default for forecast
* [ADD] tooling update

```
-        classpath 'com.android.tools.build:gradle:8.4.1'
+        classpath 'com.android.tools.build:gradle:8.5.0'

-    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.1"
+    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2"

-distributionUrl=https\://services.gradle.org/distributions/gradle-8.6-all.zip
+distributionUrl=https\://services.gradle.org/distributions/gradle-8.7-all.zip



```

## 55895 2024_06_05

* [ADD] increase mind size for 2nd retry for hourly download with new API
* [ADD] library update

```
-    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0"
+    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.1"
```

## 55894 2024_05_29

* [ADD] 2nd retry for forecast download with new API
* [ADD] add 1 sec sleep in 2nd retry for hourly download with new API
* [REF] in response to **SCN24-19: The National Centers for Environmental Prediction (NCEP) Climate
  Prediction Center (CPC) will Change the Depiction and Output Formats of Several Ultraviolet
  Index (UVI) Product Graphics on or about May 25, 2024**

```
--- a/app/src/main/java/joshuatee/wx/wpc/UtilityWpcImages.kt
+++ b/app/src/main/java/joshuatee/wx/wpc/UtilityWpcImages.kt
@@ -303,10 +303,10 @@ internal object UtilityWpcImages {
             "${GlobalVariables.NWS_CPC_NCEP_WEBSITE_PREFIX}/products/expert_assessment/month_drought.png",
             "${GlobalVariables.NWS_CPC_NCEP_WEBSITE_PREFIX}/products/expert_assessment/sdohomeweb.png",
             "https://droughtmonitor.unl.edu/data/png/current/current_usdm.png",
-            "${GlobalVariables.NWS_CPC_NCEP_WEBSITE_PREFIX}/products/stratosphere/uv_index/gif_files/uvi_usa_f1_wmo.gif",
-            "${GlobalVariables.NWS_CPC_NCEP_WEBSITE_PREFIX}/products/stratosphere/uv_index/gif_files/uvi_usa_f2_wmo.gif",
-            "${GlobalVariables.NWS_CPC_NCEP_WEBSITE_PREFIX}/products/stratosphere/uv_index/gif_files/uvi_usa_f3_wmo.gif",
-            "${GlobalVariables.NWS_CPC_NCEP_WEBSITE_PREFIX}/products/stratosphere/uv_index/gif_files/uvi_usa_f4_wmo.gif",
+            "${GlobalVariables.NWS_CPC_NCEP_WEBSITE_PREFIX}/products/stratosphere/uv_index/gif_files/uvi_usa_f1_who.png",
+            "${GlobalVariables.NWS_CPC_NCEP_WEBSITE_PREFIX}/products/stratosphere/uv_index/gif_files/uvi_usa_f2_who.png",
+            "${GlobalVariables.NWS_CPC_NCEP_WEBSITE_PREFIX}/products/stratosphere/uv_index/gif_files/uvi_usa_f3_who.png",
+            "${GlobalVariables.NWS_CPC_NCEP_WEBSITE_PREFIX}/products/stratosphere/uv_index/gif_files/uvi_usa_f4_who.png",
```

## 55893 2024_05_22

* [ADD] updates

```
-    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0'
-    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0'
+    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1'
+    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1'

-        classpath 'com.android.tools.build:gradle:8.4.0'
+        classpath 'com.android.tools.build:gradle:8.4.1'
```

## 55892 2024_05_17

* [FIX] pre-launch crash report: Exception java.lang.NullPointerException:
  at joshuatee.wx.audio.UtilityTts.synthesizeTextAndPlay (UtilityTts.kt:152)
* [REF] general cleanup in UtilityTts

## 55891 2024_05_16

* [ADD] per **SCN24-50: Migration of JSON and DWML Data Services to NWS’s API Application at
  api.weather.gov and Removal from forecast.weather.gov, Effective June 27, 2024**
  remove options to not use "new NWS API" and force everyone to use "new NWS API"

## 55890 2024_05_03

* [FIX] 2 crashes only seen in Pre-launch report: UtilityTts.kt and UtilityUI.kt

```
-        return if (Build.VERSION.SDK_INT >= 30) {
+        return if (Build.VERSION.SDK_INT >= 30 && activity.window.decorView.rootWindowInsets != null) {

         ttobjGlobal.let {
-            ttobjGlobal!!.stop()
-            ttobjGlobal!!.shutdown()
+            ttobjGlobal?.stop()
+            ttobjGlobal?.shutdown()
         }

```

## 55889 2024_05_03

* [ADD] lib update

```
-    implementation 'com.google.android.material:material:1.11.0'
+    implementation 'com.google.android.material:material:1.12.0'
```

* [REF] lint

## 55888 2024_05_02

* [ADD] tool update

```
-    implementation 'androidx.core:core-ktx:1.13.0'
+    implementation 'androidx.core:core-ktx:1.13.1'
```

* [REF] remove unused VoiceCommandActivity.kt
* [FIX] add UtilityTts.initTts(this) in CommonActionBarFragment.kt when VR is invoked
* [FIX] UtilityTts.initTts, move 2 statements into callback

## 55887 2024_05_01

* [FIX] crash java.lang.IndexOutOfBoundsException at
  joshuatee.wx.ui.SingleTextAdapterList.deleteItem (SingleTextAdapterList.kt:45)
  at joshuatee.wx.ui.ObjectRecyclerView.deleteItem (ObjectRecyclerView.kt:60)
  at joshuatee.wx.settings.FavRemoveActivity.deleteItem (FavRemoveActivity.kt:128)
* [FIX] VR "forecast" was not playing audio at start
* [ADD] updates and lint

```
-        classpath 'com.android.tools.build:gradle:8.3.2'
+        classpath 'com.android.tools.build:gradle:8.4.0'

-distributionUrl=https\://services.gradle.org/distributions/gradle-8.4-all.zip
+distributionUrl=https\://services.gradle.org/distributions/gradle-8.6-all.zip
```

## 55886 2024_04_30

* [FIX] NexradRenderConstruct.lines: check greater then zero (was commented out as analyzer suggests
  it's always true)

```
Exception java.lang.ArithmeticException: divide by zero
  at joshuatee.wx.radar.NexradRenderConstruct.lines (NexradRenderConstruct.kt:106)
```

## 55885 2024_04_28

* [FIX] check for null in NexradRenderTextObject.addSpotter
* [FIX] National Text now shows play/pause button similar to other text viewing activities

## 55884 2024_04_27

* [ADD] VR "forecast" now goes to text screen showing forecast (was not working) - not able to test
  via emulator
* [FIX] VR popup changed to short duration from indefinite

## 55883 2024_04_24

* [ADD] add dev option to scroll to top of main screen on restart/resume
* [ADD] dep update

```
-    implementation 'androidx.core:core-ktx:1.12.0'
+    implementation 'androidx.core:core-ktx:1.13.0'
```

## 55882 2024_04_21

* [ADD] add dev option to show hourly graph
* [ADD] per user request add the following to notification filters

```
"Coastal Flood Warning",
"Coastal Flood Watch",
"Coastal Flood Advisory"
```

* [ADD] okhttp update

```
-    implementation "com.squareup.okhttp3:okhttp:5.0.0-alpha.12"
+    implementation "com.squareup.okhttp3:okhttp:5.0.0-alpha.14"
```

* [REF] rename

```
	renamed:    app/src/main/java/joshuatee/wx/misc/UtilityUSHourly.kt -> app/src/main/java/joshuatee/wx/misc/UtilityHourly.kt
```

## 55881 2024_04_10

* [REF] lint
* [ADD] tool update

```
-        classpath 'com.android.tools.build:gradle:8.3.1'
+        classpath 'com.android.tools.build:gradle:8.3.2'
```

## 55880 2024_04_07

* [REF] In nexrad layout remove unnecessary layouts
* [REF] lint, including the following

```
	modified:   app/src/main/java/joshuatee/wx/externalColorChooser/SVBar.kt
	modified:   app/src/main/java/joshuatee/wx/externalColorChooser/SaturationBar.kt
	modified:   app/src/main/java/joshuatee/wx/externalColorChooser/ValueBar.kt
	modified:   app/src/main/res/values/attrs.xml
```

* [ADD] migrate to new UtilityUI.statusBarHeight

## 55879 2024_04_05

* [ADD] move to non-deprecated method "synthesizeToFile" in UtilityTts.kt
* [ADD] make changes in the following files to avoid computing pane sizes in quad pane nexrad (use
  layout from dual pane)

```
	modified:   app/src/main/java/joshuatee/wx/radar/NexradStatePane.kt
	modified:   app/src/main/res/layout/activity_uswxoglmultipane_quad_immersive.xml
	modified:   app/src/main/res/layout/activity_uswxoglmultipane_quad_immersive_white.xml
```

## 55878 2024_04_03

* [ADD] In support of **SCN24-02: New Forecast Product “Offshore Waters Forecast for SW N Atlantic
  Ocean”
  Will Start on March 26, 2024**
  add `offnt5` and rename title for `offnt3`. These products are accessed via "National Text"
  activity.
* [FIX] lint in UtilityTheme.setPrimaryColor by using different methods
* [REF] native code formatting
* [ADD] move Text To Speech (TTS) data files into private storage to remove the need for this:
  android.permission.WRITE_EXTERNAL_STORAGE

## 55877 2024_03_26

* [ADD] add support for Nexrad "KHDC" for Service Change Notice 24-11 Hammond, LA WSR-88D (KHDC)
  to Begin NEXRAD Level III Product Dissemination on or around March 31, 2024.

## 55876 2024_03_25

* [REF] remove compose until needed (app size grows by 8MB)
* [REF] move some methods to new files
* [REF] lint

## 55875 2024_03_25

* [REF] cleanup from 55874
* [FIX] migration to API34 (or a newer material lib, etc) required NavigationView background to be
  hardcoded in Drawer.kt
* [ADD] enable compose
* [REF] fix build
  warning `warning: [options] source value 8 is obsolete and will be removed in a future release`

```
-        sourceCompatibility JavaVersion.VERSION_1_8
-        targetCompatibility JavaVersion.VERSION_1_8
+        sourceCompatibility JavaVersion.VERSION_11
+        targetCompatibility JavaVersion.VERSION_11

     kotlinOptions {
-        jvmTarget = "1.8"
+        jvmTarget = "11"
     }
```

## 55874 2024_03_24

* [ADD] per upcoming changes: wX Android screen recording (**but not** associated drawing tools)
  will not be available
  after May 1, 2024 Please use native screen recording and screen shot capabilities instead.
  This existing functionality does not fall within Google's accepted "Foreground Service Type" once
  the app
  targets API34 (Android 14) which is required by sometime later in 2024.
* [ADD] Target the latest version of Android (API 34, this is periodically required to be compliant
  with
  Google Play Store)
* [ADD] update libs

```
-    implementation 'com.google.android.material:material:1.9.0'
-    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2"
+    implementation 'com.google.android.material:material:1.11.0'
+    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0"

-    implementation 'androidx.core:core-ktx:1.10.0'
-    implementation 'androidx.media:media:1.6.0'
+    implementation 'androidx.core:core-ktx:1.12.0'
+    implementation 'androidx.media:media:1.7.0'
```

* [ADD] Prelim KHDC data (commented out) for Service Change Notice 24-11 Hammond, LA WSR-88D (KHDC)
  to Begin NEXRAD Level III
  Product Dissemination on or around March 31, 2024
* [FIX] lint cleanup
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

## 55873 2024_03_09

* [FIX] (thanks to "ski warz" for the suggestion) despite alpha tag update okhttp, "safe to run in
  production" and works better with ipv6 hopefully fix GOES slowness
  with [https://www.star.nesdis.noaa.gov/goes/index.php](https://www.star.nesdis.noaa.gov/goes/index.php):
  [https://square.github.io/okhttp/changelogs/changelog/](https://square.github.io/okhttp/changelogs/changelog/)

```
-    implementation "com.squareup.okhttp3:okhttp:4.12.0"
+    implementation "com.squareup.okhttp3:okhttp:5.0.0-alpha.12"
```

* [FIX] (thanks to "ski warz" for noticing and providing fix) "Aviation only AFD" was not working

## 55872 2024_03_05

* NDK update

```
-    def ndkVersion = "android-ndk-r26b"
+    def ndkVersion = "android-ndk-r26c"
```

## 55871 2024_03_02

* [REF] lint - constant renames to upper (part1)
* [REF] deprecate String.getHtmlSep() (UtilityNetworkIO.getStringFromUrlWithSeparator) used in
  NotificationSpcFireWeather.kt

## 55870 2024_03_01

* [REF] minor lint
* [ADD] tool upgrade
  2024_03_01:

```
-    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'
-    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
+    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0'
+    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0'

-        classpath 'com.android.tools.build:gradle:8.2.2'
+        classpath 'com.android.tools.build:gradle:8.3.0'

-distributionUrl=https\://services.gradle.org/distributions/gradle-8.2-all.zip
+distributionUrl=https\://services.gradle.org/distributions/gradle-8.4-all.zip
```

2024_01_24:

```
-        classpath 'com.android.tools.build:gradle:8.2.1'
+        classpath 'com.android.tools.build:gradle:8.2.2'
```

## 55869 2024_01_09

* [FIX] WPC Rainfall Outlook Day 1 - 5, Title was truncated, move parts of Title to Sub-title
* [FIX] usalerts on chromeOS, cards should take remaining horizontal space on screen
* [ADD] Previously, the graph in the hourly activity was set to a fixed size height.
  On laptops (ie ChromeOS) this led to the graph being very narrow. The graph is now set to
  slightly less then the width and the height is a fixed fraction of that width.
* [REF] lint
* [ADD] ChromeOS keyboard shortcut for main screen "Ctrl - s" (SPC SWO Summary)
* [ADD] ChromeOS keyboard shortcut for main screen "Ctrl - g" (Rainfall Outlook Summary)
* [REF] WPC renames

```
	renamed:    WpcImagesActivity.kt -> NationalImagesActivity.kt
	renamed:    WpcTextProductsActivity.kt -> NationalTextActivity.kt
	renamed:    WpcRainfallForecastActivity.kt -> RainfallOutlookActivity.kt
	renamed:    WpcRainfallForecastSummaryActivity.kt -> RainfallOutlookSummaryActivity.kt
```

## 55868 2024_01_05

* [REF] lint
* [REF] rename

```
renamed:    app/src/main/java/joshuatee/wx/misc/USAlertsDetailActivity.kt -> app/src/main/java/joshuatee/wx/misc/AlertsDetailActivity.kt
```

* [REF] rename dir activitiesmisc to misc
* [REF] remove the following which were not being used

```
	deleted:    app/src/main/java/joshuatee/wx/fingerdraw/FingerDrawActivity.kt
	deleted:    app/src/main/res/layout/fingerdraw_layout.xml
```

## 55867 2024_01_04

* [REF] Android Studio migration plus optional build.gradle migrations as follows
  https://kotlinlang.org/docs/whatsnew1920.html#ide-support

```
-        classpath 'com.android.tools.build:gradle:8.2.0'
+        classpath 'com.android.tools.build:gradle:8.2.1'

-        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0"
+        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22"

-    aaptOptions {
-        additionalParameters "--no-version-vectors"
-    }

-    lintOptions {
-        disable 'Typos'
-        checkReleaseBuilds false
-        abortOnError false
-    }

+    androidResources {
+        additionalParameters '--no-version-vectors'
+    }
+    lint {
+        abortOnError false
+        checkReleaseBuilds false
+        disable 'Typos'
+    }

```

* [REF] remove older options in build.gradle
* [FIX] for theme "white", change the following in `src/main/res/values/styles.xml`

```
-        <item name="alertDialogTheme">@style/AppCompatAlertDialogStyle</item>
+        <item name="alertDialogTheme">@style/AppCompatAlertDialogStyle2</item>
```

```
-        vectorDrawables.useSupportLibrary = true
+        // vectorDrawables.useSupportLibrary = true

-    androidResources {
-        additionalParameters '--no-version-vectors'
-    }
+//    androidResources {
+//        additionalParameters '--no-version-vectors'
+//    }
```

## 55866 2024_01_03

* [FIX] In GOES, move share icon to submenu as when animating pause icon was not showing on some
  devices
* [FIX] In GOES, Animate button was not working as intended. if tap Stop once animating, it should
  show latest image
* [REF] continue lint and spellcheck activities

## 55865 2024_01_02

* [REF] remove the following from UtilityStorePreferences.kt

```
-            editor.putString("LOC1_NWS", "OUN")
-            editor.putString("NWS1_STATE", "OK")
-            editor.putString("STATE", "Oklahoma")
-            editor.putString("STATE_CODE", "OK")
-            editor.putString("THEME_BLUE", "whiteNew")
-            editor.putString("NWS_RADAR_BG_BLACK", "true")
-            editor.putInt("ALERT_BLACKOUT_AM", 7)
-            editor.putInt("ALERT_BLACKOUT_PM", 22)
-            editor.putString("ALERT_BLACKOUT_TORNADO", "true")
-            editor.putString("ALERT_BLACKOUT", "false")
-            editor.putString("LOCK_TOOLBARS", "true")
-            editor.putString("RADAR_SHOW_COUNTY", "true")
-            editor.putString("ALERT_ONLYONCE", "true")
-            editor.putString("ALERT_AUTOCANCEL", "true")


```

## 55864 2023_12_31

* [REF] cleanup

## 55863 2023_12_31

* [REF] move UtilityNotification.storeWatchMcdLatLon to LatLon.storeWatchMcdLatLon (to match other
  versions)
* [REF] lint esp for new graphview code
* [REF] sync up UtilitySpcStormReports.kt and StormReport.kt with other versions
* [REF] remove remnants of deprecated option: UIPreferences.iconsEvenSpaced

## 55862 2023_12_30

* [REF] in gradle.properties, comment out `android.enableJetifier=true`, the default is `false`
* [ADD] Hourly graph: remove the following external dependency and integrate the APL2.0 licensed
  code directly into the project
  for ongoing stability. The repo maintainer states they are looking for someone else to take it
  over and not many changes
  done in past few years to keep current, this should enable TODO to be completed:
  `migrate from "android.enableJetifier=true" to "android.enableJetifier=false" in`

```
build.gradle:
+    // graphview source code was merged locally into project on 2023-12-30
+//    implementation 'com.jjoe64:graphview:4.2.2'

// add the following files/dir
	app/src/main/java/joshuatee/wx/externalGraphView/
	app/src/main/res/values/attr_graphview.xml
	doc/COPYING.GraphView
```

* [ADD] ObjectModelGet
* [FIX] SPC SREF - time menu was not showing up in bottom toolbar
* [FIX] SPC HREF - when change sector, reset zoom level on image(s)
* [FIX] SPC HREF - remove first time "00" in series which is not valid for this model
* [FIX] NCEP MAG Models - when change sector, reset zoom level on image(s)
* [REF] remove unused resources
* [ADD] change keyboard shortcuts (ie for ChromeOS) to more closely match desktop ports, SPC Meso (
  cltr-Z), Nat Text (cltr-T), Settings (cltr-P)

```
	deleted:    app/src/main/res/drawable/ic_skip_next_24dp.xml
	deleted:    app/src/main/res/drawable/ic_skip_previous_24dp.xml
	deleted:    app/src/main/res/layout/activity_uswxoglmultipane_immersive.xml
	deleted:    app/src/main/res/layout/activity_uswxoglmultipane_immersive_white.xml
	deleted:    app/src/main/res/layout/activity_uswxoglmultipane_quad.xml
	deleted:    app/src/main/res/layout/activity_uswxoglmultipane_quad_white.xml
```

## 55860 2022_12_29

* [ADD] have all HTML get functions use user-agent header
* [REF] Metar - should be `private var obsLatlon = mutableMapOf<String, LatLon>()`
* [REF] RID.distance change from Int to Double

## 55861 2022_12_28

## 55860 2022_12_28

* [REF] UtilityUS - use LatLon as arg, add latForNws/lonForNws (.4 precision) to LatLon
* [REF] add UtilityDownloadNws.getHourlyOldData and use in UtilityHourlyOldApi.kt
* [REF] add UtilityIO.getHtml for compat
* [REF] rename Metar.readData to Metar.loadMetarData for compat

## 55859 2022_12_24

* [FIX] Orange theme still had blue FAB
* [ADD] Main screen: make location label and hazards bold
* [FIX] Nexrad radar city textual labels and observation labels were not working well on ChromeOS,
  change the way size is
  computed.
* [ADD] AppCompatAlertDialogStyle2 in app/src/main/res/values-v28/styles.xml to have less rounder
  corners in dialogue
* [FIX] Rainfall Outlook Summary - swap title and subtitle as title was too long
* [FIX] (needs test on real device) on ChromeOS using keyboard shortcuts in Nexrad, textual labels
  were not updating

## 55858 2022_12_24

* [ADD] as mentioned in "Upcoming Changes" since Aug 2022:
  The option **Icons evenly spaced** will be removed in **Settings->UI**.
  This was meant to be a bridge from Android 4.4 to Android 5.0 back in Fall 2014.
  It goes against modern Android design and has caused issues in the past for users who have
  unknowingly enabled it.
* [REF] lint
* [ADD] SPC SWO Summary - similar to Severe Dashboard, move "pin to homescreen" icon to submenu so
  it's clear what this is doing
* [FIX] Nexrad radar city textual labels were not working well on ChromeOS, change the way size is
  computed.

## 55857 2022_12_20

* [REF] C source code cleanup
* [ADD] hardcode value in styles.xml as possibly required by future Material update, example
* [ADD] UtilityCapAlert
* [REF] rename activitiesmisc/USWarningsWithRadarActivity.kt -> activitiesmisc/USAlertsActivity.kt

```
<item name="popupMenuBackground">@color/off_white</item>
```

## 55856 2022_12_14

* [ADD] NDK update android-ndk-r25c -> android-ndk-r26b
    - add variable in `build.gradle` for NDK path
    - `app/src/main/jni/genCircleWithColor.c` required type definition in func for C99 compliance (
      should have been there from start)

* [ADD]  okhttp minor update

```
-    implementation "com.squareup.okhttp3:okhttp:4.11.0"
+    implementation "com.squareup.okhttp3:okhttp:4.12.0"
```

* [REF] gradle 9.0 will require this being removed

```
--- a/gradle.properties
+++ b/gradle.properties
@@ -20,7 +20,6 @@ org.gradle.jvmargs=-Xms2048m -Xmx4096m -XX:ReservedCodeCacheSize=2048m
 android.useAndroidX=true
 # com.jjoe64:graphview:4.2.2 requires jetifier and legacy support libraries
 android.enableJetifier=true
-android.defaults.buildfeatures.buildconfig=true
 android.nonTransitiveRClass=true
 android.nonFinalResIds=true
 #In Android Studio, just go to Fi
```

## 55855 2022_12_04

* [FIX] KLIX (LA, New Orleans) nexrad radar is being physically moved.
  This update prevents it from being used as an active radar in long press radar
  selection or if adding a new location.
* [REF] tooling update:

```
-        classpath 'com.android.tools.build:gradle:8.1.4'
+        classpath 'com.android.tools.build:gradle:8.2.0'

-distributionUrl=https\://services.gradle.org/distributions/gradle-8.0-all.zip
+distributionUrl=https\://services.gradle.org/distributions/gradle-8.2-all.zip
```

## 55854 2022_11_28

* [FIX] WPC US Hazards Outlook Days 3-7: product discontinued via SCN23-101: Termination of the
  Weather Prediction Center Day 3-7
  Hazards Outlook Discussion Effective November 15, 2023
* [REF] cleanup in DownloadText.kt

## 55853 2022_11_25

* [FIX] NHC storm detail - force to mph for wind intensity
* [ADD] Android Studio update twice and tool update

```
-        classpath 'com.android.tools.build:gradle:8.1.2'
+        classpath 'com.android.tools.build:gradle:8.1.3'
```

```
-        classpath 'com.android.tools.build:gradle:8.1.3'
+        classpath 'com.android.tools.build:gradle:8.1.4'

-    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.0.3'
+    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.0.4'
```

## 55852 2022_11_05

* [ADD] Nexrad radar widget - load geometry data locally in an attempt to make widget more reliable

## 55851 2022_11_03

* [FIX] remove 3 obsolete Canadian text products in the National Text Viewer (MISC Tab, 2nd row,
  left)

## 55850 2022_11_02

* [ADD] version bump for internal testing
* [FIX] NWS changed some of the URLs for the SST images accessed in the NHC activity

## 55845 2022_10_27

* [REF] lint

## 55844 2022_10_26

* [ADD] target API33 until after May 1, 2024 - at that time screen recorder will be removed along
  with drawing tools
* [ADD] as communicated in "Upcoming changes": after support for Android 7.1 is removed
  the option **Settings->Radar->Launch app directly to radar** will be removed since Android 8.0 and
  higher supports *static pinned launchers* i.e. if you long press on the app icon in the android
  home screen you can launch the radar directly and also setup another icon to do so.

## 55843 2022_10_26

* [REF] remove the deprecated media controller

## 55842 2022_10_26

* [ADD] per URL add to Manifest
  android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION https://developer.android.com/about/versions/14/changes/fgs-types-required#use-cases

## 55841 2022_10_22

* [FIX] crash in model animation seen occasionally in crash reports

## 55840 2022_10_22

* [FIX] disallow screen recorder on 12L as it's no longer working and will not be supported (see
  FAQ)
* [FIX] Simplify and hopefully fix some issues (with all white theme) with nexrad multipane as seen
  across various devices

## 55839 2022_10_22

* [FIX] misc lint
* [ADD] remove option "Use AWC Radar Mosaic" and all supporting files since NWS has changed AWC
  website and removed radar mosaics
* [FIX] Simplify and hopefully fix some issues with nexrad multipane as seen across various devices

## 55838 2022_10_21

* [FIX] VideoRecordActivity.kt use isStoragePermissionGranted from AudioPlayActivity
* [FIX] in support of API34 grants perms in manifest <uses-permission android:name="
  android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION" />
* [FIX] in support of API34 in TelecineService.kt use ServiceCompat.startForeground

## 55837 2022_10_20

* Target API 34 and upgrade support libs

```
-        compileSdk 33
+        compileSdk 34

-        targetSdkVersion 33
+        targetSdkVersion 34

-    implementation 'com.google.android.material:material:1.9.0'
+    implementation 'com.google.android.material:material:1.10.0'
-    implementation 'androidx.core:core-ktx:1.10.0'
+    implementation 'androidx.core:core-ktx:1.12.0'
-    implementation 'androidx.recyclerview:recyclerview:1.3.1'
+    implementation 'androidx.recyclerview:recyclerview:1.3.2'
```

This required a few changes in NexradRenderSurfaceView.kt and UtilityWidgetDownload.kt

```
-    override fun onFling(event1: MotionEvent, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = true
+    // API34 changed first arg to Optional
+    override fun onFling(event1: MotionEvent?, event2: MotionEvent, velocityX: Float, velocityY: Float): Boolean = true

-    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
+    override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
```

## 55836 2022_10_16

* [FIX] Nexrad windbarbs and observations in response to NWS planned AWC website
  upgrade: https://aviationweather.gov/

## 55835 2022_09_29

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

## 55834 2022_09_22

build.gradle:

```
-    implementation "com.squareup.okhttp3:okhttp:4.10.0"
+    implementation "com.squareup.okhttp3:okhttp:4.11.0"
```

## 55833 2022_09_18

build.gradle:

```
-    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4'
-    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4'
+    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'
+    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
```

## 55832 2022_09_10

* [ADD] All model activities (ie SPC SREF/HREF/HRRR, ESRL, NCEP, etc) - use floating buttons (
  similar to single pane) for back/forward
* [REF] lint and format (including menu and layout)

## 55831 2022_09_10

* [REF] lint and format (including menu and layout)
* [REF] widget_spcswo_layout.xml - remove unnecessary parent layout
* [REF] widget_layout_small.xml - remove unnecessary parent layout (used by newer 7day forecast
  widget)
* [REF] remove unnecessary parent layout: activity_uswxoglmultipane_quad.xml
  activity_uswxoglmultipane_quad_white.xml activity_uswxoglmultipane.xml
  activity_uswxoglmultipane_white.xml
* [ADD] move setting from UI into settings Developer as they are discouraged to change and will
  likely be deprecated in the future
  "Radar with transparent toolbars"
  "Radar with transparent status bar"
* [REF] per lint widget_7day_layout.xml

```
-        android:layout_height="match_parent"
+        android:layout_height="0dp"
```

## 55830 2022_09_10

* [REF] lint and format (including menu and layout)
* [ADD] in build.gradle change to "minSdkVersion 25" from "minSdkVersion 23" as mentioned in
  upcoming changes
* [ADD] in gradle.properties change "android.nonFinalResIds=false" to "android.nonFinalResIds=true"
* [ADD] migrate to use non-transitive R classes by going to Refactor > Migrate to Non-Transitive R
  Classes.

```
-        val attrs = intArrayOf(R.attr.colorPrimary, R.attr.colorPrimaryDark, R.attr.colorAccent)
+        val attrs = intArrayOf(androidx.appcompat.R.attr.colorPrimary, androidx.appcompat.R.attr.colorPrimaryDark, androidx.appcompat.R.attr.colorAccent)
	modified:   app/src/main/java/joshuatee/wx/settings/SettingsLocationGenericActivity.kt
	modified:   app/src/main/java/joshuatee/wx/ui/UtilityTheme.kt
	modified:   gradle.properties
	
-android.nonTransitiveRClass=false
+android.nonTransitiveRClass=true
```

## 55829 2022_09_09

* [REF] lint and format
* [FIX] In Settings->Color, button text color is now dark instead of set to the default color for
  that particular item. If the preference is a light color it was hard to see.

## 55828 2022_09_09

* [REF] lint and format

## 55827 2022_09_09

* [REF] lint
* [ADD] In NHC Storm, add titles so that the image viewer (screen shown when tapping on an image)
  has something in subtitle and when sharing
* [REF] In UtilityStorePreferences and ObjectLocation remove references to unused location data:
  "COUNTY$locNum" and "ZONE$locNum"

## 55826 2022_09_06

* [FIX] crash in Alert detail (that does not have polygons present) caused by recent change
* [FIX] more lint

## 55825 2022_09_06

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

## 55824 2022_09_05

* [FIX] SPC MCD and WPC MPD screen were not showing areas affected in subtitle
* [ADD] SPC Thunderstorm outlook will now show the image full width when there is only one graphic
  active later in the day
* [ADD] Radar Color Palette selection screen: after user chooses one show yellow border around image
* [ADD] Radar Color Palette selection screen: submenu had one entry, remove and show entry in
  toolbar ("Help")
* [FIX] Spc Fire weather and excessive rainfall were not working after recent change

## 55823 2022_09_05

* [ADD] Settings->Radar->Color Palettes add text label to floating buttons
* [ADD] Settings->Radar->Color Palettes->Edit add text label to floating button

## 55822 2022_09_05

* [ADD] Settings->Homescreen: add 2nd floating button

## 55821 2022_09_04

* [ADD] Observations activity: remove submenu and show "RTMA" button directly in toolbar
* [ADD] Alerts detail: use floating action button to access radar instead of icon in toolbar
* [ADD] Settings->Location: use Button with text for adding a new location
* [ADD] Settings->Location->Add: use Button with text for saving a new location
* [ADD] Spotters: use Button with text for navigating to spotter reports
* [ADD] Settings->Colors: Add subtitle to screen to indicate how to change color
* [ADD] MCD, MPD, Watch display activity: add radar button instead of icon in toolbar

## 55820 2022_09_04

* [REF] lint
* [FIX] force Level2 to use Kotlin instead of native C as it is not working well
* [FIX] per lint - git mv drawable/temp_* drawable-nodpi
* [REF] move perm handling for notif tts in settings->Notifications into Switch.kt
* [ADD] Radar Mosaics are no longer pulled from the AWC Website which is due to be upgraded on Sep
  12, 2023.
  It appears the mosaic graphics are no longer going to be provided so the default is to now use the
  graphics
  used prior to using AWC.

## 55819 2022_09_03

* [REF] JNI C-native lint

## 55818 2022_09_03

* [ADD] Beta label to Color Palettes in Settings->Radar
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

## 55817 2022_09_03

* [ADD] SPC Thunderstorm Outlooks will now scale graphic size to match how many images are shown
* [ADD] BitmapAttr object which wraps Bitmap and provides a String field to store url
* [FIX] in the NHC Storm activity do not show images that don't exist. For example storms staying
  over the ocean do not
  have QPF and excessive rainfall graphics.

## 55816 2022_08_30

* [ADD] NHC notifications will now be sent (less often) when the advNum changes for publicAdvisory (
  as opposed to lastUpdate for the storm)
  https://www.nhc.noaa.gov/CurrentStorms.json
* [ADD] Android Studio update and tooling upgrade

```
-        classpath 'com.android.tools.build:gradle:8.1.0'
+        classpath 'com.android.tools.build:gradle:8.1.1'
```

## 55815 2022_08_20

* [FIX] NHC Storm rainfall graphics were not working after NWS URL change

## 55814 2022_08_19

* [ADD] NHC Storm sharing now includes more content in the subject and text product included

## 55813 2022_08_12

* lib update

```
-    implementation 'androidx.preference:preference-ktx:1.2.0'
+    implementation 'androidx.preference:preference-ktx:1.2.1'
```

## 55812 2022_08_07

* [FIX] CPAC long range graphic is now 7 days instead of 5 (as accessed via main NHC activity)

## 55811 2022_08_06

* [FIX] CPAC long range graphic is now 7 days instead of 5 (as accessed via main NHC activity)

## 55810 2022_07_29

* Lint

## 55809 2022_07_29

* Lint
* tools update

```
-    implementation 'androidx.recyclerview:recyclerview:1.3.0'
+    implementation 'androidx.recyclerview:recyclerview:1.3.1'

-    compileSdkVersion 33
-    buildToolsVersion '33.0.0'
+//    compileSdkVersion 33
+//    buildToolsVersion '33.0.1'
+
+    defaultConfig {
+        compileSdk 33
+    }
```

```
-    task buildNative(type: Exec, description: 'Compile JNI source via NDK') {
+    tasks.register('buildNative', Exec) {
         def ndkDir = "../../../android-ndk-r25c"
         commandLine "$ndkDir/ndk-build",
                 '-C', file('src/main/jni').absolutePath,
@@ -50,7 +50,7 @@ android {
                 'all'
     }

-    task cleanNative(type: Exec, description: 'Clean JNI object files') {
+    tasks.register('cleanNative', Exec) {
         def ndkDir = "../../../android-ndk-r25c"
         commandLine "$ndkDir/ndk-build",
                 '-C', file('src/main/jni').absolutePath,

-    tasks.withType(JavaCompile) {
+    tasks.withType(JavaCompile).configureEach {
         compileTask -> compileTask.dependsOn buildNative
     }
```

## 55808 2022_07_29

* [ADD] Android Studio and tools upgrade

```
-        classpath 'com.android.tools.build:gradle:8.0.2'
+        classpath 'com.android.tools.build:gradle:8.1.0'
```

## 55807 2022_07_02

* [ADD] "Test Message" in SettingsNotificationsActivity.kt

## 55806 2022_06_28

* [ADD] Continue to deprecate use of Canadian weather data as mentioned in upcoming changes.

## 55805 2022_06_25

* [ADD] Ability to view state level SPC Convective Outlooks for Days 4 - 8
* [ADD] For all state level SPC Convective Outlooks show all products available. Reminder that you
  can tap on an image to view by itself or double tap to zoom in.

## 55804 2022_06_14

* [ADD] SPC Mesoanalysis, add sectors Intermountain West and Great Lakes

## 55803 2022_06_14

* [FIX] NSSL WRF model activity remove FV3 which is no longer a supported model at this upstream
  site
* [FIX] Textual labels in Nexrad that were recently forced to one line to avoid edge wrap should not
  apply to detailed observations

## 55802 2022_06_13

* [FIX] crash due to not handling empty list at
  joshuatee.wx.models.UtilityModelSpcSrefInputOutput.getRunTime line 47
  UtilityModelSpcSrefInputOutput.kt
* [FIX] NSSL WRF model activity was not working at all
* [FIX] SPC Meso, selected image was not being saved when using back arrow or keyboard in chromeOS

## 55801 2022_06_05

* [ADD] Continue to deprecate use of Canadian weather data as mentioned in upcoming changes.

## 55800 2022_06_04

* [ADD] Continue to deprecate use of Canadian weather data as mentioned in upcoming changes.

## 55799 2022_06_04

* [ADD] Continue to deprecate use of Canadian weather data as mentioned in upcoming changes.

## 55798 2022_06_03

* [ADD] NCEP MAG models (MISC Upper Left) update "MAG 4.0.0 - May 2023" described
  here [https://mag.ncep.noaa.gov/version_updates.php](https://mag.ncep.noaa.gov/version_updates.php)
* [FIX] NCEP MAG GFS-WAVE: corrections to some already existing labels

## 55797 2022_06_01

* [ADD] Excessive Rainfall Outlook activity (MISC Tab) now shows a Day 4 and Day 5 image. No
  dedicated text
  product exists similar to Day1-Day3 and so discussion is included in the PMDEPD "Extended Forecast
  Discussion"
  more details here:
  [Service Change Notice 23-55](https://www.weather.gov/media/notification/pdf_2023_24/scn23-55_ero_days_4_5_t2o.pdf)
  and
  [NWS Product Description Document - PDD](https://www.weather.gov/media/notification/PDDs/PDD_ERO_Days_4_5_T2O.pdf)

## 55796 2022_05_28

* [REF] remove remaining remnants of twitter, web class and icons
* [FIX] for Android 13 (API 33), no need to request storage permission in AudioPlayActivity.kt
* [REF] various lint mostly make private
* [REF] remove remaining remnants of canada support

## 55795 2022_05_26

* [FIX] As communicated in the "upcoming changes" document in April 2022,
  Canadian local forecast support is being removed.
  In support of this the ability to add new Canadian locations is being disabled.

## 55794 2022_05_26

* [FIX] Attempt to avoid text wrap in Nexrad radar: set single line in NexradRenderTextObject.kt via
  textView.setSingleLine() in 2 spots

## 55793 2022_05_25

* [FIX] NHC - replace retired 5 day outlooks with new 7 day outlooks

## 55792 2022_05_25

* [FIX] Thanks to F-Droid maintainer Licaon_Kter for fixing this issue that was preventing build on
  F-Droid

```
# gradle.properties
-org.gradle.jvmargs=-XX:MaxPermSize=2048m -Xms2048m -Xmx4096m -XX:ReservedCodeCacheSize=2048m
+org.gradle.jvmargs=-Xms2048m -Xmx4096m -XX:ReservedCodeCacheSize=2048m
```

* [ADD] AS update

```
# build.gradle
-        classpath 'com.android.tools.build:gradle:8.0.1'
+        classpath 'com.android.tools.build:gradle:8.0.2'
```

## 55791 2022_05_23

* [FIX] Access to twitter content for state and tornado is no longer working and has been removed.
  Please access via your browser if needed.

## 55790 2022_05_15

```
-        classpath 'com.android.tools.build:gradle:8.0.0'
+        classpath 'com.android.tools.build:gradle:8.0.1'

-    implementation 'com.google.android.material:material:1.8.0'
+    implementation 'com.google.android.material:material:1.9.0'
```

## 55789 2022_04_17

* [FIX] in SPC Mesoanalysis, 850mb version 2 when selected from bottom toolbar was bringing old
  850mb product
* [FIX] In single pane nexrad with toolbar showing, rainfall products are not updating text
  dynamically

## 55788 2022_04_14

* Android Studio upgrade and upgrade to Gradle 8.0

```
     sourceSets {
         main {
-            jni.srcDirs = [] //disable automatic ndk-build
+            //disable automatic ndk-build
             jniLibs.srcDir 'src/main/libs'
         }
     }
     
--- a/app/src/main/AndroidManifest.xml
+++ b/app/src/main/AndroidManifest.xml
@@ -20,7 +20,6 @@
 -->
 
 <manifest xmlns:android="http://schemas.android.com/apk/res/android"
-    package="joshuatee.wx"
     android:versionCode="55788"
     
     
     dependencies {
-        classpath 'com.android.tools.build:gradle:7.4.2'
+        classpath 'com.android.tools.build:gradle:8.0.0'


+++ b/gradle.properties
@@ -19,4 +19,7 @@ org.gradle.jvmargs=-XX:MaxPermSize=2048m -Xms2048m -Xmx4096m -XX:ReservedCodeCac
 #org.gradle.configureondemand=false
 android.useAndroidX=true
 android.enableJetifier=true
+android.defaults.buildfeatures.buildconfig=true
+android.nonTransitiveRClass=false
+android.nonFinalResIds=false

-distributionUrl=https\://services.gradle.org/distributions/gradle-7.5-all.zip
+distributionUrl=https\://services.gradle.org/distributions/gradle-8.0-all.zip

```

* [ADD] dep updates

```
-    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0"
+    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1"

-    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.0.2'
+    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.0.3'
```

## 55787 2022_03_24

* [FIX] NWS SPC has changed the URL/format type for SPC MCD and thus code updates were required
* [ADD] dep updates

```
-    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:1.1.8'
+    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:2.0.2'
```

## 55786 2022_03_21

* [ADD] update NDK

```
     task buildNative(type: Exec, description: 'Compile JNI source via NDK') {
-        def ndkDir = "../../../android-ndk-r25b"
+        def ndkDir = "../../../android-ndk-r25c"

     task cleanNative(type: Exec, description: 'Clean JNI object files') {
-        def ndkDir = "../../../android-ndk-r25b"
+        def ndkDir = "../../../android-ndk-r25c"
```

## 55785 2022_03_17

* [FIX] SPC Meso Violent Tornado Parameter (VTP) was not working as SPC changed the product ID
* [ADD] dep updates

```
- implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1"
+    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.0"
-    implementation 'androidx.recyclerview:recyclerview:1.2.1'
+    implementation 'androidx.recyclerview:recyclerview:1.3.0'
```

## 55784 2022_03_16

* [ADD] Widget with weather conditions will fall back to 2nd closest observation point if primary
  obs point is not updating in the past 2 hours (similar to main screen / notification)
* [ADD] tooling updates:

```
-        classpath 'com.android.tools.build:gradle:7.4.1'
+        classpath 'com.android.tools.build:gradle:7.4.2'
```

## 55783 2022_02_12

* [FIX] NWS Has removed static graphic for space weather: Estimated Planetary K index
  and replaced with a web accessible version for this product
  at https://www.swpc.noaa.gov/products/planetary-k-index
  if you use this data you could access via a browser, etc
* [ADD] tooling updates:

```
-    implementation 'com.google.android.material:material:1.7.0'
+    implementation 'com.google.android.material:material:1.8.0'

-        classpath 'com.android.tools.build:gradle:7.4.0'
-        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20"
+        classpath 'com.android.tools.build:gradle:7.4.1'
+        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0"
```

## 55782 2022_01_21

* [FIX] minor change in date picker accent color used in darker themes

## 55781 2022_01_18

* [FIX] crash in RTMA when network is down
* [REF] update Android Studio
* [ADD] NCEP Models (MISC Tab, upper left) has replaced model "ESTOFS" with "STOFS" (v1.1.1)
  per https://mag.ncep.noaa.gov/version_updates.php

```
-distributionUrl=https\://services.gradle.org/distributions/gradle-7.4-all.zip
+distributionUrl=https\://services.gradle.org/distributions/gradle-7.5-all.zip

     dependencies {
-        classpath 'com.android.tools.build:gradle:7.3.1'
+        classpath 'com.android.tools.build:gradle:7.4.0'
         classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20"
     }
```

## 55780 2022_01_15

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

## 55759 2022_01_05

* [ADD] Weather conditions notifications will fall back to 2nd closest observation point if primary
  obs point is not updating in the past 2 hours (similar to main screen)
* [FIX] date picker in SPC Storm reports had incorrect color when using dark themes
* [FIX] Settings -> HomeScreen , if one taps top back arrow after making changes it now restarts app
  properly similar to tapping bottom arrow
* [FIX] Metar.findClosestObsSite - add try/catch around sortBy to avoid Exception
  java.lang.IllegalArgumentException: Comparison method violates its general contract!

## 55758 2022_11_30

* [REF] simply DownloadText.byProduct using version as used in LsrByWfoActivity.kt and
  WfoTextActivity.kt
* [REF] remove apparent unnecessary code in CapAlert.kt using String.getHtmlSep()
* [REF] UtilityNetworkIO.kt consolidate to shared function:
  getStringFromUrl/getStringFromUrlWithNewLine
* [ADD] Text widgets AFD/HWO/National Text now match formatting as seen within the main app
* [REF] remove Utility.fromHtml in some places
* [FIX] SPC Storm report share was not including full detail

## 55757 2022_11_29

* [REF] For DownloadText Watch, MCD, MPD do not use getHtmlSep. Requires downstream changes in
  SpcMcdWatchShowActivity.kt

## 55756 2022_11_29

* [REF] replace String.getHtmlSep() with String.getHtmlWithNewLine() in UtilityCanada.kt
  NotificationSwo.kt NexradDownload.kt
* [REF] rename SpotterUtil to UtilitySpotter to match other ports
* [REF] deprecate UtilityString.getHtmlAndParseSep

## 55755 2022_11_29

* [REF] misc
* [REF] replace String.getHtmlSep() with String.getHtmlWithNewLine() in WpcFronts.kt
  SpcStormReportsActivity.kt SwoDayOne.kt ObjectMetar.kt Metar.kt SpotterUtil.kt

## 55754 2022_11_28

* [REF] misc

## 55753 2022_11_27

* [REF] converge ModelsSpcHrefActivity.kt / ModelsSpcSrefActivity.kt(delete)
* [REF] converge ModelsGenericActivity.kt / ModelsSpcHrrrActivity.kt(delete)
* [REF] NavDrawer.connect2 (rename to connect) in USWarningsWithRadarActivity.kt
  SpcCompmapActivity.kt SpcStormReportsActivity.kt

## 55752 2022_11_26

* [FIX] Settings -> Playlist: buttons were not aligned correctly
* [FIX] Nexrad - crash if enabling a geometry such as county lines
* [REF] use with keyword in certain cases

## 55751 2022_11_25

* [ADD] UtilityModels.getAnimation and use in most models
* [REF] ViewColorLegend

## 55750 2022_11_24

* [ADD] more work on RTMA
* [REF] UtilityUS

## 55749 2022_11_23

* [ADD] RTMA accessible via Observations (main submenu)

## 55748 2022_11_23

* [ADD] RTMA add times

## 55747 2022_11_22

* [ADD] RTMA add sectors
* [ADD] RTMA add dew/wind as homescreen options
* [REF] DownloadText - remove QPFPFD, product is no longer offered and was removed from WpcText some
  time ago

## 55746 2022_11_22

* [FIX] Local text product viewer navigation drawer background color was not consistent with rest of
  app
* [ADD] RTMA_TEMP (Real-Time Mesoscale Analysis) as a Homescreen option in the generic images menu

        more details on the product: [https://nws.weather.gov/products/viewItem.php?selrow=539](https://nws.weather.gov/products/viewItem.php?selrow=539)

        graphics are from here: [https://mag.ncep.noaa.gov/observation-type-area.php](https://mag.ncep.noaa.gov/observation-type-area.php)

## 55745 2022_11_14

* [ADD] SevenDayCollection.kt , use in ForecastActivity.kt / LocationFragment.kt

## 55744 2022_11_13

* [REF] move color picker stuff to externalColorChooser/
* [FIX] run time translation fix for SREF/ESRL/NSSL

## 55743 2022_11_13

* [REF] DatePicker.kt - don't use java.util.Calendar
* [REF] reduce import joshuatee.wx.util.* (ui/other)

## 55742 2022_11_13

* [FIX] HREF did not have drawer with correct colors (55734)

## 55741 2022_11_12

* [REF] CitiesExtended.kt, CountyLabels.kt use DoubleArray instead of Array<Double>
* [REF] UtilityString

## 55740 2022_11_11

* [FIX] TVS was not working

## 55739 2022_11_11

* [REF] SPC HRRR / SREF RunTimeData refactor
* [REF] ObjectDateTime.translateTimeForHourly
* [ADD] Hourly using old API no longer shows the date, just weekday/hour similar to Hourly with new
  API (uses ObjectDateTime.translateTimeForHourly)

## 55738 2022_11_10

* [FIX] ESRL RAP model was not working correctly

## 55737 2022_11_09

* [FIX] playlist fix (bad time string)

## 55736 2022_11_09

* [REF] misc refactor

## 55735 2022_11_08

* [REF] UtilityModelSpcHrrrInputOutput.getValidTime - use ObjectDateTime
* [REF] UtilityForecastIcon - chain up replace
* [FIX] NSSL WRF (and other NSSL models) run only once per day, not twice
* [REF] implement new ObjectDateTime.generateModelRuns using LocalDateTime
* [REF] implement new ObjectDateTime.currentHourInUtc() using LocalDateTime (WPC GEFS)

## 55734 2022_11_07

* [ADD] NCEP HREF now goes out to 48 hours (was 36)
* [ADD] (in progress) color and spacing changes for SPC Meso, SPC SREF, WPC Images, National Text
  nav drawer in files: MyExpandableListAdapter.kt activity_spcmeso.xml listrow_details.xml
  listrow_group.xml

## 55733 2022_11_06

* [ADD] Dawn/Dusk to sunrise card on main screen

## 55732 2022_11_06

app/build.gradle

- implementation 'com.google.android.material:material:1.6.1'

+ implementation 'com.google.android.material:material:1.7.0'

this caused an error (Duplicate class androidx.lifecycle.ViewTreeViewModelKt found in modules
jetified-lifecycle-viewmodel-ktx-2.3.1-runtime) so had to add
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1"
might be this
https://issuetracker.google.com/issues/238425626?pli=1

## 55731 2022_11_06

* [REF] remove lightning maps stuff
* [REF] move bzip2 from util/ to externalBzip2/
* [REF] canvas nexrad now uses UtilityIO.uncompress
* [REF] sunrise/sunset and icon determination work - ObjectMetar.kt UtilityTimeSunMoon.kt
* [ADD] replace
  ExternalSunriseLocation/ExternalSunriseSunsetCalculator/ExternalSolarEventCalculator/ExternalZenith
  with externalSolarized/

## 55730 2022_11_02

* [ADD] SPC HREF now has image layers stored in the application bundle so response should be
  slightly better with less data downloaded.

## 55729 2022_11_02

* [ADD] back SPC HREF "Reflectivity: stamps" including proper background
* [ADD] remove option in Settings->UI, "GOES GLM for lightning (requires restart)". Lightning Maps
  is being phased out and is no longer an option.

## 55728 2022_11_02

```
-        classpath 'com.android.tools.build:gradle:7.3.0'
-        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10"
+        classpath 'com.android.tools.build:gradle:7.3.1'
+        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20"
```

* [ADD] back SPC HREF reflectivity under heading "member viewer" including an additional layer that
  shows the colormap and time
* [FIX] from the national text product viewer remove the following which retired some time ago: "
  pmdsa: South American Synoptic Discussion"
* [FIX] seven day forecast with verbiage "Light (.*?) wind increasing" was not being parsed and
  displayed in the 7 day card header with temp/wind speed

## 55727 2022_10_11

* [REF] in CitiesExtended.kt CountyLabels.kt Metar.kt

## 55726 2022_10_05

* [REF] Level 3 text prod decode is to complicated
* [ADD] DownloadTimer now takes 2nd arg with default (timeout value)
* [ADD] Use DownloadTimer in SPC Fire Sum, SPC Swo Sum/Day, and WPC Rainfall Sum

## 55725 2022_10_04

* [FIX] Change RID distance from Double to Int to see if it helps with crash reports in Metar.kt

## 55724 2022_10_03

* [REF] sync up tdwr array with swift port (GlobalArrays.tdwrRadars - colon sep)
* [ADD] NHC / NHC Storm Download Timer
* [ADD] NHC needs onrestart
* [REF] breakup UtilityRadar
* [REF] move util get x,y for sites into UtilityLocation - make private if possible

## 55723 2022_10_03

* [REF] rename various classes to remove unnecessary "Object" verbiage
* [REF] add interface Widget and start to use
* [REF] for UtilityLocation add OfficeTypeEnum with WFO, RADAR, SOUNDING (standardize case)
* [REF] in PolygonWaring/Watch rename to byType

## 55722 2022_10_02

* [FIX] NWS radar mosaic sector lat/lon SOUTHMISSVLY
* [REF] rename various classes to remove unnecessary "Object" verbiage

## 55721 2022_09_19

* [FIX] nexrad - not all storm tracks were being drawn if enabled
* [REF] use new Level 3 text product function for VWP, STI, HI, and TVS

## 55720 2022_09_18

* [REF] rename files in radar/ part3
* [FIX] model animations now start from current time position (not start) till end

## 55719 2022_09_17

* [REF] rename files in radar/ part2

## 55718 2022_09_17

* [REF] Android Studio update to Dolphin
* [REF] inspect code revealed resources that could be removed
* [REF] rename files in radar/ part1

```
-distributionUrl=https\://services.gradle.org/distributions/gradle-7.3.3-all.zip
+distributionUrl=https\://services.gradle.org/distributions/gradle-7.4-all.zip

-        classpath 'com.android.tools.build:gradle:7.2.2'
+        classpath 'com.android.tools.build:gradle:7.3.0'
```

## 55717 2022_09_17

* [REF] move additional code to NexradRenderRadar.kt

## 55716 2022_09_16

* [REF] move additional to NexradRenderConstruct.kt
* [FIX] NHCStorm images 3,4 don't work in ImageViewer (filter causes some images to be skipped)

## 55715 2022_09_16

* [REF] move additional to NexradRenderConstruct.kt
* [ADD] NexradRenderState.kt

## 55714 2022_09_15

* [REF] break UtilityDownload into 2 files (DownloadImage.kt and DownloadText.kt)
* [REF] move WXGLRender.colorSwo to UtilitySwoDayOne.colors
* [REF] create NexradRenderConstruct.kt and move WPC Front / SWO constructors there

## 55713 2022_09_15

* [ADD] use DownloadTimer in LocationFragment.kt

## 55712 2022_09_14

* [FIX] In the main nexrad viewer (lightning icon), if you access settings->radar->colors and change
  the nexrad background the change will be immediate

## 55711 2022_09_14

* [REF] move animation code into new class NexradAnimation

## 55710 2022_09_14

* [REF] nexrad - move more GPS/auto-update/animation code into NexradUI
* [FIX] In multipane radar if animate and then long press to switch radar site, animation keeps
  playing old site after switch

## 55709 2022_09_13

* [FIX] Hourly will remain a small text size regardless of text size chosen in settings->UI (which
  can lead to an unusable display of text if too large)
* [REF] nexrad - move GPS/auto-update code into NexradUI

## 55708 2022_09_13

* [REF] misc refactor - nexrad
* [FIX] attempted fix for quad pane nexrad frame sizing issue

## 55707 2022_09_12

* [REF] misc refactor - LocationFragment.kt

## 55706 2022_09_12

* [REF] misc refactor - WXGLSurfaceView.kt LocationFragment.kt
* [ADD] utilUI assume gestures (no nav bar) are being used in Android 13 or higher (nexrad multipane
  sizing)
* [ADD] Settings -> Playlist, if user adds a product that is already there, have the popup
  notification close after 3 sec instead of requiring user to close
* [ADD] optional duration param to ObjectPopupMessage
* [ADD] Add location screen, save status popup now closes automatically after 3 seconds
* [ADD] bring back prior back button handling in web based activities
* [FIX] if multiple nexrad on homescreen in some situations radar label in top current conditions
  card was not fully correct

## 55705 2022_09_10

* [FIX] seven day notification and legacy cc/7day widget has errant newlines in 7 day listing
* [FIX] legacy widget timestamp has dark text
* [FIX] legacy widget icons are not the same color (white vs off-white)
* [REF] Spotter / SpotterReports to store LatLon instead of String/Double
* [REF] cleanup WXGLTextObject
* [REF] find forEach loops that use .indices that should not

## 55704 2022_09_09

* [REF] misc refactor - settings->homescreen, reduction in url download methods in
  UtilityDownloadNws
* [FIX] update NWS Radar Mosaic URL in response
  to [PNS22-09](https://www.weather.gov/media/notification/pdf2/pns22-09_ridge_ii_public_local_standard_radar_pages_aaa.pdf)
* [REF] remove usage of UtilityDownloadNws.forecastZone in settings->about
* [REF] remove the need for UtilityUS.obsClosestClass
* [ADD] SevenDayDataLegacy for use in UtilityUS
* [REF] rename UtilityUS.getCurrentConditionsUS -> UtilityUS.getSevenDayLegacy
* [ADD] The location based "7day" notification will now open a normal forecast view instead of a
  plain text 7 day listing

## 55703 2022_09_08

* [REF] misc refactor - favorite management

## 55702 2022_09_07

* [REF] misc refactor
* [REF] rename in UtilityHomeScreen
* [REF] ObjectNhc in prep for usage by notif (instead of UtilityNhc)
* [ADD] move from android-ndk-r23b to android-ndk-r25b
* [FIX] SPC Meso multipane not saving x/y/zoom on exit
* [REF] Misc/Spc Tab ObjectTabScreen (take Activity, View, String, and List<TileObject>)
* [REF] more methods in WX.kt to break up constructor
* [FIX] nexrad invoked from alert will not keep site when jump to multipane

```
-    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0'
-    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0'
+    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4'
+    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4'

-    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:1.1.5'
+    coreLibraryDesugaring 'com.android.tools:desugar_jdk_libs:1.1.8'
```

## 55699 2022_09_06

* [REF] misc refactor
* [REF] XML layout/Menu cleanup - part 1
* [ADD] AWC beta website API to obs sites
* [FIX] SPC Compmap navigation drawer on/off for each parameter was not showing correctly

## 55698 2022_09_05

* [REF] refactor in notif code cont
* [REF] ObjectNotification.send: create the notification as well (
  createNotificationBigTextWithAction)
* [FIX] SPC Meso - layers topo and county were not working together
* [ADD] SPC Meso - add more layers: Observations/Population (Topo and Population can't be shown
  together)
* [ADD] Settings -> UI "Navigation Drawer Configuration" will show regardless of whether or not
  drawer is enabled

## 55697 2022_09_03

* [ADD] Settings->Main, update Notifications label if App not allowed to send
* [REF] ObjectNotification, take objectPendingIntents once instead of 2 member vars
* [REF] ObjectNotification, consolidate down to one: noBody / noSummary are redundant
* [REF] Notifications - remove variation in LED colors, most were Color.Yellow
* [REF] remove context from ObjectNotification.sendNotification

## 55696 2022_09_02

* [REF] refactor - move all widget stuff into widgets/ (save top-level which must remain there)
* [REF] don't use short notation AFD vs WidgetFile.AFD
* [FIX] SPC convective outlook notif icon was displaying as all black
* [REF] WPC MPD/SPC MCD notifications - share common send code between CONUS and location specific
* [FIX] convective outlook notification icon not working
* [FIX] SPC MPD / Watch notification action was not working

## 55695 2022_09_02

* [REF] refactor in BackgroundFetch - move tor/wat/mcd/mpd to different files
* [ADD] download timer added for backgroundFetch

## 55694 2022_09_02

* [ADD] Add Location - add label that if notifications are blocked will be displayed. If on Android
  13 you can tap on it to open the perm dialogue
* [ADD] In BackgroundFetch.kt, check of notifications allow before even checking
* [REF] major refactor in BackgroundFetch.kt
* [REF] move widget download to dedicated method, remove from notif cc/7day

## 55693 2022_09_01

* [REF] misc refactor
* [FIX] migrate away from onBackPressed

## 55692 2022_09_01

* [REF] misc refactor
* [FIX] remove web widget in LocationFragment
* [FIX] Nexrad radar - if map is shown and then app is switched or screen is turned off, when
  returning to nexrad, hide the map
* [FIX] For Android 13, add android.permission.POST_NOTIFICATIONS and check for this when navigating
  to Settings->Notification

## 55691 2022_09_01

* [ADD] If running Android 13, screen recording is regrettably no longer working. Updated Nexrad to
  only show drawing tools. Updated all other image based activities
  to offer normal image sharing. Updated FAQ and Upcoming Changes
* [FIX] In Settings->Homescreen remove the "Web" option from the menu. This is meant as a workaround
  when NWS 7 day is not working. However, the web widget appears to no longer be working either.

## 55690 2022_08_31

* [FIX] UtilityWidgetDownload.nexrad needed obs for wind barbs
* [REF] misc refactor
  renamed:    app/src/main/java/joshuatee/wx/UtilityWidget.kt ->
  app/src/main/java/joshuatee/wx/util/UtilityWidget.kt
  renamed:    app/src/main/java/joshuatee/wx/UtilityWidgetDownload.kt ->
  app/src/main/java/joshuatee/wx/util/UtilityWidgetDownload.kt
* [ADD] Target Android 13

```
-    compileSdkVersion 32
+    compileSdkVersion 33
-    buildToolsVersion '31.0.0'
+    buildToolsVersion '33.0.0'
-        targetSdkVersion 32
+        targetSdkVersion 33
```

## 55689 2022_08_31

* [REF] misc refactor - spc meso, misc

## 55688 2022_08_30

* [ADD] recent changes in how polygon data is being downloaded and cached no longer require the
  background job to fetch it
* [ADD] DownloadTimer set warnings to 3 min instead of based off a variable that doesn't necessarily
  relate to it

## 55687 2022_08_30

* [REF] misc refactor
* [ADD] In activity to add or edit location, place cursor at end of text in label field

## 55686 2022_08_29

* [FIX] nexrad widget warning colors broke in recent refactor
* [REF] refactor warning handling in UtilityCanvas for NexradWidget

## 55685 2022_08_29

* [REF] misc refactor - SettingsLocationGenericActivity.kt
* [FIX] In activity to add/modify location, if you delete location, add new location - lat/lon is
  not blank and shows for the deleted location
* [ADD] Settings -> location add # of locations in title
* [FIX] settings -> location truncates actual stored lat / lon too much - add a few more characters

-        classpath 'com.android.tools.build:gradle:7.2.1'
-        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21"

+        classpath 'com.android.tools.build:gradle:7.2.2'
+        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10"

## 55684 2022_08_28

* [REF] nexrad widget, refactor and re-order some of the drawing
* [REF] remove unused ProjectionType.WX_OGL_48
* [ADD] Settings->Radar has been split it two activities. The line/marker (tvs/hail) size settings
  can now be accessed
  via a second activity accessible from the "Line / Marker sizes" button 3rd from the top
* [FIX] SPC SREF - when using left/right button to move through model images, image was download
  twice instead of just once

## 55683 2022_08_28

* [REF] misc refactor - cleanup in canvas nexrad and projection type

## 55682 2022_08_27

* [REF] misc refactor - remove Route.image that fills background to white (test NHC)
* [ADD] In US Alerts submenu, show Alaska instead of AK (and same for HI)
* [FIX] In nexrad widget, draw county line below state-line
* [ADD] use enum for fav mgmt

## 55681 2022_08_27

* [REF] misc refactor
* [ADD] Nexrad radar widget now respects geographic line size setting as used for the main program
  Nexrad

## 55680 2022_08_27

* [REF] misc refactor
* [ADD] long press radar status message shows nearest radar to where long press occurred - not radar
  currently selected
* [FIX] handle RSM (radar status message) better for terminal radars
* [FIX] SPC SWO Day X State graphic - don't like AK/HI in menu as SPC only covers CONUS
* [FIX] Revert this change as it breaks the radar on the main screen when "center on location" is
  enabled: location dot on main screen will continue to be dot with circle around it (for now)

## 55679 2022_08_24

* [REF] misc refactor

## 55678 2022_08_23

* [FIX] nexrad autorefresh fix related to onresume/pause or restart (use onresume and not both)
* [ADD] nexrad multipane "ctrl-a" will now toggle an animation on/off (similar to how single pane
  has been)
* [ADD] nexrad multipane "ctrl-r" (ref) and "ctrl-v" (vel) added (similar to how single pane has
  been) (use alt-? on main screen and nexrad to show shortcuts)
* [ADD] additional helper Nexrad* classes

## 55677 2022_08_22

* [REF] misc refactor
* [FIX] nexrad autorefresh fix related to onresume/pause or restart

## 55676 2022_08_22

* [REF] misc refactor
* [FIX] In single pane nexrad, if animating, switching radars via the map was not working
* [FIX] nexrad help in submenu needed to be freshened up a bit
* [FIX] The main screen will no longer show the primary "location dot" surrounded by a circle as
  this shape is reserved from the dedicated
  nexrad viewer which if configured does use active GPS location
* [ADD] NexradLongPressMenu and changes in NexradState* and NexradArguments* to make it happen

## 55675 2022_08_21

* [REF] NexradDraw, reduce arg count with NexradState
* [REF] VideoRecordActivity.kt, remove obsolete SDK checks after going to 23
* [REF] nexrad convert locXCurrent to double
* [ADD] Nexrad long press - shorten (since large text goes off screen) and make verbiage consistent
  with iOS port wXL23
* [FIX] adhoc location forecast activity (via nexrad long-press) had to many digits for lat/lon

## 55674 2022_08_20

* [REF] more work on NexradState for LocationFragment
* [REF] LocationFragment, remove paneList and SDK check 23 related to activity

## 55673 2022_08_20

* [REF] more work on NexradState
* [ADD] Nexrad multipane, if you tap on the text in the upper left that lists radar site/product it
  will take you to severe dashboard (similar to single pane)
* [ADD] Nexrad multipane, if warnings(tor/ffw/tst) enabled, show in top toolbar similar to single
  pane
* [REF] Nexrad multipane, remove idxIntAl, it is redundant to NexradState.curRadar

## 55672 2022_08_20

* [REF] use NexradState in multi pane Nexrad

## 55671 2022_08_19

* [REF] nexrad - migrate away from paneList
* [FIX] tap on any NHC notification would cause a crash (user reported)
* [FIX] NHC graphics for ATL storms
* [FIX] wxglrender @Synchronized fun constructWarningLines(polygonWarningType: PolygonWarningType) {
* [REF] use NexradState in single pane Nexrad

## 55670 2022_08_19

* [REF] nexrad work, objectImageMap, getContent, remove: restartedZoom, oglInView, firstRun,
  mapShown, restarted, tiltOption, animRan

## 55669 2022_08_19

* [ADD] additional download threads to "Severe Dashboard"
* [ADD] nexrad multi-pane, remove single threaded getContent, use @Synchronize for spotters if
  needed (or other)
* [REF] move getImageForShare in nexrad* into utilRadarUI

## 55668 2022_08_18

* [ADD] severe dashboard - warnings/watch/mcd/mpd are downloaded in parallel
* [REF] WXGLRender.constructStiLines - use constructLines and not constructLinesShort (and change in
  NexradLayerDownload)
* [REF] WXGLRender - changes in wind barbs layout
* [REF] Nexrad - remove some archiveMode used at granular level
* [ADD] to meet the Oct 2022 announcement - raise minSDK to Android 6 API 23 from API 21
* [REF] nexrad remove var touchImage: TouchImageView2
* [ADD] NexradArguments - dedicated class to handle nexrad arguments

## 55667 2022_08_17

* [REF] remove some dependence on PolygonWarningType
* [FIX] Day 8-14 Hazard Outlook is to large to display via default options
* [FIX] in Color Palette Editor remove the unimplemented "help" option
* [FIX] in Color Palette Editor remove the "load from file" option - please use copy/paste instead.
  It is not worth the complexity to leave this in place
* [ADD] in settings->radar WPC Fronts line size is now a configurable item

## 55666 2022_08_17

* [REF] LocationFragment.kt: val cardViews = mutableListOf<CardView>() -> val cards =
  mutableListOf<Card>()
* [ADD] Us Alerts - if in landscape do 2 columns
* [ADD] Nexrad radar has had a large chunk of it re-written to modernize the code and to help
  improve performance
*       by doing more work in parallel. Please do email me if any bugs are encountered.
* [ADD] For new NWS radar mosaic, tile icon in MISC tab will show the last viewed location. Main
  submenu will continue to show nearest image.
* [REF] add RadarGeomInfo and revamp RadarGeometry
* [ADD] in Nexrad single pane some settings changes that previously required restarting the nexrad
  view no longer require (like adding CA/MX borders)

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
* [ADD] Nexrad on main screen and multipane nexrad now show SPC Convective Outlooks if configured to
  do so
* [ADD] NexradDraw and move content from UtilityRadarUI
* [FIX] Nexrad long press observation dialogue was not respecting the text size setting
* [FIX] Nexrad new - wpc fronts exceptions (@Synchronized fun constructWpcFronts())

## 55661 2022_08_14

* [REF] misc refactor
* [FIX] new radar was not launching via static shortcut
* [FIX] nexrad - long press and select the "Beam Height" entry changed the radar site, it should do
  nothing

## 55660 2022_08_14

* [REF] misc refactor
* [FIX] MCD/MPD/Watch viewer won't crash if text product is not yet available
* [FIX] SevereDashboard will now only show "Done" in title if all downloads are done
* [ADD] Move new Nexrad Radar file into place

## 55659 2022_08_14

* [REF] misc refactor
* [ADD] move from UtilityDownloadWarnings to ObjectPolygonWarning for TOR/TST/FFW (will need a few
  weeks of testing)

## 55658 2022_08_14

* [REF] misc refactor
* [ADD] Nexrad radar option "Show radar during a pan/drag motion" which is enabled by default to
  mimic historic behavior
* [FIX] download warnings in NexradLayerDownload which is now used by LocationFragment, Multipane,
  and Nexrad new

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
* [FIX] if user chose to not use main screen radar floating action button and also chose to use a
  navigation drawer, the radar FAB would not show
* [FIX] adhoc forecast via long press in nexrad was not showing correct sunrise/sunset data
* [REF] add SunRiseCard and use in LocationFragment and ForecastActivity
* [REF] WXGLRadarActivityMultiPane.kt refactor getContent to use FutureVoid
* [FIX] If audio was previously paused, going into another activity with text to speech controls
  shows text "stop button" instead of icon

## 55655 2022_08_06

* [REF] misc refactor

## 55654 2022_08_05

* [FIX] TouchImageView2.java, add try/catch in onDraw to handle images that are to large. Show
  nothing but don't crash

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

* [ADD] ImageMap.java - change base class from android.widget.ImageView to
  androidx.appcompat.widget.AppCompatImageView
* [FIX] ObjectCurrentConditions.kt - if the heat index and the temp (both rounded) are equal, don't
  show heat index
* [ADD] SPC MCD/Watch summary icons in SPC Tab now show only images regardless of how many Watch or
  MCD there are. This resolves one bug
  and makes the interface more predictable. Please note that the "Severe Dashboard" accessible on
  the main screen via octagon icon
  is the preferred method to see MCD/Watch. When the Navigation drawer is configured as compared to
  Tabs they are not even visible.
* [ADD] SPC MCD/Watch summary - if no watch or mcd is present use a bigger font with a "high light"
  color
* [ADD] SPC MCD/Watch summary - add timer and onrestart
* [ADD] SPC MCD/Watch single - add onrestart
* [REF] ObjectDateTime migration
* [ADD] In severe dashboard if no warnings/watch/mcd/mpd are present show "None" in subtitle

## 55647 2022_08_02

* [REF] misc refactor (radar incl)
* [FIX] deprecations in UtilityUI.immersiveMode, Nexrad related to transparent status bar

## 55646 2022_08_01

* [REF] misc refactor
* [ADD] In Settings -> Homescreen the menu option "Set to default (Canada)" is being removed in
  preparation for CA content removal by end of next year.
* [ADD] SettingsLocationGenericActivity.kt in onRequestPermissionsResult add super call, remove
  unused VR code
* [ADD] Settings -> celsius to fahrenheit table was rounding to integer but still showing with ".0"
  at the end - remove
* [FIX] in response to pixel 6a having main screen nav drawer truncating top entry made these two
  changes:
* [FIX] in nav_header_main.xml change the main LinearLayout from android:layout_height="
  @dimen/nav_header_height" to android:layout_height="match_parent"
* [FIX] in nav_header_main.xml change the main LinearLayout from android:gravity="bottom" to
  android:gravity="center_vertical"
* [FIX] in activity_main_drawer.xml and app/src/main/res/layout/activity_main_drawer_right.xml
  change NavigationView at bottom to android:fitsSystemWindows="false"
* [ADD] refactor Settings* to match desktop ports
* [ADD] restructure (MCD/MPD/Watch viewer) SpcMcdWatchShowActivity to download text and image in
  parallel
* [REF] ObjectCardCurrentConditions.kt, remove time arg for updateContent, rename to update
* [REF] ObjectCardCurrentConditions.kt, rename "updateContent" to "update"
* [REF] ObjectCardCurrentConditions.kt, remove "bitmap: Bitmap" as first arg to "update"
* [REF] ObjectCard7Day taking url instead of bitmap as 2nd arg
* [REF] rename ObjectCard7Day to SevenDayCard
* [REF] fix deprecated startActivityForResult, onActivityResult in SettingsNotificationsActivity.kt,
  CommonActionBarFragment.kt
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

## 55645 2022_07_29

* [ADD] ObjectAnimate.kt to be used in RadarMosaicNwsActivity/SPC Meso/Vis
* [REF] cut-over WATCH to ObjectPolygonWatch
* [ADD] Vis/SPC Meso/Radar Mosaic now have a play/stop/pause bottom for animations that is more
  appropriate to the circumstance and similar to Nexrad
* [ADD] In GOES/SPC Meso animate icon now uses the number of frames (default 10 - can be change in
  settings->UI), similar to nexrad
  a submenu in the main menu will allow access to the other frame count choices

## 55644 2022_07_27

* [FIX] locfrag continued attempts to fix crash on app launch on older versions

## 55643 2022_07_27

* [ADD] locfrag.getRadar - move to FutureVoid. (ran into issues on initial attempt, not able to
  replicate failures in google pre-launch but did find emulators with older APIs to replicate)
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
* [ADD] If device is in landscape some activities will show 3 images across instead of just 2 (SPC
  Convective outlook, fire, wpc rainfall, etc)

## 55636 2022_07_25

* [REF] misc refactor

## 55635 2022_07_25

* [REF] misc refactor
* [ADD] ObjectDatePicker for SpcStormReportsActivity
* [ADD] SpcStormReportsActivity - add subtitle indicating you can tape the image to open a date
  picker

## 55634 2022_07_24

* [REF] misc refactor

## 55633 2022_07_23

* [REF] misc refactor

## 55632 2022_07_23

* [REF] misc refactor
* [FIX] 7day not honoring C/F setting
* [REF] ObjectTouchImageView -> TouchImage
* [REF] DisplayDataNoSpinner and WpcImagesActivity were not using TouchImage (using raw
  TouchImageView2)

## 55631 2022_07_22

* [REF] misc refactor

## 55630 2022_07_22

* [REF] misc refactor

## 55629 2022_07_22

* [FIX] slowness in LocationEdit search icon appearing by loading cities before the activity is
  displayed
* [FIX] SPC HREF - some images not working, more work is needed (non-production model service)
* [ADD] Settings->Notification: alphabetize with logical groups
* [ADD] Settings: alphabetize
* [FIX] In usalerts for sps (special weather statement) and other alerts - don't show radar button
  if no polygon data is present
* [REF] rename ObjectCardAlertSummaryItem to ObjectCardAlertDetail, move button connections into
  card

## 55628 2022_07_21

* [REF] add ObjectLocation
* [REF] move MyApplication.locations to Location

## 55627 2022_07_20

* [FIX] Nexrad: if non-default option to use hi-res State lines data, not all lines were showing

## 55626 2022_07_20

* [ADD] In US Alerts - if you touch blank space in the top toolbar or the label to the left it will
  open the navigation drawer
* [REF] In all activities when a context is needed replace "this@ForecastActivity" with "this" (
  example)

## 55625 2022_07_20

* [ADD] In GOES, AWC, and NWS radar mosaic if you touch blank space in the top toolbar or the label
  to the left it will open the navigation drawer

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

* [REF] remove animated gif share which is not working and is considered deprecated within the
  program

## 55618 2022_07_17

* [ADD] Nexrad radar long press - shorten verbiage (continued)
* [REF] UtilityRadarUI/WXGLSurfaceView change from MutableList to List in some areas
* [FIX] new NWS Radar Mosaic - remove menu with products that aren't available
* [FIX] NHC now shows complete summary information and text products for Central Pacific "CP"
  hurricanes
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

* [REF] continue moving content out of MyApp and into UIPreferences - *fav (favorites for misc
  activities)

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
* [ADD] Option to use new NWS Radar Mosaic (restart required). This is a temporary option as it
  appears likely the default mosaic will switch to NWS (away from AWC) later this year
* [REF] move patterns in RegExp that are only used in one class back into those classes
* [REF] Change Level2 data site back to nomads after maintenance in Apr 2022
* [ADD] radar.RadarGeometry and move items from MyApplication to it (ongoing)
* [REF] remove MyApplication.radarHwEnh, git rm ./app/src/main/res/raw/hw (option has been gone for
  some time)
* [REF] move numerous URLs and other val String into GlobalVariables (myapp is too big)

## 55607 2022_07_05

* [ADD] framework for new NWS Mosaic (to replace AWC later this year), main menu, widget, homescreen
  widget
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

* [FIX] terminal radar TICH (Wichita, KS) was not properly coded (was TICT), it is now available for
  use

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
* In addition to Android Studio update (and the various updates it brings along), update the
  following:

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
    -
  A [reminder](https://gitlab.com/joshua.tee/wxl23/-/blob/master/doc/FAQ.md#why-is-level-2-radar-not-the-default)
  on Level 2 support within wX

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

* [ADD] access to settings via hourly in case app won't load (can access via 4x4 blue widget ->
  hourly -> settings)

## 55595 2022_03_20

* [ADD] update about copyright and remove link to wxl23 ios version
* [ADD] SPC Meso - new "layer" county boundaries. Turn on from submenu -> Layers (NOTE: the state
  line boundary provided for the Radar Mosaic does not quite match up with the county layer. This
  can be observed from the SPC Website as well)
* [ADD] SPC Meso - new param: "Hodograph Map", access from "Wind Shear" submenu
* [ADD] The default hourly data provided (the new NWS API) does not reliably return results on the
  first download, added a retry mechanism.

## 55594 2022_03_06

* NWS is deprecating this website on 3/22 (substitute NWS observation point,
  etc): https://www.wrh.noaa.gov/mesowest/timeseries.php?sid=KCAR
  in favor of: https://www.weather.gov/wrh/timeseries?site=KCAR
  Thus updated the activity accessed through the "MISC" tab to reflect this

## 55593 2022_03_05

* [FIX] GOES GLM (lightning) animation was not working

## 55592 2022_02_26

* [ADD] (main screen) if closest observation point has data older then 2 hours, show data for the
  2nd closest obs point

## 55590 2022_02_23

* [FIX] prune the list of Observation points by removing 57 sites that had not updated in the past
  28 days
  This pruning will occur more frequently in the future to avoid a bad user experience
  In the future as the new NWS API stabilizes there might be a less manual (but still performant)
  way to handle this
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

* [ADD] disable new super-res products as NWS has not fully deployed
* [FIX] remove the following weather obs point in `stations_us4.txt` and `us_metar3.txt` as user
  reported it has not updated since Jan 26

```
IL,ROMEOVILLE/CHI  ,KLOT
KLOT 41.6 -88.1
```

## 55587 2022_02_12

* [ADD] nexrad: force 2153/2154 to use Kotlin and avoid native since we shouldn't need to use native
  anymore
* [REF] native c code format cleanup

## 55586 2022_02_12 (release should not be used for production yet due to N0B/N0G integration)

* [ADD] work in native C code (via JNI) to support super-res products

## 55585 2022_02_10 (release should not be used for production yet due to N0B/N0G integration)

* [ADD] NXB and NXG framework, Level3 super-res
    - https://www.weather.gov/media/notification/pdf2/scn21-96_sbn_super-res.pdf
    - only at KRAX so far https://tgftp.nws.noaa.gov/SL.us008001/DF.of/DC.radar/DS.00n1b/ missing
      lowest tilt
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

* [ADD] In Settings -> UI -> Navdrawer config, have top arrow respond in the same way that bottom
  arrow does when pressed
* [ADD] In Settings -> about, add navdrawer token string to assist in troubleshooting
* [ADD] com.android.tools.build:gradle:7.0.4 -> com.android.tools.build:gradle:7.1.0
* [FIX] remove observation point KSTF (Starkville, MS) as it's impacting users.
* [FIX] remove decommissioned text products
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

* [FIX] GOES Viewer, eep Eastern East Pacific image was not working after NOAA changed image
  resolution
* [ADD] implementation 'androidx.media:media:1.4.2' -> implementation 'androidx.media:media:1.4.3'
* [REF] Lint fix especially move aware from hardcoded text in layout/menu
* [REF] remove external links in Color Palette editor

## 55570 2021_10_22

* [FIX] wx.kt nav draw lightning entry was not correct, now matches misc frag

## 55569 2021_10_21

* release to F-Droid (tag)

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



* [ADD] [Target Android 14 API34](https://developer.android.com/google/play/requirements/target-sdk)
  As the URL explains Google requires application developers to target newer versions each year so
  that they can enforce newer security policies (and other things - see URL). You should see no
  differences within the app, please let me know if otherwise.
