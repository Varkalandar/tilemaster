/*
 * File: FillRectTool.java
 * Creation: 2010_05_25
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.paintingtools;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Draws a filled rectange.
 *
 * @author Hj. Malthaner
 */
public class FillRectTool extends PaintingToolBase
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
        return "Fill";
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
        // if(x != firstX || y != firstY) {

            final int l = Math.min(firstX, x);
            final int t = Math.min(firstY, y);
            final int w = Math.abs(x-firstX) + (filled ? 1 : 0);
            final int h = Math.abs(y-firstY) + (filled ? 1 : 0);

            if(filled) {
                gr.fillRect(l, t, w, h);
            } else {
                gr.drawRect(l, t, w, h);
            }
        // }
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

    /** Creates a new instance of FillRectTool */
    public FillRectTool()
    {
    }
    
}
