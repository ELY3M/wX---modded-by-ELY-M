# wX app modded by ELY M.

Thank you to Joshua Tee for this app! 

This app is intended to be my replacmement of my favorite app called pykl3   
and I intended to keep this app open sourced!    

this app will be customized for me and anyone who wants.  

Joshua Tee is welcome to add any of changes I did.  

==================================================================

The apk is in Releases section. download from there   

https://github.com/ELY3M/wX---modded-by-ELY-M/releases  

==================================================================   

The screenshots of the app are at 
http://bmx3r.com/wx 

==================================================================

This app have been tested on Nintendo Switch and it worked very great on my Switch.  
you need android running on your Nintendo Switch for this app to work.   
you will need an exploitable Switch.  I feel that the newer Switch will be exploitable 
for homebrew or Alternative OS.  
http://bmx3r.com/wx/switch

==================================================================   

## TO DO List: 

* change up how Spotter icons is - green for active - yellow for idle - red for inactive based on their time of their last location report.  think I will use custom icon like in Pykl3
* conus radar overlay when you zoom out like on pykl3   
* add rda and tdwrs icon/names and add in options to enable/disable showing rdas/tdwrs    
* add warning list within radar  
* add LSR reports from SN spotters/option to turn on/off LSR reports  
* as for LSR reports.  I want to do same LSR reports as in the grlevel with the placefile to see how much snow the area gets.    
* move show color legend to bottom as a option.  there will be option to choose which side.     
* rename application id and change icon color so someone can run both versions of wX   
* 8-bit SRM like in pykl3  
* fix up the ability to change products in the multi-pane radar. it seem wont change products for me  
* android wear support. I own android wear watches and love them.      
* would need to make a function to read any new txt files in /wX/pal/ that is not added via the editor.  
* make VAD Wind Profile more better and friendly  like the one at cod.edu    
* color tables for SRM 
* get dbz and wind speed from point via long press - request by Mike T.      


#
## DONE!
==================================== 
* added ACRA Crash Logger from https://www.acra.ch and updated privacy policy because of acra crash logger.  This is temporary.  
* added Idle Time in Spotter Info popup.  Now, you can see how long they been idling since their last report time.   
* I finally fixed how hail icons works. I know it have been long time that radar was showing wrong hail icons for hail sizes. It should show correct hail icons for hail sizes from now on.  The hail icons are in wX folder on the phone memory or sd card.  This idea came from GRLevelx and PYKL3. this meant to allow users to change hail icons.  just have to use png and follow image names.         
* as temp fix, I switched conus radar to WPC for now.   I know the radar on weather.gov is changed and no longer using static gif files.   
* Added 3 fingers touch function to show conus radar from any zoom.  press and hold screen with 3 fingers to show conus radar map anytime.  
* Added in function to enable and disable showing radar.  idea/request by Mike T. - very great idea!   The radar show/hide function is in menu in tools section. NOTE:  the radar do not disappear or appear auto - you have to tap/move the screen then radar will disappear or appear.             
* reversed my old warning mod and cleaned up.   
* updated the code with Joshua many changes and added in zoom setting for conus radar to zoom out to (5-20-19) (Tornado day in OK!)   
* all watch crash bugs should be gone, hopefully!   
* updated the code with Joshuas changes with the warnings mod/settings (4-20-19).   
* updated the code with Joshuas changes (4-13-19).     
* Now, you can change the radar legend text color in the color settings - it was requested by Mike T.  
* Userpoints system is working but userpoints update/refresh is not done asap as you delete/add them.  you have to wait few mins or close the app to see updated userpoints.     
* added in internet check on start with option to turn on and off internet check.    
* get MSL and AGL Beam Heights are available in long press now.  
* Sun/Moon data are calculated from SunCalc library now - No More loading/parsing from website with broken SSL.   
* Special Weather Statements Polygons are working!  I had to create a list for SPS same way as how spotters are done in wX.  you should see matching texts for sps polygons now.      
* added hail icons and text for showing hail sizes on radar, still testing  
* added backup urls in getRadarStatus function.  
* show MPD text within text is done and working. - added in official wX 
* Conus Radar is being worked on... right now, It is not being plotted right  so keep it off in the radar settings  
* added base spectrum width - thanks to Joshua Tee for doing it first before me!  
* reading Watch texts within radar is working properly now.  
* reading MCD texts within radar is working properly now.  
* added setting for adjusting width of colorbar (color table on side if enabled) and text size too.  
* heading bug on GPS location is done and it have setting to enable and disable and can set bug size too.    
* added "displayhold" function as seen in pykl3 because I liked it and it helps very much with "GLThread" crashes! - added in offical wX
* added option to change default lines on radar in the radar settings.  
* added "show MCD text" in radar" - It only show last MCD right now.  
* added TVS Icon - replaced the tvs triangle with tvs.png - need more testing and adjusting - need more badass storms!  
* due to a bug in location dot size if not following gps, I added a new setting for location icon size  
* you are able to change location icon size now.  I increased the location size to 530 in the settings.    
* the gps location is loaded by location.png now.    
* for now, I put in cod.edu url to load up for VAD Wind Profile.  
* The BackupRestore function now backup everything that are in wX folder even your icons and any files.  
* added pal files backup/restore function in BackupRestore function  
* the color table editor is saving txt to /wX/pal/ now  
* reading from pal files on sdcard are done!  I had to redo checkfiles and permissions processing abit.   
* added enhanced vel color table - cool one that help you to see tornadoes.     
* fixed up my own vel table colors.     
* reverted the vel color table fix due to messed up color table in vel  
* added checkpalfiles function to check pal files and copy them if any missing * reading from files is not done yet! *       
* added Extreme Wind warnings (EWW) 
* added conus radar image auto-download in radaractivity, need to figure out how to plot it when the radar map gets zoom out like in pykl3  
* android permissions are pita! I put in perm library for filechecking for custom icons later.    
* changed the icon to black and cyan color so one can tell which version wX is running.    
* added backup/restore settings/pallette like in pykl3    
* added third button for SRM on radar.    
* separate warnings option on radar.. so can enable and disable individual warnings on radar. for example, you only want to see tor/svr and you can disable all others.  
* added SVS (severe weather statement)  
* added request for sn spotter info in the tap menu in the radar  
* Spotter Network intergretion for spotters to auto report your location (add your SN key in the settings)   
* zoom out bug fixed!  obs / spotters / tvs disappear when you zoom out.  

  
