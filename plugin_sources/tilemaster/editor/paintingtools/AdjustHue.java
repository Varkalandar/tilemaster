/*
 * File: AdjustHue.java
 * Creation: 2012/09/11
 *
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */

package tilemaster.editor.paintingtools;

import asktools.Requester;
import asktools.ValueChangeInterface;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import tilemaster.editor.TilesetEditor;

/**
 * Adjust the hue of the currently displayed tile.
 * 
 * @author Hj. Malthaner
 */
public class AdjustHue extends YUVPaintingToolBase implements ValueChangeInterface
{
    private TilesetEditor editor;
    private BufferedImage canvas;
    private BufferedImage src;
    private int angle;
    private int saturation;
            
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
        return "Adjust Hue and Saturation";
    }

    /**
     * Sets the drawing area for tools which need direct access
     *
     * @param image The current tile image, writable
     */
    @Override
    public void setCanvas(BufferedImage canvas)
    {
        this.canvas = canvas;
        
        showUI();
    }

    /**
     * User clicked a new location
     */
    @Override
    public void firstClick(Graphics gr, int dummy1, int dummy2) 
    {
        showUI();
    }

    private void showUI()
    {
        src = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics sgr = src.getGraphics();
        sgr.drawImage(canvas, 0, 0, null);

        Requester requester = new Requester();
        requester.setDialogOffset(0, 196);
        requester.askValues(editor, 
                new String [] {"Color Shift:", "Saturation:"},
                0, 100, 50, this);
    }
    
    private void doAdjustment()
    {
        Cursor oldCursor = editor.getCursor();
        editor.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        final double [] yuv = new double[3];
        
        for(int y=0; y<canvas.getHeight(); y++)
        {
            for(int x=0; x<canvas.getWidth(); x++)
            {
                int rgb = src.getRGB(x, y);
                if((rgb & 0xFF000000) == 0xFF000000)
                {
                    rgbToYuv(yuv, rgb);
                    
                    yuvRotate(yuv, 3.6*angle, 1.0+saturation/50.0);
                                        
                    rgb = yuvToRGB(yuv);
                    
                    canvas.setRGB(x, y, 0xFF000000 | rgb);
                }
            }
        }

        editor.setCursor(oldCursor);
    }

    /**
     * User drags mouse
     */
    @Override
    public void paint(Graphics gr, int x, int y, boolean filled)
    {
    }
    
    /**
     * This is called once the user clicked a color. The index of the
     * selected color is passed as parameter.
     *
     * @param colorIndex The color's index in the color map.
     */
    @Override
    public void onColorSelected(int colorIndex)
    {
    }

    /**
     * Set the editor which is calling this tool.
     * @param editor The tile set editor which is calling this tool.
     */
    @Override
    public void setEditor(TilesetEditor editor)
    {
        this.editor = editor;
    }

    public AdjustHue()
    {
    }

    @Override
    public void onValueChanged(int [] values)
    {
        angle = values[0] - 50;
        saturation = values[1] - 50;
        
        // System.err.println("Angle=" + angle + " saturation=" + saturation);
        
        doAdjustment();
        editor.repaint(100);
    }
}
