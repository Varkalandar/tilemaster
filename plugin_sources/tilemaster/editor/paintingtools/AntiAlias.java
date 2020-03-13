/*
 * File: AntiAlias.java
 * Creation: 2012/08/24
 *
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */

package tilemaster.editor.paintingtools;

import asktools.Requester;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import tilemaster.editor.TilesetEditor;

/**
 * Anti-Aliases pixels of the chosen color in the current tile.
 * 
 * @author Hj. Malthaner
 */
public class AntiAlias extends PaintingToolBase 
{
    private TilesetEditor editor;
    private BufferedImage canvas;
    private int weight;
    
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
        return "Anti Alias";
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

        Requester requester = new Requester();
        weight = requester.askNumber(editor, "Please enter the midpoint weight:", weight);
        
        firstClick(null, 0, 0);
        editor.repaint();
    }

    /**
     * User clicked a new location
     */
    @Override
    public void firstClick(Graphics g, int dummy1, int dummy2) 
    {
        BufferedImage aaImg = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);

        aaImg.getGraphics().drawImage(canvas, 0, 0, null);
        
        Cursor oldCursor = editor.getCursor();
        editor.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        Graphics gr = canvas.getGraphics();
        Color color = gr.getColor();
        final int argb = color.getRGB();
        final int oldWeight = weight;
        
        for(int y=0; y<canvas.getHeight(); y++)
        {
            for(int x=0; x<canvas.getWidth(); x++)
            {
                int c = canvas.getRGB(x, y);
                weight = oldWeight;
                
                if(c == argb)
                {
                    // vertical line?
                    if(getRGB(x, y-1) == argb && getRGB(x, y+1) == argb)
                    {
                        weight = weight * 2;
                        if(getRGB(x, y-2) == argb && getRGB(x, y+2) == argb)
                        {
                            continue;
                        }
                    }
                    // horizontal line?
                    if(getRGB(x-1, y) == argb && getRGB(x+1, y) == argb)
                    {
                        weight = weight * 2;
                        if(getRGB(x-2, y) == argb && getRGB(x+2, y) == argb)
                        {
                            continue;
                        }
                    }
                    
                    aaImg.setRGB(x, y, aa(x, y, argb));
                }
            }
            
        }
        
        
        for(int y=0; y<canvas.getHeight(); y++)
        {
            for(int x=0; x<canvas.getWidth(); x++)
            {
                int c = aaImg.getRGB(x, y);
                canvas.setRGB(x, y, c);
            }
            
        }

        weight = oldWeight;
        editor.setCursor(oldCursor);
    }
    
    private int getRGB(int x, int y)
    {
        if(x>=0 && x<canvas.getWidth() && y>=0 && y<canvas.getHeight())
        {
            return canvas.getRGB(x, y);
        }
        
        return 0;
    }

    /**
     * Create an interpolated pixel.
     */
    private int aa(int x, int y, int argb)
    {
        int R = 0;
        int G = 0;
        int B = 0;
        
        for(int j=y-1; j<=y+1; j++)
        {
            for(int i=x-1; i<=x+1; i++)
            {
                final int rgb = getRGB(i, j);
                
                R += (rgb >> 16) & 0xFF;
                G += (rgb >> 8) & 0xFF;
                B += rgb & 0xFF;
            }
        }
        
        R += ((argb >> 16) & 0xFF) * weight;
        G += ((argb >> 8) & 0xFF) * weight;
        B += (argb & 0xFF) * weight;
        
        R /= 9 + weight;
        G /= 9 + weight;
        B /= 9 + weight;
        
        return 0xFF000000  | (R << 16) | (G << 8) | B;
        
/*        
        final int rgb1 = getRGB(x+1, y) & 0xFCFCFCFC;
        final int rgb2 = getRGB(x-1, y) & 0xFCFCFCFC;
        final int rgb3 = getRGB(x, y+1) & 0xFCFCFCFC;
        final int rgb4 = getRGB(x, y-1) & 0xFCFCFCFC;
        
        final int sum = rgb1 + rgb2 + rgb3 +rgb4;
        
        // final int aa = ((sum >> 2) & 0xFEFEFE) + (argb & 0xFEFEFE);
        // return 0xFF000000 | (aa >> 1);

        final int aa = ((sum >> 2) & 0xFCFCFC) + ((argb & 0xFCFCFC) * 3);
        return 0xFF000000 | (aa >> 2);
        */
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

    /** Creates a new instance of CrossTool */
    public AntiAlias()
    {
        weight = 12;
    }   
}
