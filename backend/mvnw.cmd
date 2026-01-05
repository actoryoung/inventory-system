@echo off
REM ----------------------------------------------------------------------------
REM Maven Wrapper for Windows
REM ----------------------------------------------------------------------------

set ERROR_CODE=0

set APP_HOME=%~dp0
set WRAPPER_JAR=%APP_HOME%.mvn\wrapper\maven-wrapper.jar
set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper.jar

if not exist "%WRAPPER_JAR%" (
    echo Downloading Maven Wrapper...
    powershell -Command "& {Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile '%WRAPPER_JAR%'}"
)

if not exist "%WRAPPER_JAR%" (
    echo ERROR: Failed to download Maven Wrapper
    set ERROR_CODE=1
    goto end
)

java -jar "%WRAPPER_JAR%" %*

:end
exit /b %ERROR_CODE%
