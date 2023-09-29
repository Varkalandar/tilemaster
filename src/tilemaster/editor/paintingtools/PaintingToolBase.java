/*
 * File: PaintingToolBase.java
 * Creation: 2011/12/16
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.paintingtools;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import tilemaster.editor.TilesetEditor;

/**
 * Abstract base class for all painting tools. Mostly for
 * convenience because otherwise each tool must implement
 * all methods.
 * 
 * @author Hj. Malthaner
 */
public abstract class PaintingToolBase implements PaintingTool
{

    @Override
    public int getRefreshMode()
    {
        return REPAINT_NOTHING;
    }

    /**
     * The name is used for the tool plugin list if this
     * tool is used as a plugin.
     * 
     * @return The name to display for this tool.
     */
    @Override
    public abstract String getToolName();

    /**
     * Set the editor which is calling this tool.
     * @param editor The tileset editor which is calling this tool.
     */
    @Override
    public void setEditor(TilesetEditor editor)
    {
    }

    /**
     * Sets the drawing area for tools which need direct access
     *
     * @param image The current tile image, writeable
     */
    @Override
    public void setCanvas(BufferedImage image)
    {
    }

    /**
     * The user clicked a new location. This will always
     * be called before paint()
     * 
     * @param x The new mouse x coordinate.
     * @param y The new mouse y coordinate.
     * @param gr canvas graphics
     */
    @Override
    public void firstClick(Graphics gr, int x, int y)
    {
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
    }
    
    /**
     * Called when the user releases the mouse button.
     */
    @Override
    public void onMouseReleased()
    {
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
    }
}
