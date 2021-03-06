/*
 * File: Interpolate8Tool.java
 * Creation: 2011_11_25
 *
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */

package tilemaster.editor.paintingtools;

import java.awt.image.BufferedImage;
import java.awt.Graphics;

/**
 * An example painting tool plugin which interpolates pixels
 * based on the eight nearest neighboring pixels
 *
 * @author Hj. Malthaner
 */
public class Interpolate8Tool extends PaintingToolBase
{
    private BufferedImage canvas;
    
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
        return "Interpolate Eight Neighbors";
    }

    /**
     * Sets the drawing area for tools which need direct access
     *
     * @param image The current tile image, writeable
     */
    public void setCanvas(BufferedImage image)
    {
        canvas = image;
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

    /**
     * Create an interpolated pixel.
     */
    private void plot(Graphics gr, int x, int y)
    {
        final int rgb1 = canvas.getRGB(x+1, y) & 0xF8F8F8F8;
        final int rgb2 = canvas.getRGB(x-1, y) & 0xF8F8F8F8;
        final int rgb3 = canvas.getRGB(x, y+1) & 0xF8F8F8F8;
        final int rgb4 = canvas.getRGB(x, y-1) & 0xF8F8F8F8;
        
        final int rgb5 = canvas.getRGB(x+1, y+1) & 0xF8F8F8F8;
        final int rgb6 = canvas.getRGB(x-1, y+1) & 0xF8F8F8F8;
        final int rgb7 = canvas.getRGB(x+1, y-1) & 0xF8F8F8F8;
        final int rgb8 = canvas.getRGB(x-1, y-1) & 0xF8F8F8F8;

	final int sum = rgb1 + rgb2 + rgb3 +rgb4 + rgb5 + rgb6 + rgb7 +rgb8;
        
        canvas.setRGB(x, y, sum >> 3);
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

    public Interpolate8Tool() 
    {
    }
}
