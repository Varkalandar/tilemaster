/*
 * File: GradientV2.java
 * Creation: 2011_11_24
 *
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */
package tilemaster.editor.paintingtools.filler.gradients;

/**
 * An experimental gradient.
 *
 * @author Hj. Malthaner
 */
public class GradientV2 extends GradientLinear
{
    /**
     * Calculate a RGB gradient from rgb1 to rgb2.
     *
     * @param rgb1 First RGB value.
     * @param rgb2 Second RGB value.
     * @param f Must be in range [0..1]
     * @return Calculated RGB value.
     */
    @Override
    public int calcRGB(final int rgb1, final int rgb2,  double f)
    {
        double vf = Math.min(f*f*1.08, 1.0);
        return super.calcRGB(rgb1, rgb2, vf);
    }
}
