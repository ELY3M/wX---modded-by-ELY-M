# TODOs

* Animate buttons in goes not working as intended. stop once animating should show latest image,
  play after pause should start from where it was
* Color Pal - vel not well developed
* Color Pal - have to select before delete even though subtitle shows
* rainfall - title is truncated, check other devices
* after 2023-10-01 change min api to 25 (7.1)
* after 2023-12-31 have the option Icons evenly spaced removed in Settings->UI.
* wx href 0 hour
* Beam height is truncated
* NCEP STOFS
* have all HTML get functions use user-agent header
* rename radarForLocation in locfrag -> radarForLocationIndex
* UtilityNotification.storeWatchMcdLatLon - move
* remove usage of String.getHtmlSep()
* NotificationSpcFireWeather
* [ADD] migrate from "android.enableJetifier=true" to "android.enableJetifier=false" in
  gradle.properties (com.jjoe64:graphview:4.2.2 requires jetifier and legacy support libraries)

## Refactor

./app/src/main/java/joshuatee/wx/spc/SpcStormReportsActivity.kt:import java.util.Calendar
./app/src/main/java/joshuatee/wx/models/UtilityModels.kt:import java.util.Calendar
./app/src/main/java/joshuatee/wx/notifications/AlertService.kt:import java.util.Calendar
CurrentConditions - metarSiteCache
HREF - reset zoom when change sector

## Bugs

* [FIX] ChromeOS: with obs enabled, zoom out via kbd - obs don't update location and never hides
* [FIX] nexrad crash with ConcurrentModificationException in NexradRender:
  data.wpcFrontBuffersList.forEach {
* [FIX] nexrad with cities, zoom in on location and then long press to current radar - text objects
  don't refresh
* [FIX] USAlerts state count is not accurate
* [FIX] chromeOS: text size for observations in nexrad
* [FIX] storm reports - having location follow gps breaks the location marker
* [FIX] if nexrad transparent toolbars are turned off (feature deprecated, will not fix), multipane
  does not use the entire area
* [FIX] if nexrad transparent toolbars are turned off (feature deprecated, will not fix), map of
  radar sites does not display properly
* [FIX] if nexrad transparent toolbars are turned off (feature deprecated, will not fix) and theme
  is all white, toolbar text and some icons do not show
* [ADD] move settings

## Features

* [ADD] more granular downloads for widgets "WIDGETS_ENABLED"
* [FIX] separate the cc/7day notif timer from the widget download timer
* [ADD] UtilityWidget.uriShareAndGenerate try some new suggested code for grant perms on
  fileprovider
* [ADD] debug option with circular log for downloads
* [REF] migrate all to Future* (BackgroundFetch)
* [ADD] nexrad pan 1:1 and configurable

## Deprecations

> Task :app:processReleaseMainManifest
> package="joshuatee.wx" found in source AndroidManifest.xml:
> /Users/josh/StudioProjects/wX/app/src/main/AndroidManifest.xml.
> Setting the namespace via a source AndroidManifest.xml package attribute is deprecated.
> Please instead set the namespace (or testNamespace) in the module's build.gradle file, as
> described
> here: https://developer.android.com/studio/build/configure-app-module#set-namespace
> This migration can be done automatically using the AGP Upgrade Assistant, please refer
> to https://developer.android.com/studio/build/agp-upgrade-assistant for more information.

* [FIX] onStop? (onBackPressedDispatcher see SettingsUI)onBackPressed deprecated
  https://stackoverflow.com/questions/72634225/onbackpressed-deprecated-what-is-the-alternative
  wX
* [FIX] UtilityWidget.kt: (56, 46): 'resolveActivity(Intent, Int): ResolveInfo?' is deprecated.
* [FIX] Utility.kt: (85, 46): 'getPackageInfo(String, Int): PackageInfo!' is deprecated.
  https://stackoverflow.com/questions/52977079/android-sdk-28-versioncode-in-packageinfo-has-been-deprecated
* [FIX] SettingsNotificationsActivity.kt: (146, 30): 'getParcelableExtra(String!): T?' is
  deprecated. (and ColorPicker, OpacityBary, SVBar, ValueBar, SaturationBar TelecineService)
  https://stackoverflow.com/questions/73019160/android-getparcelableextra-deprecated
* [FIX] NHCStorm 'getSerializableExtra(String!): Serializable?' is deprecated.
  https://stackoverflow.com/questions/72571804/getserializableextra-deprecated-what-is-the-alternative
* [FIX] WXGLRadarActivity.kt: (441, 22): onStatusChanged deprecated? (also
  WXGLRadarActivityMultiPane.kt: (497, 22): )
  https://stackoverflow.com/questions/71918743/android-locationlistener-onstatuschanged-deprecated-what-can-replace-it
* [ADD] 'getter for defaultDisplay: Display!' is deprecated. Deprecated in Java
* [ADD] 'getMetrics(DisplayMetrics!): Unit' is deprecated. Deprecated in Java
* [REF] (WorkManager) IntentService is deprecated (AudioService* and
  others) https://stackoverflow.com/questions/62138507/intentservice-is-deprecated-how-do-i-replace-it-with-jobintentservice
* [ADD] handle deprecations in
  UtilityUI https://stackoverflow.com/questions/62577645/android-view-view-systemuivisibility-deprecated-what-is-the-replacement
* [FIX] deprecated startActivityForResult onActivityResult (see example in
  SettingsNotificationsActivity.kt)

## User Requests

* [ADD] user request for metar homescreen widget
* [ADD] user request for multiple nexrad widgets of different sites (products?)
* [ADD] user request for no county lines in GOES
* [ADD] multiple users request import/export of settings
