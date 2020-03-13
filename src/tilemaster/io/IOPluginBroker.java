/*
 * File: IOPluginBroker.java
 * Creation: 2010_05_26
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.io;

import itemizer.editor.ui.SimpleMessageBox;
import itemizer.io.ObjectBroker;
import java.io.File;
import java.util.Enumeration;
import tilemaster.tile.TileSet;

/**
 * Registry and dispatcher for IO handler plugins.
 *
 * @author Hj. Malthaner
 */
public class IOPluginBroker
{
    private static final ObjectBroker objectBroker = new ObjectBroker();

    public static void registerHandler(String extension, FileTypeIO handler)
    {
        objectBroker.registerHandler(extension, handler);
    }

    public static String getFileFilterList(final String separator)
    {
        String filterMask = "";

        final Enumeration <String> keys = objectBroker.getKeys();
        while(keys.hasMoreElements())
        {
            String key = keys.nextElement();
            filterMask += key;
            if(keys.hasMoreElements())
            {
                filterMask += separator;
            }
        }

        return filterMask;
    }

    private static FileTypeIO getHandler(final File file)
    {
        final String name = file.getName();

        final int p = name.indexOf('.');
        if(p >= 0)
        {
            final String ext = name.substring(p);
            Object handler = objectBroker.get(ext);
            if(handler != null)
            {
                return (FileTypeIO)handler;
            }
            else
            {
                SimpleMessageBox box = 
                        new SimpleMessageBox(null,
                        "No input/output handler found:",
                        "<html>No handler could be found for file extension " + ext +
                        "<br>The extension should be one of :" + getFileFilterList(", ") +
                        "</html>",
                        null);
                box.setOneButtonOnly(true);
                box.setVisible(true);
            }
        }

        SimpleMessageBox box = new SimpleMessageBox(null,
                "No input/output handler found:",
                "<html>No handler could be found for file: " + file +
                "<br>The file needs an extension like: "  + getFileFilterList(", ") +
                "<html>",
                null);

        box.setOneButtonOnly(true);
        box.setVisible(true);
        
        return null;
    }


    public static TileSet read(final String filename)
    {
        TileSet result = new TileSet(null, 0);

        final File file = new File(filename);
        final FileTypeIO handler = getHandler(file);

        if(handler != null)
        {
            try
            {
                result = handler.read(filename);
            } 
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }

        return result;

    }

    public static void write(final String filename, final TileSet tileSet)
    {
        final File file = new File(filename);
        final FileTypeIO handler = getHandler(file);

        if(handler != null)
        {
            try
            {
                handler.write(filename, tileSet);
            } 
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public static void loadPlugins()
    {
        try
        {
            Class.forName("tilemaster.io.TicaZipIo");
            Class.forName("tilemaster.io.TicaIO");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            final File dir = new File("plugins/tilemaster/io");
            final String[] children = dir.list();
            if (children == null)
            {
                System.err.println("Found no IO plugins to load.");
            }
            else
            {
                for (int i=0; i<children.length; i++)
                {
                    // Get filename of file or directory
                    String filename = children[i];
                    int p = filename.indexOf('.');
                    filename = filename.substring(0, p);

                    System.err.println("Loading plugin: " + filename);

                    try
                    {
                        Class.forName("tilemaster.io." + filename);
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        } 
        catch(Exception e)
        {
            e.printStackTrace();
        }        
    }
}
