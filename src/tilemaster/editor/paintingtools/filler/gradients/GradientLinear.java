/*
 * File: GradientLinear.java
 * Creation: 2011_11_23
 *
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */

package tilemaster.editor.paintingtools.filler.gradients;

/**
 * A linear gradient.
 * 
 * @author Hj. Malthaner
 */
public class GradientLinear extends GradientBase
{
    /**
     * Calculate a RGB gradient from rgb1 to rgb2.
     *
     * @param rgb1 First RGB value.
     * @param rgb2 Second RGB value.
     * @param f Must be in range [0..1]
     * @return Calculated RGB value.
     */
    public int calcRGB(final int rgb1, final int rgb2, final double f)
    {
        if(f < 0) System.err.println("f=" + f);
        if(f > 1.0) System.err.println("f=" + f);


        final int r1 = red(rgb1);
        final int g1 = green(rgb1);
        final int b1 = blue(rgb1);

        final int rv = red(rgb2) - r1;
        final int gv = green(rgb2) - g1;
        final int bv = blue(rgb2) - b1;

        final int alpha = 0xFF;

        return (alpha << 24) +
               ((r1 + (int)(rv * f + 0.4)) << 16) +
               ((g1 + (int)(gv * f + 0.4)) << 8) +
                (b1 + (int)(bv * f + 0.4));

    }
}
