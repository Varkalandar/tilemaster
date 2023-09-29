/*
 * File: TilingPreview.java
 * Creation: ???
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;

/**
 *
 * @author Hj. Malthaner
 */
public class TilingPreview extends JPanel
{
    private boolean isIso;
    private Image tile;
    private int rasterW, rasterH;
    private int cursorX, cursorY;

    public void setIsometric(boolean isIso)
    {
        this.isIso = isIso;
    }

    public void setCursorPosition(int tileX, int tileY)
    {
        cursorX = tileX;
        cursorY = tileY;
    }

    public void setImage(Image img)
    {
        tile = img;
    }

    public void setRaster(int w, int h)
    {
        rasterW = w;
        rasterH = h;
    }

    @Override
    public void paint(Graphics gr)
    {
        super.paint(gr);
        
        if(tile != null) {
            final int w = getWidth();
            final int h = getHeight();

            for(int y=-rasterH*6; y<h+rasterH*2; y+=rasterH)
            {
                for(int x=-rasterW*2; x<w+rasterW*2; x+=rasterW)
                {
                    gr.setPaintMode();
                    final int off;
                    if(isIso)
                    {
                        off = ((y / rasterH) & 1) * rasterW / 2;
                    }
                    else
                    {
                        off = 0;
                    }
                    gr.drawImage(tile, x+off, y, this);

                    gr.setColor(Color.BLACK);
                    gr.fillRect(x+off+cursorX, y+cursorY-1, 1, 1);
                    gr.fillRect(x+off+cursorX-1, y+cursorY, 3, 1);
                    gr.fillRect(x+off+cursorX, y+cursorY+1, 1, 1);
                    gr.setColor(Color.WHITE);
                    gr.fillRect(x+off+cursorX, y+cursorY, 1, 1);
                }
            }
        }
    }
}
