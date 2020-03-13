/*
 * File: RecolorTool.java
 * Creation: 2011_11_27
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.paintingtools;

import asktools.ColorPalette;
import asktools.Requester;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;

/**
 * A painting tool to replace color ranges.
 *
 * @author Hj. Malthaner
 */
public class RecolorTool extends PaintingToolBase
{
    private int firstX, firstY;
    private BufferedImage canvas;
    private ColorPalette colorPalette;
    private RecolorState recolorState;

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
        return "Replace Color Range";
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
     * @param gr Graphics context.
     * @param x x coordinate.
     * @param y y coordinate.
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
     * @param gr Graphics context.
     * @param x x coordinate.
     * @param y y coordinate.
     * @param filled draw filled shapes?
     */
    @Override
    public void paint(final Graphics gr, final int x, final int y, boolean filled)
    {
        recolor(gr, firstX, firstY, x, y);
    }

    /**
     * This is called once the user clicked a color. The index of the
     * selected color is passed as parameter.
     *
     * @param colorIndex The color's index in the color map.
     */
    @Override
    public void onColorSelected(int colorIndex)
    {
    }


    public RecolorTool(JFrame parent,
                       BufferedImage canvas,
                       ColorPalette colorPalette,
                       boolean clean)
    {
        recolorState = new RecolorState();
        recolorState.clean = clean;

        this.canvas = canvas;
        this.colorPalette = colorPalette;

        Requester requester = new Requester();
        
        int [] colors = requester.askColorsIndices(parent,                       
                                                   new String []
                                                   {
                                                      "Select the start of the color range to replace:",
                                                      "Select the end of the color range to replace:",
                                                      "Select the start of the new color range:",
                                                      "Select the end of the new color range:"
                                                   },
                                                   colorPalette);
        
        
        for(int i=0; i<4; i++)
        {
            int ci = colors[i];
            System.err.println("Recolor colors #" + i + " is " + ci);
            recolorState.colors[i] = ci;
        }
    }

    /**
     * Replaces a color range by another, in the specified rectangle.
     */
    private void recolor(Graphics gr, int x, int y, int xx, int yy) 
    {
        final int startColor = recolorState.colors[0];
        final int endColor = recolorState.colors[1];
        final int repSColor = recolorState.colors[2];
        final int repEColor = recolorState.colors[3];

        final int width = canvas.getWidth();

        if(xx >= width)
        {
            xx = width - 1;
        }

        if(yy >= canvas.getHeight())
        {
            yy = canvas.getHeight() - 1;
        }

        // System.err.println("Recoloring x=" + x + " y=" + y + " xx=" + xx + " yy=" + yy);

        for(int j = y; j <= yy; j++)
        {
            for(int i = x; i <= xx; i++)
            {
                final int colorARGB = canvas.getRGB(i, j);

                if((colorARGB >>> 24) > 127)
                {
                    final int color = colorPalette.bestColorMatch(colorARGB);

                    if(color >= startColor && color <= endColor)
                    {
                        if(recolorState.clean == false)
                        {
                            final double p = (double)(color - startColor)/(double)(endColor - startColor);
                            final int index = repSColor + (int)((repEColor - repSColor) * p + 0.5);

                            final Color paintColor = colorPalette.getColor(index);

                            gr.setColor(paintColor);
                            gr.fillRect(i, j, 1, 1);
                        }
                    }
                    else
                    {
                        if(recolorState.clean == true)
                        {
                            final Color paintColor = colorPalette.getColor(repSColor);
                            gr.setColor(paintColor);
                            gr.fillRect(i, j, 1, 1);
                        }
                    }
                }
            }
        }
    }

    /**
     * State information for recoloring tool
     * @author Hj. Malthaner
     */
    private class RecolorState
    {
        /** True means to clear out other ranges rather than recolor */
        public boolean clean = false;
        public int counter = 0;
        public int colors [] = new int [4];
    }

}
