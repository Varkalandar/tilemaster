# Tilemaster

Tilemaster is a tile set editor for computer game projects. It can
also be used as a simple painting tool for icons and other small 
images.

At the moment it is in an early stage of development, but the basic
functionality to manage tile sets and draw tiles is there.

For more details, please check the project website:
https://github.com/Varkalandar/Tilemaster/

# How To Start

To run Tilemaster, unzip the tilemaster.zip and execute the start.bat
script if you are using Windows, or start.sh if you are using Unix (maybe
works for MacOS X, too). If the start script doesn't work, you can try
to double click the tilemaster.jar file. If you have Java 8 properly
installed this should start Tilemaster, and also load the plugins, but in
some old versions this did not work properly.

If all of this doesn't work for you, but you are familiar with the
command line interface of your OS, this line should start Tilemaster
and also load the plugins:

    java -classpath plugins;Tilemaster.jar;lib\Itemizer.jar tilemaster.editor.TilesetEditor

Tilemaster needs Java 8 or newer installed to be run.

For more information please check the wiki:
    http://sourceforge.net/apps/mediawiki/tilemaster/index.php?title=Main_Page


## How To Compile The Plugin Sources

Step 1: 

Edit the compile.bat in the plugin_sources directory and
set the %JAVA_HOME% variable to the correct location for
your JDK (you need a Java development kit of the Java 6
series installed to compile the plugins).

Step 2:

Execute the compile.bat script.
It will compile all plugin source files stored in the 
plugin_sources directorty and install them in the right
place to be loaded when Tilemaster starts up.
