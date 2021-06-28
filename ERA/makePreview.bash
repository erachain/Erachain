#!/bin/sh

## http://www.ffmpeg.org/download.html

## not used now! see in code: org.erachain.webserver.PreviewMaker
ffmpeg -i $1 -y -fs 512k -vcodec h264 -an -vf scale=256:-2,setsar=1:1 -q:v $2 -r:v $3 $4

