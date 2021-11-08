
Руководство по настройке защищенного соединения для WEB-сервера (HTTPS, SSL)

Нужно для защиты ответов для команд API

Полное руководство:
https://docs.google.com/document/d/1N4YO-fm2ORqPcHNu0UWE-D9DeOuM5n084acyGLLDAVo/edit?usp=sharing

Создайте свое хранилище ключей в папке SSL:
/SSL/WEBkeystore
И для него настройте согласно инструкции ниже

Если есть сертификат в формате PKCS12, пункты 1,2,3 можно пропустить
1. Качаем из интернета утилиту OpenSSL.
2. Создаем само-подписанный (self-signed) сертификат
openssl  req -x509 -nodes -days 365 -newkey rsa:2048 -keyout  KeyFileName.key -out CertFileName.crt
где
KeyFileName.key – имя файла ключей
 CertFileName.crt – имя файла сертификата
Экран:
Generating a 2048 bit RSA private key
....+++
...............................................+++
writing new private key to ‘KeyFileName.key’
-----
You are about to be asked to enter information that will be incorporated
into your certificate request.
What you are about to enter is what is called a Distinguished Name or a DN.
There are quite a few fields but you can leave some blank
For some fields there will be a default value,
If you enter '.', the field will be left blank.
-----
Country Name (2 letter code) [AU]: вводим 2 символа страны
State or Province Name (full name) [Some-State]:вводим регион
Locality Name (eg, city) []:	вводим город
Organization Name (eg, company) [Internet Widgits Pty Ltd]:вводим организацию
Organizational Unit Name (eg, section) []:вводим организацию
Common Name (e.g. server FQDN or YOUR name) []:вводим организацию
Email Address []:вводим E-mail

3. Преобразуем полученные файлы в формат PKCS12
openssl  pkcs12 -inkey KeyFileName.key -in CertFileName.crt -export -out PKCS12FileName.pkcs12
где PKCS12FileName.pkcs12 – имя файла в формате PKCS12
Экран:
Enter Export Password: вводим Пароль Сертификата (ПС).
Verifying - Enter Export Password:

4. При помощи утилиты keytool помещаем сертификат в защищенное хранилище (KeyStore)
Утилита keytool входит в стандартную поставку Java и находится по адресу C:\Program Files\Java\jre1.8.0_251\bin
keytool  -importkeystore -srckeystore PKCS12FileName.pkcs12 -srcstoretype PKCS12 -destkeystore JavaKeyStoreName
Экран:
Enter destination keystore password: Вводим пароль KeyStore (мин 6 символов)
Re-enter new password:
Enter source keystore password:  Вводим пароль сертификата (ПС) см. Пункт 3
Entry for alias 1 successfully imported.
Import command completed:  1 entries successfully imported, 0 entries failed or cancelled

Где
JavaKeyStoreName – имя файла защищенного хранилища (KeyStore).
5. Переименовываем файл JavaKeyStoreName в WEBkeystore и копируем его в директорию Erachain/SSL
6. Запускаем ноду ЕРА, открываем Файл-> Настройки, где задаем путь до WEBkeystore и пароли.
 - Для пробного WEBkeystore_example - доступ к keyStore: 123456 и пароль для сертификата: 1

7. Перезапускаем ноду
8. Открываем Блокэксплорер
Меню Файл->Встроенный BlockExplorer

В подмогу: Для пакетной обработки можно использовать командный файл MakeKeyStore.bat, предварительно задав в нем нужные названия файлов в пути.
При этом первый пароль и его подтверждение - это пароль для запаковки сертификата в PKCS12.
Затем спросит пароль и его подтверждение - это пароль доступа к KeyStore
И последний запрос пароля - это пароль от ранее созданного PKCS12.
В папке увидите файл PKCS12 и ваше хранилище ключей WEBkeystore


