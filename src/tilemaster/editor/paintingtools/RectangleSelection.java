/*
 * File: RectangleSelection.java
 * Creation: 2011_12_28
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */
package tilemaster.editor.paintingtools;

import asktools.Requester;
import java.awt.Dimension;
import java.awt.Graphics;
import tilemaster.editor.Selection;
import tilemaster.editor.TilesetEditor;

/**
 * Let the user define a rectangle by size to select an area.
 *
 * @author Hj. Malthaner
 */
public class RectangleSelection extends PaintingToolBase
{
    private enum Mode {MOVE, DONE};

    private Mode mode = Mode.MOVE;
    private String input = "64x64";
    private Selection selection;
    private TilesetEditor tilesetEditor;

    @Override
    public String getToolName()
    {
        return "Select Rectangle";
    }


    public RectangleSelection(TilesetEditor tilesetEditor)
    {
        this.tilesetEditor = tilesetEditor;

        Requester requester = new Requester();
        Dimension size = requester.askDimension(tilesetEditor, "Please enter the rectangle size:", input);
        input = "" + size.width + "x" + size.height;

        this.selection = tilesetEditor.getSelection();
        this.selection.setSize(size);
    }

    /**
     * Called when the user releases the mouse button.
     */
    @Override
    public void onMouseReleased()
    {
        mode = Mode.DONE;
        tilesetEditor.selectRubberbandSelectionButton();
    }

    /**
     * Called when the user moves the mouse.
     * 
     * @param x The new mouse x coordinate.
     * @param y The new mouse y coordinate.
     * @param gr canvas graphics
     */
    @Override
    public void onMouseMoved(Graphics gr, int x, int y)
    {
        if(mode == Mode.MOVE)
        {
            selection.setLocation(x, y);
            tilesetEditor.repaint(50);
        }
    }

}
