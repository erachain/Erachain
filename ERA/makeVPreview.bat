rem @echo off
@rem http://www.ffmpeg.org/download.html
@rem 
@echo %1 %2 %3 %4

"C:\Program Files\ffmpeg\bin\ffmpeg.exe" -i C:\Erachains\IMG\demo1.mp4 -y -fs 200k -vcodec mpeg4 -q:v 20 -r:v 10 -s 255x255 dataPreviews\probe.mp4

pause