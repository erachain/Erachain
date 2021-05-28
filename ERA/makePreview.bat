@rem http://www.ffmpeg.org/download.html

rem https://stackoverflow.com/questions/34391499/change-video-resolution-ffmpeg
rem ffmpeg -y -i "%%i" -vf scale=480:-2,setsar=1:1 -c:v libx264 -c:a copy "%%~ni_shrink.mp4"

"C:\Program Files\ffmpeg\bin\ffmpeg.exe" -i %1 -y -fs 512k -vcodec h264 -vf scale=256:-2,setsar=1:1 -q:v %2 -r:v %3 %4

