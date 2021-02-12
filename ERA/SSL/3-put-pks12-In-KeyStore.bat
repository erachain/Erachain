
set KeyToolPath=C:\Program Files\Java\jre1.8.0_251\bin
set OpenSSlPath=C:\openssl\bin

set PKCS12FormatFileName=ERAcert.pkcs12
set JavaKeyStoreName=WEBkeystore

REM put in Java KeyStore
"%KeyToolPath%\keytool" -importkeystore -srckeystore %PKCS12FormatFileName% -srcstoretype PKCS12 -destkeystore %JavaKeyStoreName%

pause