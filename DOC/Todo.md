# TODOs

## Sep 2025
getter/setter consistency
ObjectLocation fix handling of notif vars
UtilityStorePreferences.setDefaults use Utility.writePref
If Settings - Colors visited, refresh main screen (for Nexrad)
better way to handle text color in ViewColorLegend

## Bugs

* [FIX] fab spacing in location add and colorpal when gesture nav turned off
* [FIX] during recent AS updates
* [FIX] models don't show correct time when older run is selected
* [FIX] legend in nexrad doesn't match for tvs

```
Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/8.10.2/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.
```

* [ADD] Color Pal - vel not well developed
* [FIX] Color Pal - have to select before delete even though subtitle shows
* [FIX] Beam height is truncated
* [FIX] nexrad crash with ConcurrentModificationException in NexradRender:
  data.wpcFrontBuffersList.forEach {
* [FIX] nexrad with cities, zoom in on location and then long press to current radar - text objects
  don't refresh
* [FIX] USAlerts state count is not accurate
* [FIX] storm reports - having location follow gps breaks the location marker

## Features

* [ADD] edge-to-edge
    - https://developer.android.com/about/versions/15/behavior-changes-15#ux
    - https://developer.android.com/develop/ui/compose/layouts/insets
* [ADD] vr button when using nav drawer
* [ADD] more granular downloads for widgets "WIDGETS_ENABLED"
* [FIX] separate the cc/7day notif timer from the widget download timer
* [ADD] UtilityWidget.uriShareAndGenerate try some new suggested code for grant perms on
  fileprovider
* [ADD] debug option with circular log for downloads
* [REF] migrate all to Future* (BackgroundFetch)

## Refactor

* [ADD] option to not show graph in hourly
* [ADD] https://developer.android.com/studio/write/lint#config
* [REF] remove usage of legacy java.util.Calendar

```
./app/src/main/java/joshuatee/wx/models/UtilityModels.kt:import java.util.Calendar
./app/src/main/java/joshuatee/wx/notifications/AlertService.kt:import java.util.Calendar
```

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
