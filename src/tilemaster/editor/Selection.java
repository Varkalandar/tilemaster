/*
 * File: Selection.java
 * Creation: 2011/12/12
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Refactored selection data and methods.
 * 
 * @author Hj. Malthaner
 */
public class Selection
{
    public BufferedImage image;
    private Rectangle rectangle;


    Selection(Rectangle rectangle)
    {
        this.rectangle = rectangle;
    }

    public void clear()
    {
        rectangle.x = -1;
        image = null;
    }

    public void setLocation(int x, int y)
    {
        rectangle.setLocation(x, y);
    }
    
    public void setSize(Dimension size)
    {
        rectangle.setSize(size);
    }

    public int getX()
    {
        return rectangle.x;
    }

    public int getY()
    {
        return rectangle.y;
    }

    public int getWidth()
    {
        return rectangle.width;
    }
    
    public int getHeight()
    {
        return rectangle.height;
    }

    public boolean inside(int x, int y)
    {
        return rectangle.contains(x, y);
    }

    void flipHoriz()
    {
        if(image != null)
        {
            // System.err.println("flipHoriz");

            final int width = image.getWidth();
            final int height = image.getHeight();

            for(int j=0; j<height; j++)
            {
                for(int i=0; i<width/2; i++)
                {
                    final int argb = image.getRGB(i,j);
                    image.setRGB(i, j, image.getRGB(width-1-i, j));
                    image.setRGB(width-1-i, j, argb);
                }
            }
        }
    }
    
    void flipVert()
    {
        if(image != null)
        {
            // System.err.println("flipVert");

            final int height = image.getHeight();
            final int width = image.getWidth();

            for(int j=0; j<height/2; j++)
            {
                for(int i=0; i<width; i++)
                {
                    final int argb = image.getRGB(i, j);
                    image.setRGB(i, j, image.getRGB(i, height-1-j));
                    image.setRGB(i, height-1-j, argb);
                }
            }
        }
    }

    @Override
    public String toString()
    {
        return rectangle.toString();
    }
}
