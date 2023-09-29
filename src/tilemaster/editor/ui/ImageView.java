/*
 * File: ImageView.java
 * Creation: 2010_05_25
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.JPanel;

/**
 * A JPanel that shows an image. Image offset from top left corner
 * can be specified. An underlay image and a selection area are
 * supported too.
 *
 * @author Hj. Malthaner
 */
public class ImageView extends JPanel
{    
    private Image underlay;
    private Image image;
    private Image overlay; // Overlay is also used to display a floating selection.
    
    private int xoff;
    private int yoff;
    private int uXoff;
    private int uYoff;
    private int oXoff;
    private int oYoff;
    
    /**
     * Selection area
     */
    private final Rectangle selectionArea;
    
    
    private int zoomLevel = 1;


    public ImageView(Rectangle r)
    {
        selectionArea = r;
        
        // Hajo: by default show no selection
        selectionArea.x = -1;
        
        setOpaque(true);
    }

    public void setZoomLevel(int n)
    {
        if(n != zoomLevel) 
        {
            zoomLevel = n;
            recalcSize();
            repaint();
        }
    }
    
    /**
     * Set selection area. Pass -1 for x1 to unset the whole area
     * @param x1 left 
     * @param y1 top 
     * @param x2 right 
     * @param y2 bottom 
     */
    public void setSelectionArea(int x1, int y1, int x2, int y2)
    {
        selectionArea.setBounds(x1, y1, x2-x1+1, y2-y1+1);
        repaint(100);
    }
    
    public void setOffset(int x, int y)
    {
        xoff = x;
        yoff = y;
    }
    
    public void setUnderlayOffset(int x, int y)
    {
        uXoff = x;
        uYoff = y;
    }

    public void setOverlayOffset(int x, int y)
    {
        oXoff = x;
        oYoff = y;
    }

    public void setImage(Image img)
    {
        image = img;
        recalcSize();
        repaint(100);
    }
    
    public void setUnderlay(Image img)
    {
        underlay = img;
        repaint(100);
    }
    
    public void setOverlay(Image img)
    {
        overlay = img;
        repaint(100);
    }

    @Override
    public void paint(final Graphics gr) 
    {
        super.paint(gr);
        
        if(underlay != null) 
        {
            final int w = underlay.getWidth(null);
            final int h = underlay.getHeight(null);

            gr.drawImage(underlay, (xoff+uXoff)*zoomLevel, (yoff+uYoff)*zoomLevel, 
                         w*zoomLevel, h*zoomLevel,
                         null);

            gr.setColor(Color.DARK_GRAY);

            for(int y=0; y<h; y++) 
            {
                gr.fillRect((0+xoff+uXoff)*zoomLevel, (y+yoff+uYoff)*zoomLevel, w*zoomLevel, 1);
            }
            for(int x=0; x<w; x++) 
            {
                gr.fillRect((x+xoff+uXoff)*zoomLevel, (0+yoff+uYoff)*zoomLevel, 1, h*zoomLevel);
            }
        }
        
        if(image != null) 
        {
            gr.drawImage(image, xoff*zoomLevel, yoff*zoomLevel, 
                         image.getWidth(null)*zoomLevel, image.getHeight(null)*zoomLevel,
                         null);
        }
                
        if(overlay != null) 
        {
            gr.drawImage(overlay, (xoff+oXoff)*zoomLevel, (yoff+oYoff)*zoomLevel, 
                         overlay.getWidth(this)*zoomLevel, overlay.getHeight(this)*zoomLevel,
                         null);
        }

        if(selectionArea.x != -1) 
        {
            gr.setColor(Color.CYAN);
            gr.drawRect(selectionArea.x * zoomLevel,
                        selectionArea.y * zoomLevel,
                        selectionArea.width * zoomLevel,
                        selectionArea.height * zoomLevel);
        }
    }

    private void recalcSize() 
    {
        if(image != null) 
        {
            Dimension size = 
                    new Dimension(xoff + image.getWidth(this)*zoomLevel, yoff + image.getHeight(this)*zoomLevel);
            setPreferredSize(size);
        }
        
        if(getParent() != null) 
        {
            getParent().doLayout();
        }
    }

}
