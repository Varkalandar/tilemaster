/*
 * File: ExtractShapeTool.java
 * Creation: 2012/09/14
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.paintingtools;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import tilemaster.editor.TilesetEditor;

/**
 * Extract a shape from the tile background by drawing a 
 * polygon around it.
 * 
 * @author Hj. Malthaner
 */
public class ExtractShapeTool extends PaintingToolBase
{
    protected ArrayList <Point> points = new  ArrayList <Point> (128);
    protected int currentPoint = -1;
    protected BufferedImage src;
    protected BufferedImage canvas;
    private Color lineColor = new Color(50, 210, 50);
    protected TilesetEditor editor;
    
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
        return "Extract Shape";
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
     * Sets the drawing area for tools which need direct access
     *
     * @param image The current tile image, writeable
     */
    @Override
    public void setCanvas(BufferedImage canvas)
    {
        this.canvas = canvas;
    }

    /**
     * User clicked a new location
     */
    @Override
    public void firstClick(final Graphics gr, final int x, final int y) 
    {
        if(points.isEmpty())
        {
            initSrcImage();
        }
        
        currentPoint = findPointIndex(x, y);
        
        if(currentPoint == -1)
        {
            // System.err.println("Adding new point x=" + x + " y=" + y);
            
            // no match
            final Point p = new Point(x,y);
            points.add(p);
        }
        else
        {
            // System.err.println("Moving old point i=" + currentPoint);
            
            if(currentPoint == 0)
            {
                // first point clicked again
                // we finish the operation now

                finish();
                return;
            }
            else
            {
                // old point - move it
                final Point p = points.get(currentPoint);
                p.x = x;
                p.y = y;
            }
        }
        
        drawPoly(gr);
    }

    /**
     * User drags mouse
     * 
     * @param x The new mouse x coordinate.
     * @param y The new mouse y coordinate.
     * @param gr canvas graphics
     * @param filled True if the shape should be drawn filled.
     */
    @Override
    public void paint(final Graphics gr, final int x, final int y, final boolean filled)
    {
        if(currentPoint >= 0)
        {
            // System.err.println("Moving old point i=" + currentPoint);
            
            Point p = points.get(currentPoint);
            p.x = x;
            p.y = y;
        }

        drawPoly(gr);
    }

    @Override
    public void onTileWillChange(int newIndex)
    {
        // prepare for a new shape
        points.clear();
        currentPoint = -1;

        // restore src image
        if(src != null)
        {
            for(int y=0; y<src.getHeight(); y++)
            {
                for(int x=0; x<src.getWidth(); x++)
                {
                    final int rgb = src.getRGB(x, y);
                    canvas.setRGB(x, y, rgb);
                }
            }
            
            // delete src, new tile needs a new one
            src = null;            
        }
    }
    
    /** 
     * Creates a new instance of ExtractShapeTool 
     */
    public ExtractShapeTool()
    {
    }
    
    
    private int findPointIndex(final int x, final int y)
    {
        int d = 10;
        int best = -1;
        for(int i=0; i<points.size(); i++)
        {
            final Point p = points.get(i);
            final int dd = Math.abs(x-p.x) + Math.abs(y-p.y);
            
            if(dd < d) 
            {
                d = dd;
                best = i;
            }
            
            if(dd == 0)
            {
                // perfect match found
                break;
            }
            
        }
        
        return best;
    }


    private void drawPoly(final Graphics gr)
    {
        if(!points.isEmpty())
        {
            for(int y=0; y<src.getHeight(); y++)
            {
                for(int x=0; x<src.getWidth(); x++)
                {
                    final int rgb = src.getRGB(x, y);
                    canvas.setRGB(x, y, rgb);
                }
            }

            final Polygon poly = new Polygon();
            for(final Point p : points)
            {
                poly.addPoint(p.x, p.y);
            }   

            gr.setColor(lineColor);
            gr.drawPolygon(poly);

            for(final Point p : points)
            {
                gr.setColor(Color.BLACK);
                gr.fillRect(p.x-2, p.y-2, 5, 5);
                gr.setColor(p == points.get(0) ? Color.RED : Color.WHITE);
                gr.fillRect(p.x-1, p.y, 3, 1);
                gr.fillRect(p.x, p.y-1, 1, 3);
            }    
        }
    }
    
    protected void finish()
    {
        final Polygon poly = new Polygon();
        for(final Point p : points)
        {
            poly.addPoint(p.x, p.y);
        }   
        
        for(int y=0; y<src.getHeight(); y++)
        {
            for(int x=0; x<src.getWidth(); x++)
            {
                if(poly.contains(x, y))
                {
                    final int rgb = src.getRGB(x, y);
                    canvas.setRGB(x, y, rgb);
                }
                else
                {
                    canvas.setRGB(x, y, 0);
                }
            }
        }

        // set undo to former state since we
        // clicked multiple times and the editor
        // only saved undo since the last click,
        // which doesn't make sense for this tool.
        editor.setUndo(src);
        
        // prepare for a new shape
        points.clear();
        currentPoint = -1;
        src = null;
    }

    private void initSrcImage()
    {
        src = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
        final Graphics sgr = src.getGraphics();
        sgr.drawImage(canvas, 0, 0, null);
        points.clear();
        currentPoint = -1;
    }
}
