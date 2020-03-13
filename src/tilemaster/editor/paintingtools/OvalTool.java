/*
 * File: OvalTool.java
 * Creation: 2010_05_29
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.paintingtools;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Draws a circle or ellipse.
 * 
 * @author Hj. Malthaner
 */
public class OvalTool extends PaintingToolBase
{
    private int firstX, firstY;

    /**
     * @return The redraw mode to use for this tool.
     */
    @Override
    public int getRefreshMode()
    {
        return REPAINT_REFRESH;
    }

    /**
     * @return The name to display for this tool.
     */
    @Override
    public String getToolName()
    {
        return "Oval";
    }

    /**
     * Sets the drawing area for tools which need direct access
     *
     * @param image The current tile image, writeable
     */
    @Override
    public void setCanvas(BufferedImage image)
    {
        // Not needed for this tool.
    }

    /**
     * User clicked a new location
     */
    @Override
    public void firstClick(Graphics gr, int x, int y)
    {
        firstX = x;
        firstY = y;
        gr.fillRect(firstX, firstY, 1, 1);
    }

    /**
     * User drags mouse
     */
    @Override
    public void paint(final Graphics gr, final int x, final int y, boolean filled)
    {
        final int l = Math.min(firstX, x);
        final int t = Math.min(firstY, y);
        final int w = Math.abs(x-firstX);
        final int h = Math.abs(y-firstY);

        if(filled) 
        {
            gr.fillOval(l, t, w, h);
        }
        else 
        {
            gr.drawOval(l, t, w, h);
        }
    }

    /**
     * This is called once the user clicked a color. The index of the
     * selected color is passed as parameter.
     * @param colorIndex The color's index in the color map.
     * @author Hj. Malthaner
     */
    @Override
    public void onColorSelected(int colorIndex)
    {
    }

    /** Creates a new instance of OvalTool */
    public OvalTool()
    {
    }

}
