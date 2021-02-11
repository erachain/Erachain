
set KeyToolPath=C:\Program Files\Java\jre1.8.0_251\bin
set OpenSSlPath=C:\openssl\bin

rem set CertFileName=ERAcert.crt
set CertFileName=cert.pem

rem set KeyFileName=ERAcert.key
set KeyFileName=privkey.pem

set PKCS12FormatFileName=ERAcert.pkcs12
set JavaKeyStoreName=WEBkeystore

REM create self-signed certifycate
"%OpenSSlPath%\openssl" req -x509 -nodes -days 365 -newkey rsa:2048 -keyout %KeyFileName% -out %CertFileName%

REM put certifycate in pkcs12 format
"%OpenSSlPath%\openssl" pkcs12 -inkey %KeyFileName% -in %CertFileName% -export -out %PKCS12FormatFileName%

REM put in Java KeyStore
"%KeyToolPath%\keytool" -importkeystore -srckeystore %PKCS12FormatFileName% -srcstoretype PKCS12 -destkeystore %JavaKeyStoreName%

pause