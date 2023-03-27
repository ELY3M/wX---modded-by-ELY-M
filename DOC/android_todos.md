SettingsPlaylistActivity
updateList* look the same

tabletInLandscape - move into base class

https://stackoverflow.com/questions/44170028/android-how-to-detect-if-night-mode-is-on-when-using-appcompatdelegate-mode-ni

look to replace all title = with call to setSubTitle, rename method
setTitleWithWarningCounts

location fragment, remove LOC_LAST_UPDATE
app/src/main/java/joshuatee/wx/fragments/LocationFragment.kt:        Utility.writePrefLong(MyApplication.appContext, "LOC_LAST_UPDATE", lastRefresh)

line 335 location fragment, can this be refreshTextSize
            it.setTextSize(TypedValue.COMPLEX_UNIT_PX, UIPreferences.textSizeNormal)

