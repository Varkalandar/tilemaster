/*
 * File: OutlineFille.java
 * Creation: 2011_11_20
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.paintingtools.filler;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import tilemaster.editor.colors.RGB;
import tilemaster.editor.paintingtools.Fillmachine;

/**
 * Draw an outline on the outermost pixelborder of a filled area.
 * Will not draw outside of the filled area.
 *
 * @author Hj. Malthaner
 */
public class OutlineFiller implements Fillmachine.Filler
{
    private BufferedImage img;
    private BufferedImage canvas;
    private Color paintColor;

    @Override
    public void start(BufferedImage canvas, Color paintColor)
    {
        img = new BufferedImage(canvas.getWidth(),
                                canvas.getHeight(),
                                BufferedImage.TYPE_INT_ARGB);
        this.canvas = canvas;
        this.paintColor = paintColor;
    }
    
    @Override
    public void finish(byte [] marks)
    {
        canvas.getGraphics().drawImage(img, 0, 0, null);
    }

    @Override
    public void plotInside(int x, int y) 
    {
    }

    @Override
    public void plotBorder(int x, int y)
    {
        Graphics gr = img.getGraphics();
        gr.setColor(paintColor);
        gr.fillRect(x, y, 1, 1);
    }

    @Override
    public void plotLine(int x, int y, int xx, int yy)
    {

    }

}
