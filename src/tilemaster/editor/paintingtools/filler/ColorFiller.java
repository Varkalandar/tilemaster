/*
 * File: ColorFille.java
 * Creation: 2012_18_05
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.paintingtools.filler;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import tilemaster.editor.paintingtools.Fillmachine;


/**
 * Fills an area with a color. Will replace the old
 * flood fill tool.
 *
 * @author Hj. Malthaner
 */
public class ColorFiller implements Fillmachine.Filler
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
        Graphics gr = img.getGraphics();
        gr.setColor(paintColor);
        gr.fillRect(x, y, 1, 1);
    }

    @Override
    public void plotBorder(int x, int y)
    {
    }

    @Override
    public void plotLine(int x, int y, int xx, int yy)
    {
    }

}
