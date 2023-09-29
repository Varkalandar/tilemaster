/*
 * File: RasterTool.java
 * Creation: 2010_06_30
 *
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */

package tilemaster.editor.paintingtools;

import java.awt.image.BufferedImage;
import java.awt.Graphics;

/**
 * An example painting tool plugin which draws even pixels only.
 *
 * @author Hj. Malthaner
 */
public class RasterTool extends PaintingToolBase
{

    /**
     * @return The redraw mode to use for this tool.
     */
    public int getRefreshMode()
    {
        return REPAINT_NOTHING;
    }

    /**
     * @return The name to display for this tool.
     */
    public String getToolName()
    {
        return "Raster (even)";
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
        plot(gr, x, y);
    }

    /**
     * User drags mouse.
     *
     * @param gr Graphics context to use for drawing
     * @param x X coordinate
     * @param y Y coordinate
     * @param filled Paint filled shapes. Ignored by this tool.
     */
    public void paint(Graphics gr, int x, int y, boolean filled)
    {
        plot(gr, x, y);
    }

    private void plot(Graphics gr, int x, int y)
    {
        if(((x+y) & 1) == 0) {
            gr.fillRect(x, y, 1, 1);
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

    /** Creates a new instance of RasterTool */
    public RasterTool() 
    {
    }
}
