/*
 * File: CopyShapeTool.java
 * 
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.paintingtools;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import tilemaster.editor.Selection;
import tilemaster.editor.TilesetEditor;

/**
 *Copy a shape from the tile background by drawing a 
 * polygon around it.
 * 
 * @author Hj. Malthaner
 */
public class CopyShapeTool extends ExtractShapeTool
{
    /**
     * @return The name to display for this tool.
     */
    @Override
    public String getToolName()
    {
        return "Copy Shape";
    }

    /** 
     * Creates a new instance of ExtractShapeTool 
     */
    public CopyShapeTool()
    {
    }
    
    
    @Override
    protected void finish()
    {
        BufferedImage original = src;
        
        final Polygon poly = new Polygon();
        for(final Point p : points)
        {
            poly.addPoint(p.x, p.y);
        }   
        
        Rectangle bounds = poly.getBounds();

        super.finish();
                
        Selection selection = editor.getSelection();
        
        if(selection.image == null) 
        {

            selection.image = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB);

            BufferedImage sub = canvas.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);

            Graphics sgr = selection.image.getGraphics();
            sgr.drawImage(sub, 0, 0, null);
            editor.updateImageViewOverlay(selection.image);
        }

        selection.setLocation(bounds.x, bounds.y);
        selection.setSize(new Dimension(bounds.width, bounds.height));

        editor.updateImageViewOverlayOffset(bounds.x, bounds.y);        

        // set undo to former state since we
        // clicked multiple times and the editor
        // only saved undo since the last click,
        // which doesn't make sense for this tool.
        editor.setUndo(original);
        editor.undo();
        
        // prepare for a new shape
        points.clear();
        currentPoint = -1;
        src = null;
        
        editor.setPaintingTool(new RubberbandSelection(editor));
        editor.repaint();
    }
}
