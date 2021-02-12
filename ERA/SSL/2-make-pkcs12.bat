
set KeyToolPath=C:\Program Files\Java\jre1.8.0_251\bin
set OpenSSlPath=C:\openssl\bin

set KeyFileName=privkey.pem
set CertFileName=cert.pem

set PKCS12FormatFileName=ERAcert.pkcs12

REM put certifycate in pkcs12 format
"%OpenSSlPath%\openssl" pkcs12 -inkey %KeyFileName% -in %CertFileName% -export -out %PKCS12FormatFileName%

pause