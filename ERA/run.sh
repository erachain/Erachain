#!/bin/bash
# 512Mb, x32

app=erachain-test-001
xms=64
xmx=128
mms=128

pars=-nogui

#java -Xms${xms}m -Xms${xmx}m -XX:MaxMetaspaceSize=${mms}m -jar $app.jar $pars
java -XX:MaxMetaspaceSize=${mms}m -jar $app.jar $pars
