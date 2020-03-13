/*
 * File: SpraySetupTool.java
 * Creation: 2011/12/16
 *
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */

package tilemaster.editor.paintingtools;

import java.awt.Graphics;
import tilemaster.editor.TilesetEditor;


/**
 * A simple spraycan tool.
 *
 * @author Hj. Malthaner
 */
public class SpraySetupTool extends PaintingToolBase
{
    private TilesetEditor editor;
    public static int x1, y1, x2, y2;
    public static int sleep = 10;

    private int state;

    /**
     * @return The redraw mode to use for this tool.
     */
    @Override
    public int getRefreshMode()
    {
        return REPAINT_REFRESH;
    }

    /**
     * @return The name to display for this tool.
     */
    @Override
    public String getToolName()
    {
        return "Spray Setup";
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
        x1 = x;
        y1 = y;
    }

    /**
     * User drags mouse.
     *
     * @param gr Graphics context to use for drawing
     * @param x X coordinate
     * @param y Y coordinate
     * @param filled Paint filled shapes. Ignored by this tool.
     */
    @Override
    public void paint(Graphics gr, int x, int y, boolean filled)
    {
        selectSize(gr, x, y);
    }

    /**
     * Called when the user releases the mouse button.
     */
    @Override
    public void onMouseReleased()
    {
        editor.undo();
    }

    private void selectSize(Graphics gr, int x, int y)
    {
        x2 = x;
        y2 = y;

        gr.drawOval(x1, y1, x2-x1, y2-y1);
    }        
    

    public SpraySetupTool()
    {
    }


}
