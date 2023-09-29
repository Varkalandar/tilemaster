/*
 * File: AnimationPreview.java
 *
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */
package tilemaster.editor.ui;

import java.awt.Color;
import tilemaster.tile.TileSet;

/**
 *
 * @author Hj. Malthaner
 */
public class AnimationPreview extends PreviewFrame
{
    private final AnimationPanel animationPanel;
    
    public AnimationPreview(TileSet tileSet, int start, int count, int delay)
    {
        super();
        
        animationPanel = new AnimationPanel(tileSet, start, count, delay);
        
        add(animationPanel);
        
        setTitle("Animation Preview");
        setLocation(800, 10);
        pack();
        
        animationPanel.start();
    }
    
    @Override
    public void setBackground(Color c) 
    {        
        super.setBackground(c);
        
        if(animationPanel != null)
        {
            animationPanel.setBackground(c);
        }
    }
}
