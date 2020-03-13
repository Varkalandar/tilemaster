/*
 * File: RubberbandSelection.java
 * Creation: 2011/12/28
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.paintingtools;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import tilemaster.editor.Selection;
import tilemaster.editor.TilesetEditor;

/**
 * Let the user drag a rubberband rectangle with the mouse to define the
 * selection area.
 * 
 * @author Hj. Malthaner
 */
public class RubberbandSelection extends PaintingToolBase
{
    private final Selection selection;
    private final TilesetEditor tilesetEditor;
    private BufferedImage canvas;
    
    private int clickX, clickY;
    
    public RubberbandSelection(TilesetEditor tilesetEditor)
    {
        this.tilesetEditor = tilesetEditor;
        selection = tilesetEditor.getSelection();
    }

    @Override
    public String getToolName()
    {
        return "Rubberband Selection";
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
    }

    /**
     * User clicked a new location. This will always
     * be called before paint()
     */
    @Override
    public void firstClick(Graphics gr, int x, int y)
    {
        int sx = selection.getX();
        int sy = selection.getY();
        int sw = selection.getWidth();
        int sh = selection.getHeight();

        if(selection.inside(x, y)) 
        {
            // Cut and move
            if(selection.image == null) 
            {

                selection.image = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);

                BufferedImage sub = canvas.getSubimage(sx, sy, sw, sh);

                Graphics sgr = selection.image.getGraphics();
                sgr.drawImage(sub, 0, 0, null);
                tilesetEditor.updateImageViewOverlay(selection.image);
            }

            selection.setLocation(x, y);

            tilesetEditor.updateImageViewOverlayOffset(x, y);

        }
        else 
        {

            if(selection.image != null) 
            {
                Graphics cgr = canvas.getGraphics();
                cgr.drawImage(selection.image, sx, sy, null);


                selection.image = null;
                tilesetEditor.updateImageViewOverlay(null);
            }

            clickX = x;
            clickY = y;
            
            selection.setLocation(x, y);            
            selection.setSize(new Dimension(1, 1));
            tilesetEditor.updateImageViewOverlayOffset(0, 0);
        }
    }

    /**
     * User drags mouse - do the actual painting here.
     */
    @Override
    public void paint(Graphics gr, int x, int y, boolean filled)
    {
        if(selection.image == null) 
        {
            int x1 = clickX;
            int x2 = x;
            int y1 = clickY;
            int y2 = y;

            // reorder coordinates if needed so that x1,y1 is always the
            // top left corner and x2, y2 is the bottom right.
            
            if(clickX > x)
            {
                x1 = x;
                x2 = clickX;
            }
            
            if(clickY > y)
            {
                y1 = y;
                y2 = clickY;
            }
            
            tilesetEditor.updateImageViewSelection(x1, y1, x2, y2);
            
            // System.err.println("Selection: " + selection);
        }
        else
        {
            selection.setLocation(x, y);

            // Move image
            tilesetEditor.updateImageViewOverlayOffset(selection.getX(),
                                                       selection.getY());
        }
    }
}
