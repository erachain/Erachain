@ECHO OFF

set app=erachain
set task=erachain-demo

set pars=-pass=1 -seed=10:new:1 -dbschain=mapdb -bugs=7 -datachainpath=dataDEMO -testnet=demo

set jpars=

IF EXIST java (
	set run=java
	goto continue
)

REG QUERY "HKLM\SOFTWARE\JavaSoft\Java Runtime Environment\1.7" /v "JavaHome" >nul 2>nul || ( GOTO NOTFOUND1 )
	for /f "tokens=1,2,*" %%a in ('reg query "HKLM\SOFTWARE\JavaSoft\Java Runtime Environment\1.7" /v "JavaHome"') do if "%%a"=="JavaHome" set JAVAHOME=%%c

IF EXIST "%JAVAHOME%\bin\java.exe" (
	set run="%JAVAHOME%\bin\java.exe"
	goto continue
)

:NOTFOUND1

REG QUERY "HKLM\SOFTWARE\WOW6432NODE\JavaSoft\Java Runtime Environment\1.7" /v "JavaHome" >nul 2>nul || ( GOTO NOTFOUND2 )
	for /f "tokens=1,2,*" %%a in ('reg query "HKLM\SOFTWARE\WOW6432NODE\JavaSoft\Java Runtime Environment\1.7" /v "JavaHome"') do if "%%a"=="JavaHome" set JAVAHOME=%%c

IF EXIST "%JAVAHOME%\bin\java.exe" (
	set run="%JAVAHOME%\bin\java.exe"
	goto continue
)

:NOTFOUND2

REG QUERY "HKLM\SOFTWARE\JavaSoft\Java Runtime Environment\1.8" /v "JavaHome" >nul 2>nul || ( GOTO NOTFOUND3 )
	for /f "tokens=1,2,*" %%a in ('reg query "HKLM\SOFTWARE\JavaSoft\Java Runtime Environment\1.8" /v "JavaHome"') do if "%%a"=="JavaHome" set JAVAHOME=%%c

IF EXIST "%JAVAHOME%\bin\java.exe" (
	set run="%JAVAHOME%\bin\java.exe"
	goto continue
)

:NOTFOUND3

REG QUERY "HKLM\SOFTWARE\WOW6432NODE\JavaSoft\Java Runtime Environment\1.8" /v "JavaHome" >nul 2>nul || ( GOTO NOTFOUND4 )
	for /f "tokens=1,2,*" %%a in ('reg query "HKLM\SOFTWARE\WOW6432NODE\JavaSoft\Java Runtime Environment\1.8" /v "JavaHome"') do if "%%a"=="JavaHome" set JAVAHOME=%%c

IF EXIST "%JAVAHOME%\bin\java.exe" (
	set run="%JAVAHOME%\bin\java.exe"
	goto continue
)

:continue
rem	start "%app%" java -Xms%xms%m -Xmx%xmx%m -XX:MaxMetaspaceSize=%mms%m -XX:MaxMetaspaceSize=%mms%m -jar %app%.jar

start "%task%" %run% %jpars% -jar %app%.jar %pars%

exit
	
:NOTFOUND4

ECHO Java software not found on your system. Please go to http://java.com to download a copy of Java.
PAUSE