/*
 * File: SpraycanTool.java
 * Creation: 2016/03/14
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.paintingtools;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import static java.lang.Thread.sleep;
import tilemaster.editor.TilesetEditor;

/**
 * Draws a line.
 * 
 * @author Hj. Malthaner
 */
public class SpraycanTool extends PaintingToolBase
{
    private TilesetEditor editor;
    private final SprayThread sprayThread;

    private int mouseX, mouseY;

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
        return "Spraycan";
    }

    /**
     * Sets the drawing area for tools which need direct access
     *
     * @param image The current tile image, writeable
     */
    @Override
    public void setCanvas(BufferedImage image)
    {
        // Not needed for this tool.
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
        mouseX = x;
        mouseY = y;

        sprayThread.gr = gr;
        sprayThread.spray = true;
    }

    /**
     * User drags mouse
     */
    @Override
    public void paint(Graphics gr, int x, int y, boolean filled)
    {
        mouseX = x;
        mouseY = y;

        sprayThread.gr = gr;
        sprayThread.spray = true;
    }
    
    /**
     * Called when the user releases the mouse button.
     */
    @Override
    public void onMouseReleased()
    {
        sprayThread.spray = false;
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

    /** Creates a new instance of LineTool */
    public SpraycanTool()
    {
        sprayThread = new SprayThread();
        sprayThread.setDaemon(true);
        sprayThread.start();
    }    

    private class SprayThread extends Thread
    {
        Graphics gr = null;
        boolean spray;
        boolean go = true;
        int sleep;
        
        public void quit()
        {
            spray = false;
            go = false;
            sleep = 100;
        }

        @Override
        public void run()
        {
            while(go)
            {
                try
                {
                    sleep(sleep);
                }
                catch (InterruptedException e)
                {
                    // ignore ...
                }
                
                if(gr != null && spray)
                {
                    sleep = SpraySetupTool.sleep;
                    
                    double angle = Math.PI * 2 * Math.random();
                    double rad = Math.random();
                    
                    // elliptic density
                    rad *= rad;
                    
                    double radX = Math.abs(SpraySetupTool.x2 - SpraySetupTool.x1) * rad / 2;
                    double radY = Math.abs(SpraySetupTool.y2 - SpraySetupTool.y1) * rad / 2;
                    
                    int posX = (int)(Math.cos(angle) * radX);
                    int posY = (int)(Math.sin(angle) * radY);

                    editor.brush.draw(gr, posX+mouseX, posY+mouseY);
                    
                    editor.repaint(50);
                }
                else
                {
                    sleep = 100;
                }
            }
        }
    }
}


