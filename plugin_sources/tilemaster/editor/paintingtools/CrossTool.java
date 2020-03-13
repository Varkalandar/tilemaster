/*
 * File: CrossTool.java
 * Creation: 2010_06_21
 *
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */

package tilemaster.editor.paintingtools;

import java.awt.image.BufferedImage;
import java.awt.Graphics;

/**
 * An example painting tool plugin which draws crosses.
 *
 * @author Hj. Malthaner
 */
public class CrossTool extends PaintingToolBase 
{

    private int firstX, firstY;

    /**
     * @return The redraw mode to use for this tool.
     */
    public int getRefreshMode()
    {
        return REPAINT_REFRESH;
    }

    /**
     * @return The name to display for this tool.
     */
    public String getToolName()
    {
        return "Cross";
    }

    /**
     * Sets the drawing area for tools which need direct access
     *
     * @param image The current tile image, writeable
     */
    public void setCanvas(BufferedImage image)
    {
        // Not needed for this tool.
    }

    /**
     * User clicked a new location
     */
    public void firstClick(Graphics gr, int x, int y) 
    {
        firstX = x;
        firstY = y;
        gr.fillRect(firstX, firstY, 1, 1);
    }

    /**
     * User drags mouse
     */
    public void paint(Graphics gr, int x, int y, boolean filled)
    {
        if(x != firstX || y != firstY) {
            gr.drawLine(firstX, firstY, x, y);
            gr.drawLine(x, firstY, firstX, y);
        }
    }
    
    /**
     * This is called once the user clicked a color. The index of the
     * selected color is passed as parameter.
     *
     * @param colorIndex The color's index in the color map.
     */
    public void onColorSelected(int colorIndex)
    {
    }

    /** Creates a new instance of CrossTool */
    public CrossTool()
    {
    }
    
}
