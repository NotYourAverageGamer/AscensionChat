@echo off
chcp 65001 > NUL
java -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8 -Dlogback.configurationFile=logback.xml -jar ascensionchat.jar ascensionchat.conf
pause
