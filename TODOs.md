

## TO DO List: 

1\. conus radar overlay when you zoom out like on pykl3   
2\. add rda and tdwrs icon/names and add in options to enable/disable showing rdas/tdwrs   
3\. allow custom icons! esp cities dots - hail/tvs/location image is done!.    
4\. add warning list within radar  
5\. add LSR reports from SN spotters/option to turn on/off LSR reports  
6\. as for LSR reports.  I want to do same LSR reports as in the grlevel with the placefile to see how much snow the area gets.    
7\. might 4th radar icon for cc  
8\. move show color legend to bottom as a option.  there will be option to choose which side.     
9\. rename application id and change icon color so someone can run both versions of wX   
10\. 8-bit SRM like in pykl3  
11\. fix up the ability to change products in the multi-pane radar. it seem wont change products for me  
12\. android wear support. I own android wear watches and love them.      
13\. would need to make a function to read any new txt files in /wX/pal/ that is not added via the editor.  
14\. make VAD Wind Profile more better and friendly  like the one at cod.edu    
15\. color tables for SRM and Spectrum Width  
16\. Userpoints system - request by Mike T.   
17\. get dbz and wind speed from point via long press - request by Mike T.   


#
## DONE!
====================================   
Userpoints system is working but userpoints update/refresh is not done asap as you delete/add them.  you have to wait few mins or close the app to see updated userpoints.     
added in internet check on start with option to turn on and off internet check.    
get MSL and AGL Beam Heights are available in long press now.  
Sun/Moon data are calculated from SunCalc library now - No More loading/parsing from website with broken SSL.   
Special Weather Statements Polygons are working!  I had to create a list for SPS same way as how spotters are done in wX.  you should see matching texts for sps polygons now.      
added hail icons and text for showing hail sizes on radar, still testing  
added backup urls in getRadarStatus function.  
show MPD text within text is done and working.   
Conus Radar is being worked on... right now, It is not being plotted right  so keep it off in the radar settings  
added base spectrum width - thanks to Joshua Tee for doing it first before me!  
reading Watch texts within radar is working properly now.  
reading MCD texts within radar is working properly now.  
added setting for adjusting width of colorbar (color table on side if enabled) and text size too.  
heading bug on GPS location is done and it have setting to enable and disable and can set bug size too.    
added "displayhold" function as seen in pykl3 because I liked it and it helps very much with "GLThread" crashes!  
added option to change default lines on radar in the radar settings.  
added "show MCD text" in radar" - It only show last MCD right now.  
added TVS Icon - replaced the tvs triangle with tvs.png - need more testing and adjusting - need more badass storms!  
due to a bug in location dot size if not following gps, I added a new setting for location icon size  
you are able to change location icon size now.  I increased the location size to 530 in the settings.    
the gps location is loaded by location.png now.    
for now, I put in cod.edu url to load up for VAD Wind Profile.  
The BackupRestore function now backup everything that are in wX folder even your icons and any files.  
added pal files backup/restore function in BackupRestore function  
the color table editor is saving txt to /wX/pal/ now  
reading from pal files on sdcard are done!  I had to redo checkfiles and permissions processing abit.   
added enhanced vel color table - cool one that help you to see tornadoes.     
fixed up my own vel table colors.     
reverted the vel color table fix due to messed up color table in vel  
added checkpalfiles function to check pal files and copy them if any missing * reading from files is not done yet! *       
added Extreme Wind warnings (EWW) 
added conus radar image auto-download in radaractivity, need to figure out how to plot it when the radar map gets zoom out like in pykl3  
android permissions are pita! I put in perm library for filechecking for custom icons later.    
changed the icon to black and cyan color so one can tell which version wX is running.    
added backup/restore settings/pallette like in pykl3    
added third button for SRM on radar.    
seperate warnings option on radar.. so can enable and disable indidvual warnings on radar. for example, you only want to see tor/svr and you can disable all others.  
added SVS (severe weather statement)  
added request for sn spotter info in the tap menu in the radar  
Spotter Network intergretion for spotters (add your SN key in the settings)   
seperate screen on and auto refresh into 2 options. easy!   
zoom out bug fixed!  obs / spotters / tvs disappear when you zoom out.  

  
