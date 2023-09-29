/*
 * File: ReduceToColormapED.java
 * Creation: 2016/01/25
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.transforms;

import asktools.ColorPalette;
import java.awt.image.BufferedImage;
import tilemaster.editor.TilesetEditor;

/**
 * Reduce tile colors to those found in the colormap, using an
 * error diffusion strategy.
 *
 * @author Hj. Malthaner
 */
public class ReduceToColormapED implements TileTransform
{
    // minimum and maximim error to carry over
    private static final int MINDIFF = -512;
    private static final int MAXDIFF = +512;
    
    private ColorPalette palette;

    /**
     * If this transform requires user input, ask for details.
     * @param parent The parent frame.
     * @return true to execute the transform, false to cancel.
     */
    @Override
    public boolean askUserData(TilesetEditor parent)
    {
        palette = parent.getColorPalette();
        return true;
    }

    /**
     * Transform the image in the desired way
     * @param canvas The image to transform.
     * @return The new image
     */
    @Override
    public BufferedImage transform(BufferedImage canvas)
    {
        final int width = canvas.getWidth();
        final int height = canvas.getHeight();

        // stored errors from current/last line
        int [] rDiff = new int[width+3];
        int [] gDiff = new int[width+3];
        int [] bDiff = new int[width+3];
        
        int power = 12;
        
        for(int y=0; y<height; y++)
        {
            // Error carryover from current pixel
            int rLast = 0;
            int gLast = 0;
            int bLast = 0;
            
            for(int x=0; x<width; x++)
            {
                final int argb = canvas.getRGB(x, y);
                
                // the actual color
                int R = (argb >> 16) & 0xFF;
                int G = (argb >> 8) & 0xFF;
                int B = (argb) & 0xFF;

                // Color with errors added
                R += (rDiff[x-1 +1] + 5*rDiff[x +1] + 3*rDiff[x+1 +1] + 7*rLast)*power >> 8;
                G += (gDiff[x-1 +1] + 5*gDiff[x +1] + 3*gDiff[x+1 +1] + 7*gLast)*power >> 8;
                B += (bDiff[x-1 +1] + 5*bDiff[x +1] + 3*bDiff[x+1 +1] + 7*bLast)*power >> 8;                
                
                // color map index which matched best
                final int index = palette.bestColorMatch(0xFF000000 +
                                                         (clip(R, 0, 255) << 16) + 
                                                         (clip(G, 0, 255) << 8) + 
                                                          clip(B, 0, 255));

                // has match and is not transparent
                if(index != 0 && argb >>> 24 > 128)
                {
                    final int best = palette.getColor(index).getRGB();
                    canvas.setRGB(x, y, best);
                
                    int neuR = (best >> 16) & 0xFF;
                    int neuG = (best >> 8) & 0xFF;
                    int neuB = (best) & 0xFF;

                    rDiff[x-1 +1] = rLast;
                    gDiff[x-1 +1] = gLast;
                    bDiff[x-1 +1] = bLast;

                    rLast = clip(R - neuR, MINDIFF, MAXDIFF);
                    gLast = clip(G - neuG, MINDIFF, MAXDIFF);
                    bLast = clip(B - neuB, MINDIFF, MAXDIFF);
                }
                else
                {
                    // transparent pixels cancel all error
                    rDiff[x-1 +1] = 0;
                    gDiff[x-1 +1] = 0;
                    bDiff[x-1 +1] = 0;

                    rLast = 0;
                    gLast = 0;
                    bLast = 0;
                }
            }
        }

        return canvas;
    }

    private static int clip(int value, int min, int max)
    {
        return (value > max) ? max : (value < min) ? min : value;
    }    
}

