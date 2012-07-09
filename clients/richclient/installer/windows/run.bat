@ECHO OFF

REM Installation directory #########################################
SET INSTALLDRIVE=%~d0
SET INSTALLDIR=%~dp0

REM Java settings ##################################################
SET JAVACMD=java.exe

REM Proxy settings #################################################
SET PROXYUSE=false
SET PROXYHOST=10.21.0.6
SET PROXYPORT=8080

REM System settings ################################################
SET SYSPROPS=

REM Programm settings ##############################################
SET MAINCLASS=org.openecard.client.richclient.RichClient
SET CLASSPATH=lib/*;


REM ################################################################
REM ##                  No CHANGES NECESSARY !!!                  ##
REM ################################################################

IF [%PROXYUSE%] EQU [true] (
  SET PROXY=-Dhttp.proxySet=true -Dhttp.proxyHost=%PROXYHOST% -Dhttp.proxyPort=%PROXYPORT% 
) else (
  SET PROXY=
)


%INSTALLDRIVE%
CD "%INSTALLDIR%"
ECHO %JAVACMD% %SYSPROPS% %PROXY% -cp %CLASSPATH% %MAINCLASS%
%JAVACMD% %SYSPROPS% %PROXY% -cp %CLASSPATH% %MAINCLASS%
