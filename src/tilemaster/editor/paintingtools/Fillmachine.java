/*
 * File: Fillmachine.java
 * Creation: 2010_06_02
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.paintingtools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import tilemaster.editor.colors.RGB;

/**
 * Fillmachine implements a flood fill algorithm and
 * uses "Filler" objects for operatiosn to be done
 * on the filled area.
 *
 * @author Hj. Malthaner
 */
public class Fillmachine extends PaintingToolBase
{
    private static final int PASS_HORIZONTAL = 0;
    private static final int PASS_VERTICAL = 1;

    private Component parent;
    private BufferedImage canvas;
    private int tolerance;
    private int backgroundRGB;
    private int areaRGB;
    private Filler currentFiller;
    private byte [] marks;

    /**
     * To let the Fillers keep track which fill direction is currently employed
     */
    private int pass;

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
        return "Fill";
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

    public void setFiller(Filler filler)
    {
        currentFiller = filler;
    }


    /**
     * User clicked a new location
     */
    @Override
    public void firstClick(Graphics gr, int x, int y)
    {
        final Cursor cursor = parent.getCursor();

        try
        {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            areaRGB = canvas.getRGB(x,y);
            Color paintColor = gr.getColor();

            currentFiller.start(canvas, paintColor);

            pass = PASS_HORIZONTAL;
            marks = new byte [canvas.getWidth() * canvas.getHeight()];
            rangeLeftRight(x, y);

            pass = PASS_VERTICAL;
            marks = new byte [canvas.getWidth() * canvas.getHeight()];
            rangeUpDown(x, y);

            currentFiller.finish(marks);
        }
        finally
        {
            parent.setCursor(cursor);
        }
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
     * @param colorIndex The color's index in the color map.
     * @author Hj. Malthaner
     */
    @Override
    public void onColorSelected(int colorIndex)
    {
    }

    /** 
     * Creates a new instance of Fillmachine
     */
    public Fillmachine(Component parent, BufferedImage canvas, int tolerance, Color background)
    {
        this.parent = parent;
        this.canvas = canvas;
        this.tolerance = tolerance;
        this.backgroundRGB = background.getRGB();
    }


    private boolean isValid(final int x, final int y)
    {
        return x >= 0 && y >= 0 && x < canvas.getWidth() && y < canvas.getHeight();
    }

    private boolean isFillableColor(final int rgb)
    {
        return (tolerance >= 0 && RGB.diff(rgb, areaRGB) <= tolerance) ||
               (tolerance < 0 && rgb != backgroundRGB && (rgb & 0xFF000000) != 0);
    }

    private boolean isMarked(final int x, final int y)
    {
        return marks[canvas.getWidth() * y + x] != 0;
    }

    private void setMark(final int x, final int y)
    {
        marks[canvas.getWidth() * y + x] = 1;
    }


    private void rangeLeftRight(final int x, final int y)
    {
        if(isValid(x, y) && !isMarked(x, y) && isFillableColor(canvas.getRGB(x,y)))
        {
            // Hajo: snoop left
            int xl = x;
            while(isValid(xl, y) &&
                  !isMarked(xl, y) &&
                  isFillableColor(canvas.getRGB(xl,y)))
            {
                setMark(xl, y);
                currentFiller.plotInside(xl, y);
                xl --;
            }
            xl++;
            currentFiller.plotBorder(xl, y);

            // Hajo: snoop right
            int xr = x+1;
            while(isValid(xr, y) &&
                  !isMarked(xr, y) &&
                  isFillableColor(canvas.getRGB(xr,y)))
            {
                setMark(xr, y);
                currentFiller.plotInside(xr, y);
                xr ++;
            }
            xr--;
            currentFiller.plotBorder(xr, y);

            // Hajo: fill line
            currentFiller.plotLine(xl, y, xr, y);

            for(int i=xl; i<=xr; i++)
            {
                rangeLeftRight(i, y+1);
                rangeLeftRight(i, y-1);
            }
        }
    }

    private void rangeUpDown(final int x, final int y)
    {
        if(isValid(x, y) && !isMarked(x, y) && isFillableColor(canvas.getRGB(x,y)))
        {
            // Hajo: snoop up
            int yo = y;
            while(isValid(x, yo) &&
                  !isMarked(x, yo) &&
                  isFillableColor(canvas.getRGB(x,yo)))
            {
                setMark(x, yo);
                currentFiller.plotInside(x, yo);
                yo --;
            }
            yo++;
            currentFiller.plotBorder(x, yo);

            // Hajo: snoop down
            int yu = y+1;
            while(isValid(x, yu) &&
                  !isMarked(x, yu) &&
                  isFillableColor(canvas.getRGB(x,yu)))
            {
                setMark(x, yu);
                currentFiller.plotInside(x, yu);
                yu ++;
            }
            yu--;
            currentFiller.plotBorder(x, yu);

            // Hajo: fill line
            currentFiller.plotLine(x, yo, x, yu);

            for(int i=yo; i<=yu; i++)
            {
                rangeUpDown(x-1, i);
                rangeUpDown(x+1, i);
            }
        }
    }

    /**
     * Operations on the filled area must implemement
     * this interface.
     *
     * @author Hj. Malthaner
     */
    public interface Filler
    {
        public void start(BufferedImage canvas, Color paintColor);
        public void finish(byte [] marks);
        
        public void plotInside(int x, int y);
        public void plotBorder(int x, int y);
        public void plotLine(int x, int y, int xx, int yy);
    }
}
