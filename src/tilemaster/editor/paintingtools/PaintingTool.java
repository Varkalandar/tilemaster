/*
 * File: PaintingTool.java
 * Creation: 2010_05_25
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.paintingtools;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import tilemaster.editor.TilesetEditor;

/**
 * All painting tools must implement this interface.
 * 
 * @author Hj. Malthaner
 */
public interface PaintingTool 
{
    /**
     * Refresh image during drag operations
     */
    public static final int REPAINT_REFRESH = 6;

    /**
     * Keep result of former paint operations while dragging
     */
    public static final int REPAINT_NOTHING = 7;


    /**
     * Tools can choose between REPAINT_REFRESH and
     * REPAINT_NOTHING.
     * 
     * @return The redraw mode to use for this tool.
     */
    public int getRefreshMode();

    /**
     * The name is used for the tool plugin list if this
     * tool is used as a plugin.
     * 
     * @return The name to display for this tool.
     */
    public String getToolName();

    /**
     * Set the editor which is calling this tool.
     * @param editor The tileset editor which is calling this tool.
     */
    public void setEditor(TilesetEditor editor);

    
    /**
     * Sets the drawing area for tools which need direct access
     * 
     * @param image The current tile image, writeable
     */
    public void setCanvas(BufferedImage image);


    /**
     * This is called when the user changes the current tile.
     * It's called just before the drawing canvas is updated
     * witht he new tile image.
     * 
     * @param newIndex The number of the newly selected tile.
     */
    public void onTileWillChange(int newIndex);
    
    /**
     * The user clicked a new location. This will always
     * be called before paint()
     * 
     * @param x The new mouse x coordinate.
     * @param y The new mouse y coordinate.
     * @param gr canvas graphics
     */
    public void firstClick(Graphics gr, int x, int y);
    
    /**
     * Called when the user releases the mouse button.
     */
    public void onMouseReleased();

    /**
     * Called when the user moves the mouse.
     * @param x The new mouse x coordinate.
     * @param y The new mouse y coordinate.
     * @param gr canvas graphics
     */
    public void onMouseMoved(Graphics gr, int x, int y);

    /**
     * User drags mouse - do the actual painting here.
     * 
     * @param x The new mouse x coordinate.
     * @param y The new mouse y coordinate.
     * @param gr canvas graphics
     * @param filled True if the shape should be drawn filled.
     */
    public void paint(Graphics gr, int x, int y, boolean filled);

    /**
     * This is called once the user clicked a color. The index of the
     * selected color is passed as parameter.
     *
     * @param colorIndex The index of the newly selected color in the
     * color map.
     */
    public void onColorSelected(int colorIndex);
}
