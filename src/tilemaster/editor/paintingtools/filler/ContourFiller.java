/*
 * File: ContourFiller.java
 * Creation: 2011_11_20
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.paintingtools.filler;

import asktools.Requester;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import tilemaster.editor.paintingtools.Fillmachine;
import tilemaster.editor.paintingtools.filler.gradients.Func;
import tilemaster.editor.paintingtools.filler.gradients.FuncLinear;
import tilemaster.editor.paintingtools.filler.gradients.FuncV2;
import tilemaster.editor.paintingtools.filler.gradients.FuncVee;
import tilemaster.editor.paintingtools.filler.gradients.Gradient;
import tilemaster.editor.paintingtools.filler.gradients.GradientLinear;
import tilemaster.editor.paintingtools.filler.gradients.GradientV2;

/**
 * Fill contours with color gradients.
 *
 * @author Hj. Malthaner
 */
public class ContourFiller implements Fillmachine.Filler
{
    private final int [] colors;
    private BufferedImage canvas;
    private Func function;
    private Gradient gradient;
    private JFrame parent;
    private final double startAngle;
    private final double angleStep;
    
    public ContourFiller(final JFrame parent, 
                         final asktools.ColorPalette colorPalette,
                         final double startAngle,
                         final double angleStep)
    {
        this.parent = parent;
        this.startAngle = startAngle;
        this.angleStep = angleStep;
        
        Requester requester = new Requester();
        
        colors = 
            requester.askColorsRGB(parent,
                    new String []
                    {
                        "Please select the first color:",
                        "Please select the second color:"
                    },
                    colorPalette);
        
                                       
        final String selection = 
            requester.askChoice(parent,
                    "Please select the gradient type:", 
                    "(L)inear|(C)ushion|(H)ighlight|(S)hine");

                                       
        switch(selection.charAt(0))
        {
            case 'l':
                gradient = new GradientLinear();
                function = new FuncLinear();
                break;
            case 'c':
                gradient = new GradientLinear();
                function = new FuncVee();
                break;
            case 'h':
                gradient = new GradientV2();
                function = new FuncVee();
                break;
            case 's':
                gradient = new GradientV2();
                function = new FuncV2();
                break;
            default:
                gradient = new GradientLinear();
                function = new FuncVee();
                System.err.println("Invalid gradient selection!");
        }
    }
    
    
    public void start(final BufferedImage canvas, final Color paintColor)
    {
        this.canvas = canvas;
    }

    public void finish(final byte [] marks)
    {
        final Cursor cursor = parent.getCursor();

        try
        {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            final int height = canvas.getHeight();
            final int width = canvas.getWidth();

            for(int y=0; y<height; y++)
            {
                for(int x=0; x<width; x++)
                {
                    if(marks[y*width + x] != 0)
                    {
                        double sum = 0;
                        int div = 0;

                        for(double r=startAngle; r<Math.PI; r+=angleStep)
                        {
                            final double sin = Math.sin(r);
                            final double cos = Math.cos(r);

                            final int l1 = seek(marks, x, y, sin, cos, width, height);
                            final int l2 = seek(marks, x, y, -sin, -cos, width, height);

                            if(l1 < 0) System.err.println("l1=" + l1);
                            if(l2 < 0) System.err.println("l2=" + l2);

                            if(l1 + l2 != 0)
                            {
                                sum += function.calc(l1, l2);
                                // sum += function.calc(0, 10);
                                // sum += function.calc(10, 0);
                                div ++;
                            }
                        }

                        final int rgb = calcRGB(canvas.getRGB(x, y), colors[0], colors[1], sum, div);
                        // final int rgb = gradient.calcRGB(colors[0], colors[1], 0.0);
                        // final int rgb = gradient.calcRGB(colors[0], colors[1], 1.0);

                        canvas.setRGB(x, y, rgb);
                    }
                }
            }
        }
        finally
        {
            parent.setCursor(cursor);
        }
    }

    protected int calcRGB(int oldRgb, int rgb1, int rgb2, double sum, double div)
    {
        return gradient.calcRGB(rgb1, rgb2, sum/div);
    }

    public void plotInside(int x, int y) 
    {
    }

    public void plotBorder(int x, int y)
    {
    }

    public void plotLine(int x, int y, int xx, int yy)
    {
    }
    
    private int seek(final byte [] marks,
                     final int x, final int y,
                     final double sin, final double cos,
                     final int width, final int height)
    {
        final int xs = (int)(cos * (1<<16));
        final int ys = (int)(sin * (1<<16));

        int xx = (x << 16) + (1<<15);
        int yy = (y << 16) + (1<<15);

        for(int step=0; step < width+height; step ++)
        {
            xx += xs;
            yy += ys;

            final int px = xx >>> 16;
            final int py = yy >>> 16;

            if(px >= 0 && py >= 0 && px < width && py < height)
            {
                if(marks[py*width + px] == 0)
                {
                    return step;
                }
            }
            else
            {
                return step;
            }
        }

        return width+height;
    }
}
