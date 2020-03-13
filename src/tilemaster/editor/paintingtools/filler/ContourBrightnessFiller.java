/*
 * File: ContourBrightnessFiller.java
 * Creation: 17.05.2012
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.paintingtools.filler;

import asktools.ColorPalette;
import javax.swing.JFrame;
import tilemaster.editor.colors.RGB;

/**
 * Shades an filled area, i.e. changes the lightness of the pixels
 * inside the area according to the gradient and location in the
 * area. Gradient choice is handled by the superclass.
 * 
 * @author Hj. Malthaner
 */
public class ContourBrightnessFiller extends ContourFiller
{
    public ContourBrightnessFiller(final JFrame parent,
                                   final ColorPalette colorPalette,
                                   final double startAngle,
                                   final double angleStep)
    {
        super(parent, colorPalette, startAngle, angleStep);
    }

    @Override
    protected int calcRGB(int oldRgb,int rgb1, int rgb2, double sum, double div)
    {
        final int b1 = RGB.calcBrightness(rgb1);
        final int b2 = RGB.calcBrightness(rgb2);
        
        double f = sum*2.0/div;

        f = b1 + f * (b2 - b1);
        f = f / 255.0;

        return RGB.changeBrightness(oldRgb, f);
    }
}
