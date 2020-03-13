rem
rem Set JAVA_HOME to the right place for your system!
rem

rem set JAVA_HOME="C:\Program Files\Java\jdk1.6.0_30"
set JAVA_HOME="C:\Program Files\Java\jdk1.7.0_07"

set CLASSPATH=..\..\dist\Tilemaster.jar;..\..\dist\TM.jar;..\..\dist\lib\Itemizer.jar;..\..\dist\lib\AskTools.jar
set PLUGINS=..\..\plugins
md %PLUGINS%
%JAVA_HOME%\bin\javac -classpath %CLASSPATH% -d %PLUGINS% tilemaster\io\*.java
%JAVA_HOME%\bin\javac -classpath %CLASSPATH% -d %PLUGINS% tilemaster\editor\paintingtools\*.java

pause
