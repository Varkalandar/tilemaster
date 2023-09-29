#!/bin/sh

# Set JAVA_HOME to the right place for your system!

export JAVA_HOME="/usr"

export CLASSPATH=../../dist/Tilemaster.jar:../../dist/TM.jar:../../dist/lib/Itemizer.jar:../../dist/lib/AskTools.jar
export PLUGINS=../../plugins
mkdir $PLUGINS
$JAVA_HOME/bin/javac -classpath $CLASSPATH -d $PLUGINS tilemaster/io/*.java
$JAVA_HOME/bin/javac -classpath $CLASSPATH -d $PLUGINS tilemaster/editor/paintingtools/*.java

