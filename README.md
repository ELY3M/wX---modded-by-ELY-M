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


## TO DO List: 

1. conus radar overlay when you zoom out like on pykl3 
2. get special weather statements working - not working due because they do not have vtec like all other warnings.  
3. add rda and tdwrs icon/names and add in options to enable/disable showing rdas/tdwrs   
4. allow custom icons! exp tvs and hail and gps location and cities dots - partily done.  
5. add read mcd/mpd text like read warnings text in the radar tap menu 
7. add warning list within radar
8. add LSR reports from SN spotters/option to turn on/off LSR reports
9. as for LSR reports.  I want to do same LSR reports as in the grlevel with the placefile to see how much snow the area gets.    
10. might 4th radar icon for cc 
11. move show color legend to bottom   
13. rename application id and change icon color so someone can run both versions of wX   
14. 8-bit SRM like in pykl3  
15. fix up the ability to change products in the multi-pane radar. it seem wont change products for me  
16. android wear support. I own android wear watches and love them.      
17. have color table editor save a file to /wX/pal/ on sdcard  - Might not, would need to make a function to read all txt files in /wX/pal/  




#
#
## DONE!
====================================  
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

  
