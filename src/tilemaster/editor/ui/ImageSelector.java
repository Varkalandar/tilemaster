/*
 * File: ImageSelector.java
 * Creation: 2010_05_25
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JPanel;
import tilemaster.tile.TileDescriptor;

/**
 * This class shows a list (horizontally or vertically) 
 * of images and allows the user to select one by using the mouse.
 *
 * @author Hj. Malthaner
 */
public class ImageSelector extends JPanel {
    
    private final static int SPACING = 64;
    
    private TileDescriptor [] images;
    private Dimension size;
    
    private ImageSelectorInterface callback;
    private int selection = 0;
    private int hover = -1;
    
    private Font font = new Font("SANS_SERIF", 0, 11);
    
    public int getSelection() {
        return selection;
    }
    
    public void paint(Graphics gr) 
    {
        final Rectangle clip = gr.getClipBounds();
        
        gr.setFont(font);
        // gr.setColor(Color.GRAY);
        gr.setColor(Color.DARK_GRAY);
        gr.fillRect(0, 0, getWidth(), getHeight());
        
        for(int i=0; i<images.length; i++) {
            final int xpos = i*SPACING;

            if(clip.contains(xpos, 0) ||
               clip.contains(xpos, size.height - 1) ||
               clip.contains(xpos + SPACING - 1, 0) ||
               clip.contains(xpos + SPACING - 1, size.height -1)) {
            
                if(hover == i) {
                    gr.setColor(Color.LIGHT_GRAY);
                    gr.fillRect(xpos, 0, SPACING, size.height);
                }

                if(selection == i) {
                    gr.setColor(Color.LIGHT_GRAY);
                    gr.fillRect(xpos, 0, SPACING, size.height);
                    gr.setColor(Color.DARK_GRAY);
                    gr.fillRect(xpos, 0, SPACING, 1);
                    gr.fillRect(xpos, 0, 1, size.height);
                    gr.setColor(Color.WHITE);
                    gr.fillRect(xpos, size.height-1, SPACING, 1);
                    gr.fillRect((i+1)*SPACING-1, 1, 1, size.height-1);
                }
                gr.drawImage(images[i].img,
                             xpos + (SPACING-images[i].img.getWidth())/2,
                             size.height - images[i].img.getHeight() - 3, null);

                gr.setColor(Color.WHITE);

                gr.drawString("#" + i, xpos+4, 12);

                /*
                if(images[i].name.length() > 0) {
                    ShadedText.draw(gr, xpos+4, 24, Color.WHITE, Color.DARK_GRAY, images[i].name);
                }
                 */
            }
        }
    }
        
    /** Creates a new instance of ImageSelector */
    public ImageSelector(TileDescriptor [] images, ImageSelectorInterface peer)
    {
        this.images = images;
        
        size = new Dimension(images.length*SPACING, 96);
        
        setOpaque(false);
        setDoubleBuffered(false);

        setSize(size);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        
        callback = peer;
        
        addMouseListener(new MouseEventHandler());
        addMouseMotionListener(new MouseMotionHandler());
    }

    
    private class MouseEventHandler extends MouseAdapter
    {
        public void mouseReleased(MouseEvent e) 
        {
            selection = e.getX()/SPACING;
            repaint(200);
            callback.imageClicked(selection);
        }

        public void mouseExited(MouseEvent e) 
        {
            hover = -1;
            repaint(200);
        }
    }

    private class MouseMotionHandler extends MouseMotionAdapter
    {
        public void mouseMoved(MouseEvent e) 
        {
            hover = e.getX()/SPACING;
            repaint(200);
        }
        
        /*
        public void mouseDragged(MouseEvent e)
        {
            selection = e.getX()/SPACING;
            repaint(200);
            callback.imageClicked(selection);
        }
        */
    }
}
