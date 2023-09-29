/*
 * File: PluginListWrapper.java
 * Creation: ???
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.paintingtools;

/**
 *
 * @author Hj. Malthaner
 */
public class PluginListWrapper
{
    public PaintingTool tool;

    @Override
    public String toString()
    {
        return " " + tool.getToolName();
    }
}
