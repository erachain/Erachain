#!/bin/bash

LOG=`basename $0`.log
DTFMT='+ %d-%m-%Y %T'

app=erachain-dev
xms=512
xmx=1024
mms=256

pars="-nogui -pass=1"

#java -Xms${xms}m -Xms${xmx}m -XX:MaxMetaspaceSize=${mms}m -jar $app.jar $pars

count=1
while true
do
  echo "`date +"$DTFMT"` : Starting $count" >> $LOG
  java -jar $app.jar $pars
  rc=$?
  echo "`date +"$DTFMT"` : Return code: $rc" >> $LOG
  count=$(($count+1))
  sleep 20s
done
