@echo off
IF "%1%"=="ie" goto jview
:java
 java -cp classes igx.client.I %1%
 goto end
:jview
 jview /cp classes igx.client.I %2%
 goto end
:end