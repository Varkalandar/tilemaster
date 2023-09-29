/*
 * File: TransformBlackPoint.java
 * Creation: ???
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.transforms;

import asktools.Requester;
import java.awt.image.BufferedImage;
import tilemaster.editor.TilesetEditor;

/**
 *Adjust the blackpoint of the tile.
 * @author Hj. Malthaner
 */
public class TransformBlackPoint implements TileTransform
{
    private static String input = "256 256 256";
    private int rf, gf, bf;

    @Override
    public boolean  askUserData(TilesetEditor parent)
    {
        Requester requester = new Requester();
        input = requester.askString(parent,
                "Please enter the RGB levels:",
                input);
        
        String [] parts = input.split(" ");

        if(parts.length == 3)
        {
            rf = Integer.parseInt(parts[0]);
            gf = Integer.parseInt(parts[1]);
            bf = Integer.parseInt(parts[2]);
        } else {
            rf = gf = bf = 256;
            input = "256 256 256";
        }

        return true;
    }

    public BufferedImage transform(BufferedImage canvas)
    {
        for(int y=0; y<canvas.getHeight(); y++) {
            for(int x=0; x<canvas.getWidth(); x++) {
                int argb = canvas.getRGB(x, y);

                int a = argb >>> 24;
                int r = 255 - ((argb >>> 16) & 0xFF);
                int g = 255 - ((argb >>> 8) & 0xFF);
                int b = 255 - ((argb >>> 0) & 0xFF);

                if(a > 127) {

                    r = 255 - ((r * rf) >>> 8);
                    g = 255 - ((g * gf) >>> 8);
                    b = 255 - ((b * bf) >>> 8);

                    r = Math.min(Math.max(r, 0), 255);
                    g = Math.min(Math.max(g, 0), 255);
                    b = Math.min(Math.max(b, 0), 255);

                    int rgb =
                            0xFF000000 |
                            (r << 16) |
                            (g << 8) |
                            (b << 0);

                    canvas.setRGB(x, y, rgb);
                }
            }
        }

        return canvas;
    }
}
