/*
 * File: ToolPluginBroker.java
 * Creation: ???
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.paintingtools;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.JList;

/**
 *
 * @author Hj. Malthaner
 */
public class ToolPluginBroker
{

    public static void loadPlugins(JList pluginList)
    {
        try 
        {
            File dir = new File("plugins/tilemaster/editor/paintingtools");
            String[] children = dir.list();
            if (children == null) 
            {
                System.err.println("Found no tool plugins to load.");
            } 
            else 
            {
                Arrays.sort(children);
                
                Vector <PluginListWrapper> plugins = new Vector <PluginListWrapper>();

                for (int i=0; i<children.length; i++) 
                {
                    // Get filename of file or directory
                    String filename = children[i];
                    int p = filename.indexOf('.');
                    filename = filename.substring(0, p);

                    if(filename.contains("$") == false)
                    {
                        System.err.println("Loading tool plugin: " + filename);

                        try 
                        {
                            Class clazz = Class.forName("tilemaster.editor.paintingtools." + filename);
                            PaintingTool tool = (PaintingTool)clazz.newInstance();
                            PluginListWrapper wrapper = new PluginListWrapper();
                            wrapper.tool = tool;
                            plugins.add(wrapper);
                        }
                        catch(Exception e) 
                        {
                            e.printStackTrace();
                        }
                    }
                }

                pluginList.setListData(plugins);

            }
        } 
        catch(Exception e) 
        {
            e.printStackTrace();
        }
    }
}
