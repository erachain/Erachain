@rem http://www.ffmpeg.org/download.html

rem https://stackoverflow.com/questions/34391499/change-video-resolution-ffmpeg
rem ffmpeg -y -i "%%i" -vf scale=480:-2,setsar=1:1 -c:v libx264 -c:a copy "%%~ni_shrink.mp4"

rem !!! use '-pix_fmt yuv420p -vcodec libx264' for FireFox instead h264 !!!

rem param 2 and 3 - for make small video

rem "C:\Program Files\ffmpeg\bin\ffmpeg.exe" -i %1 -y -fs 512k -vcodec h264 -an -vf scale=256:-2,setsar=1:1 -q:v %2 -r:v %3 %4

rem FireFox good!
"C:\Program Files\ffmpeg\bin\ffmpeg.exe" -i %1 -y -fs 512k -pix_fmt yuv420p -vcodec h264 -an -vf scale=256:-2,setsar=1:1 -q:v %2 -r:v %3 %4

