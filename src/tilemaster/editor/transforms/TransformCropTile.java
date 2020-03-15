/*
 * File: TransformCropTile.java
 * Creation: 2011/12/12
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.transforms;

import java.awt.image.BufferedImage;
import tilemaster.editor.TilesetEditor;

/**
 * Crop tile image to the minum area which covers all non-transparent pixels.
 * @author Hj. Malthaner
 */
public class TransformCropTile implements TileTransform
{
    private int bgColor;

    /**
     * If this transform requires user input, ask for details.
     * @param parent The parent frame.
     * @return true to execute the transform, flase to cancel.
     */
    public boolean askUserData(TilesetEditor parent)
    {
        bgColor = parent.getColorPalette().getColor(0).getRGB();
        return true;
    }


    /**
     * Transform the image in the desired way
     * @param canvas The image to transform.
     * @return The new image
     */
    public BufferedImage transform(BufferedImage canvas)
    {
        int minX = canvas.getWidth();
        int minY = canvas.getHeight();
        int maxX = 0;
        int maxY = 0;

        // Hajo: Scan for bounds
        for(int y=0; y<canvas.getHeight(); y++)
        {
            for(int x=0; x<canvas.getWidth(); x++)
            {
                final int argb = canvas.getRGB(x, y);
                final int a = (argb >>> 24) & 255;

                if(a > 0 && argb != bgColor)
                {
                    if(x < minX) minX = x;
                    if(y < minY) minY = y;
                    if(x > maxX) maxX = x;
                    if(y > maxY) maxY = y;
                }
            }
        }

        System.err.println("Image area: " + minX + ", " + minY + ", " + maxX + ", " + maxY);

        final int width = maxX - minX + 1;
        final int height = maxY - minY + 1;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for(int y=0; y<height; y++) {
            for(int x=0; x<width; x++) {
                final int argb = canvas.getRGB(x+minX, y+minY);
                final int a = (argb >>> 24) & 255;

                if(a > 0 && argb != bgColor)
                {
                    img.setRGB(x, y, argb);
                } else {
                    img.setRGB(x, y, 0);
                }
            }
        }

        return img;
    }

}
