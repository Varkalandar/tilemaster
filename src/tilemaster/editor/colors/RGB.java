/*
 * File: RGB.java
 * Creation: 2012_05_18
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.colors;

/**
 * Helper class for calculations in RGB color space. RGB isn't
 * the best color space for visually mathcing color difference
 * calculations, but it should work well enough for Tilemaster.
 *
 * Most routines in here aren't "exact", but only "good enough"
 * approximations.
 * 
 * @author Hj. Malthaner
 */
public class RGB 
{
    public static int calcBrightness(final int rgb)
    {
        final int r = (rgb >> 16) & 0xFF;
        final int g = (rgb >> 8) & 0xFF;
        final int b = rgb & 0xFF;

        return (r*2 + g*5 + b) / 8;
    }
    
    public static int changeBrightness(final int rgb, final double amount)
    {
        final int a = (rgb & 0xFF000000);
        final int r = (rgb >> 16) & 0xFF;
        final int g = (rgb >> 8) & 0xFF;
        final int b = rgb & 0xFF;

        int rn = (int)(r * amount + 0.49);
        int gn = (int)(g * amount + 0.49);
        int bn = (int)(b * amount + 0.49);

        /*
        if(rn < 0) rn = 0;
        if(gn < 0) gn = 0;
        if(bn < 0) bn = 0;
        */
        
        rn = (((~rn) >>> 16) & rn) & 0xFFF;
        gn = (((~gn) >>> 16) & gn) & 0xFFF;
        bn = (((~bn) >>> 16) & bn) & 0xFFF;
        
        /*
        if(rn > 255) rn = 255;
        if(gn > 255) gn = 255;
        if(bn > 255) bn = 255;
        */
        
        rn = (((~(rn - 256)) >>> 16) | rn) & 0xFF;
        gn = (((~(gn - 256)) >>> 16) | gn) & 0xFF;
        bn = (((~(bn - 256)) >>> 16) | bn) & 0xFF;
        
        return a + (rn << 16) + (gn << 8) + bn;
    }

    public static int blend(final int rgb1, final int rgb2, final int alpha)
    {
        // final int a1 = (rgb1 & 0xFF000000);
        final int r1 = (rgb1 >> 16) & 0xFF;
        final int g1 = (rgb1 >> 8) & 0xFF;
        final int b1 = rgb1 & 0xFF;
        
        // final int a2 = (rgb2 & 0xFF000000);
        final int r2 = (rgb2 >> 16) & 0xFF;
        final int g2 = (rgb2 >> 8) & 0xFF;
        final int b2 = rgb2 & 0xFF;

        int rn = (alpha*(r1 - r2) >> 8) + r2;
        int gn = (alpha*(g1 - g2) >> 8) + g2;
        int bn = (alpha*(b1 - b2) >> 8) + b2;

        rn = (((~rn) >>> 16) & rn) & 0xFFF;
        gn = (((~gn) >>> 16) & gn) & 0xFFF;
        bn = (((~bn) >>> 16) & bn) & 0xFFF;

        rn = (((~(rn - 256)) >>> 16) | rn) & 0xFF;
        gn = (((~(gn - 256)) >>> 16) | gn) & 0xFF;
        bn = (((~(bn - 256)) >>> 16) | bn) & 0xFF;

        return 0xFF000000 + (rn << 16) + (gn << 8) + bn;
    }

    /**
     * Calculates visual differecne of two rgb colors. Differences
     * are not linear, but stetic. Differences are always greater or
     * equal to zero.
     * 
     * @param rgb1 First color rgb
     * @param rgb2 Second color rgb
     * @return Color difference
     */
    public static int diff(final int rgb1, final int rgb2)
    {
        final int a1 = (rgb1 >>> 24) & 0xFF;
        final int r1 = (rgb1 >>> 16) & 0xFF;
        final int g1 = (rgb1 >>> 8) & 0xFF;
        final int b1 = rgb1 & 0xFF;
        
        final int a2 = (rgb2 >>> 24) & 0xFF;
        final int r2 = (rgb2 >>> 16) & 0xFF;
        final int g2 = (rgb2 >>> 8) & 0xFF;
        final int b2 = rgb2 & 0xFF;
        
        int diff = 0;

        diff += (a2 - a1) * (a2 - a1);
        diff += (r2 - r1) * (r2 - r1) * 2;
        diff += (g2 - g1) * (g2 - g1) * 3;
        diff += (b2 - b1) * (b2 - b1);
        
        return diff;
    }
}
