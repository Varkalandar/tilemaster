/*
 * File: ReduceToColormap.java
 * Creation: 2011/12/12
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.transforms;

import asktools.ColorPalette;
import java.awt.image.BufferedImage;
import tilemaster.editor.TilesetEditor;

/**
 * Reduce tile colors to those found in the colormap.
 *
 * @author Hj. Malthaner
 */
public class ReduceToColormap implements TileTransform
{
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

        for(int y=0; y<height; y++)
        {
            for(int x=0; x<width; x++)
            {
                final int argb = canvas.getRGB(x, y);
                final int index = palette.bestColorMatch(argb);

                if(index != 0)
                {
                    final int rgb = palette.getColor(index).getRGB();
                    canvas.setRGB(x, y, rgb);
                }
            }
        }

        return canvas;
    }
}
