/*
 * File: PlotTool.java
 * Creation: 2010_05_25
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.paintingtools;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import tilemaster.editor.Brush;
import tilemaster.editor.TilesetEditor;

/**
 * "Freehand" style drawing tool.
 * 
 * @author Hj. Malthaner
 */
public class PlotTool extends PaintingToolBase
{
    private TilesetEditor editor;
    private int firstX, firstY;

    /**
     * @return The redraw mode to use for this tool.
     */
    @Override
    public int getRefreshMode()
    {
        return REPAINT_NOTHING;
    }

    /**
     * @return The name to display for this tool.
     */
    @Override
    public String getToolName()
    {
        return "Draw";
    }

    /**
     * Set the editor which is calling this tool.
     * @param editor The tileset editor which is calling this tool.
     */
    @Override
    public void setEditor(TilesetEditor editor)
    {
        this.editor = editor;
    }

    /**
     * User clicked a new location
     */
    @Override
    public void firstClick(Graphics gr, int x, int y) 
    {
        firstX = x;
        firstY = y;

        editor.brush.draw(gr, x, y);
    }

    /**
     * User drags mouse
     */
    @Override
    public void paint(Graphics gr, int x, int y, boolean filled)
    {
        if(editor.brush.mode == Brush.Mode.PLAIN)
        {
            if(x != firstX || y != firstY) {
                gr.drawLine(firstX, firstY, x, y);

                firstX = x;
                firstY = y;
            }
        }
        else
        {
            editor.brush.draw(gr, x, y);            
        }
    }
    /**
     * This is called once the user clicked a color. The index of the
     * selected color is passed as parameter.
     * @param colorIndex The color's index in the color map.
     * @author Hj. Malthaner
     */
    @Override
    public void onColorSelected(int colorIndex)
    {
    }

    /** Creates a new instance of PlotTool */
    public PlotTool()
    {
    }
    
}
