#!/bin/bash

LOG=`basename $0`.log
DTFMT='+ %d-%m-%Y %T'

app=erachain
xms=512
xmx=1024
mms=256

jpars"-Xms512m -Xms1024m"
pars="-nogui -pass=123456789 -seed=2:ABRjfyP7zVdtuuhEaTogtcJNUdU1hcop4zG4z2JiVjhR:123456789"

#java -Xms${xms}m -Xms${xmx}m -XX:MaxMetaspaceSize=${mms}m -jar $app.jar $pars

count=1
while true
do
  echo "`date +"$DTFMT"` : Starting $count" >> $LOG
  java $jpars -jar $app.jar $pars
  rc=$?
  echo "`date +"$DTFMT"` : Return code: $rc" >> $LOG
  count=$(($count+1))
  sleep 20s
done
