grep -Po 'android:versionName="(.*)"' app/src/main/AndroidManifest.xml | sed -r 's/.*android:versionName="([^ ]+)".*/\1/'>versionname
set /p versionname=<versionname
del versionname
git add .  
git commit -m %versionname%
git push 
pause
