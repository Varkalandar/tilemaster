rem Windows Look and Feel
rem start javaw -Dlookandfeel=com.sun.java.swing.plaf.windows.WindowsLookAndFeel -classpath plugins;Tilemaster.jar;lib\Itemizer.jar tilemaster.editor.TilesetEditor

rem Motif Look and Feel
rem start javaw -Dlookandfeel=com.sun.java.swing.plaf.motif.MotifLookAndFeel -classpath plugins;Tilemaster.jar;lib\Itemizer.jar tilemaster.editor.TilesetEditor

rem Nimbus Look and Feel
java -classpath plugins;Tilemaster.jar;lib\Itemizer.jar tilemaster.editor.TilesetEditor

rem pause