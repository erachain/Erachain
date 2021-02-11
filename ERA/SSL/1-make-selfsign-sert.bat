
set KeyToolPath=C:\Program Files\Java\jre1.8.0_251\bin
set OpenSSlPath=C:\openssl\bin

set KeyFileName=privkey.pem
set CertFileName=cert.pem

REM create self-signed certifycate
"%OpenSSlPath%\openssl" req -x509 -nodes -days 365 -newkey rsa:2048 -keyout %KeyFileName% -out %CertFileName%

pause