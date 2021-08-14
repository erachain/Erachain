@rem http://www.ffmpeg.org/download.html

rem https://stackoverflow.com/questions/34391499/change-video-resolution-ffmpeg
rem ffmpeg -y -i "%%i" -vf scale=480:-2,setsar=1:1 -c:v libx264 -c:a copy "%%~ni_shrink.mp4"

rem !!! use '-pix_fmt yuv420p for images converting - for FireFox!!! see https://github.com/ccrisan/motioneye/issues/1067

rem param 2 and 3 - for make small video

rem FireFox good!
"C:\Program Files\ffmpeg\bin\ffmpeg.exe" -i %1 -y -fs 512k -pix_fmt yuv420p -vcodec h264 -an -vf scale=256:-2,setsar=1:1 -crf %2 -r:v %3 %4

