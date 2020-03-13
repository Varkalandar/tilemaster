/*
 * File: SelectionAsMask.java
 * Creation: 2012/09/13
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.paintingtools;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import tilemaster.editor.Selection;
import tilemaster.editor.TilesetEditor;

/**
 * Use the selection as a mask (inverted stamp). 
 * 
 * @author Hj. Malthaner
 */
public class SelectionAsMask extends PaintingToolBase
{
    private Selection selection;
    private TilesetEditor tilesetEditor;
    private BufferedImage canvas;
    
    @Override
    public int getRefreshMode()
    {
        return REPAINT_REFRESH;
    }

    public SelectionAsMask(TilesetEditor tilesetEditor)
    {
        this.tilesetEditor = tilesetEditor;
        selection = tilesetEditor.getSelection();
        
        // hide the selection image now, but keep the 
        // selection frame
        tilesetEditor.updateImageViewOverlay(null);
    }

    /**
     * The name is used for the tool plugin list if this
     * tool is used as a plugin.
     * 
     * @return The name to display for this tool.
     */
    @Override
    public String getToolName()
    {
        return "Selection As Mask";
    }

    /**
     * Sets the drawing area for tools which need direct access
     *
     * @param canvas The current tile image, writeable
     */
    @Override
    public void setCanvas(BufferedImage canvas)
    {
        this.canvas = canvas;
        
        Graphics gr = canvas.getGraphics();
        onMouseMoved(gr, 0, 0);
    }

    /**
     * This is called when the user changes the current tile.
     * It's called just before the drawing canvas is updated
     * witht he new tile image.
     * 
     * @param newIndex The number of the newly selected tile.
     */
    @Override
    public void onTileWillChange(int newIndex)
    {
        tilesetEditor.undo();
    }

    /**
     * User clicked a new location. This will always
     * be called before paint()
     */
    @Override
    public void firstClick(Graphics gr, int x, int y)
    {
        // now make it permanent
        selection.clear();
        tilesetEditor.repaint();
    }

    /**
     * User drags mouse - do the actual painting here.
     * 
     * @param x The new mouse x coordinate.
     * @param y The new mouse y coordinate.
     * @param gr canvas graphics
     * @param filled True if the shape should be drawn filled.
     */
    @Override
    public void paint(Graphics gr, int x, int y, boolean filled)
    {
    }
    
    /**
     * This is called once the user clicked a color. The index of the
     * selected color is passed as parameter.
     *
     * @param colorIndex The index of the newly selected color in the
     * color map.
     */
    @Override
    public void onColorSelected(int colorIndex)
    {
        Graphics gr = canvas.getGraphics();
        Color c = tilesetEditor.getColorPalette().getColor(colorIndex);
        gr.setColor(c);
        
        onMouseMoved(gr, 0, 0);
    }  
    
    /**
     * Called when the user moves the mouse.
     * @param x The new mouse x coordinate.
     * @param y The new mouse y coordinate.
     * @param gr canvas graphics
     */
    @Override
    public void onMouseMoved(Graphics gr, int x, int y)
    {
        if(selection.image != null) 
        {
            selection.setLocation(x, y);
            tilesetEditor.undo();

            for(int j=0; j<selection.image.getHeight(); j++)
            {
                for(int i=0; i<selection.image.getWidth(); i++)
                {
                    final int rgb = selection.image.getRGB(i, j);
                    final int alpha = rgb >>> 24;
                    
                    if(alpha > 127)
                    {
                        // Opaque pixel
                    }
                    else
                    {
                        // clear pixel

                        // we want clear pixels to show masking color.
                        gr.fillRect(x+i, y+j, 1, 1);
                    }
                }
            }
            
            tilesetEditor.repaint(50);
        }        
    }
}
