/*
 * File: InterpolateTool.java
 * Creation: 2010_06_13
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.paintingtools;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 * Obsolete, to be replaced by a plugin.
 * 
 * @author Hj. Malthaner
 */
public class InterpolateTool extends PaintingToolBase
{
    private BufferedImage canvas;

    private int R, G, B;
    private int count;

    /**
     * @return The redraw mode to use for this tool.
     */
    @Override
    public int getRefreshMode()
    {
        return REPAINT_NOTHING;
    }

    /**
     * @return The name to display for this tool.
     */
    @Override
    public String getToolName()
    {
        return "Anti";
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
        count = 0;
        R = 0;
        G = 0;
        B = 0;

        addRGB(x+1, y);
        addRGB(x-1, y);
        addRGB(x, y+1);
        addRGB(x, y-1);

        if(count > 0) {
            R /= count;
            G /= count;
            B /= count;

            canvas.setRGB(x, y, 0xFF000000 + (R << 16) + (G << 8) + B);
        }
    }

    /**
     * User drags mouse
     */
    @Override
    public void paint(Graphics gr, int x, int y, boolean filled)
    {
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

    /** Creates a new instance of InterpolateTool */
    public InterpolateTool(BufferedImage canvas)
    {
        this.canvas = canvas;
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && y >= 0 && x < canvas.getWidth() && y < canvas.getHeight();
    }

    private void addRGB(int x, int y)
    {
        if(isValid(x, y)) {
            final int rgb = canvas.getRGB(x, y);

            if((rgb >>> 24) > 127) {
            count ++;
                R += (rgb & 0xFF0000) >>> 16;
                G += (rgb & 0xFF00) >>> 8;
                B += (rgb & 0xFF);
            }
        }
    }
}