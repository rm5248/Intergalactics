@echo off
IF "%1%"=="ie" goto jview
:java
 java -cp web\WEB-INF\classes igx.client.I %*
 goto end
:jview
 jview /cp web\WEB-INF\classes igx.client.I %*
 goto end
:end