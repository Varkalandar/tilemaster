/*
 * File: AnimationPanel.java
 *
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import tilemaster.tile.TileDescriptor;
import tilemaster.tile.TileSet;

/**
 *
 * @author Hj. Malthaner
 */
public class AnimationPanel extends JPanel
{
    private final TileSet tileSet;
    private final int start;
    private final int count;
    private final int delay;
    private final AnimationThread animationThread;
    private volatile int frame;

    public AnimationPanel(TileSet tileSet, int start, int count, int delay)
    {
        this.tileSet = tileSet;
        this.start = start;
        this.count = count;
        this.delay = delay;
    
        this.frame = 0;
        
        TileDescriptor tld = tileSet.get(start);
        Dimension dim = new Dimension(Math.max(tld.img.getWidth(), 128),
                                      Math.max(tld.img.getHeight(), 128));
        setPreferredSize(dim);
        
        animationThread = new AnimationThread();
    }
    
    public void start()
    {
        animationThread.start();
    }
    
    @Override
    public void paint(Graphics gr)
    {
        super.paint(gr);

        final TileDescriptor tld = tileSet.get(start);
        final int xoff = (getWidth() - tld.img.getWidth()) / 2;
        final int yoff = (getHeight() - tld.img.getHeight()) / 2;
        
        final Image img = tileSet.get(start + (frame % count)).img;
        gr.drawImage(img, xoff, yoff, this);
        // gr.drawString("" + frame, 10, 50);
    }

    private class AnimationThread extends Thread
    {
        public AnimationThread()
        {
            setDaemon(true);
        }
        
        @Override
        public void run()
        {
            while(true)
            {
                repaint();                
                try
                {
                    sleep(delay);
                }
                catch (InterruptedException ex)
                {
                    Logger.getLogger(AnimationPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                frame ++;
            }
        }
    }
}
