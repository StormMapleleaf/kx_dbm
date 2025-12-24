@echo off
title dbswitch
setlocal enabledelayedexpansion
cls

set APP_MAINCLASS=org.dromara.dbswitch.data.DataSyncApplication

set APP_HOME=%~dp0
set APP_HOME=%APP_HOME%\..\
cd %APP_HOME%
set APP_HOME=%cd%

set APP_BIN_PATH=%APP_HOME%\bin
set APP_LIB_PATH=%APP_HOME%\lib
set APP_CONF_PATH=%APP_HOME%\conf

set JAVA_OPTS=-server -Xms4096m -Xmx4096m -Xmn2048m -XX:+DisableExplicitGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Djava.awt.headless=true -Dfile.encoding=UTF-8 -Doracle.jdbc.J2EE13Compliant=true

echo System Information:
echo ********************************************************
echo COMPUTERNAME=%COMPUTERNAME%
echo OS=%OS%
echo.
echo APP_HOME=%APP_HOME%
echo APP_MAINCLASS=%APP_MAINCLASS%
echo CLASSPATH=%APP_CONF_PATH%;%APP_LIB_PATH%\*
echo CURRENT_DATE=%date% %time%:~0,8%
echo ********************************************************

echo Starting %APP_MAINCLASS% ...
echo java -classpath %APP_CONF_PATH%;%APP_LIB_PATH%\* %JAVA_OPTS% %APP_MAINCLASS%
echo .
java -classpath %APP_CONF_PATH%;%APP_LIB_PATH%\* %JAVA_OPTS% %APP_MAINCLASS%

:exit
pause
