/*
 * File: FontTool.java
 * Creation: 2012/08/24
 *
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */

package tilemaster.editor.paintingtools;

import asktools.Requester;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import tilemaster.editor.TilesetEditor;

/**
 * Paints the first 256 letters of the font on the first
 * 256 tiles of the set.
 *
 * @author Hj. Malthaner
 */
public class FontTool extends PaintingToolBase 
{
    private TilesetEditor editor;

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
        return "Letter Tiles";
    }

    /**
     * Sets the drawing area for tools which need direct access
     *
     * @param image The current tile image, writeable
     */
    @Override
    public void setCanvas(BufferedImage canvas)
    {
        Requester requester = new Requester();
        
        String fontname = requester.askString(editor, "Enter font or font file name:", "");
        int fontsize = requester.askNumber(editor, "Enter font size:", 24);
        int fontstyle = requester.askNumber(editor, "Font style (1=bold, 2=italic):", 0);

        Font font = null;

        if(fontname.endsWith(".ttf"))
        {
            try
            {
                font = Font.createFont(Font.TRUETYPE_FONT, new File(fontname));
                font = font.deriveFont(fontstyle, fontsize);
            }
            catch (FontFormatException ex)
            {
                Logger.getLogger(FontTool.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IOException ex)
            {
                Logger.getLogger(FontTool.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            font = new Font(fontname, fontstyle, fontsize);
        }

        int align = requester.askNumber(editor, "Alignment (1=feft, 2=center):", 1);

        Graphics gr = canvas.getGraphics();
        char [] data = new char[1];
        
        Cursor oldCursor = editor.getCursor();
        editor.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        gr.setFont(font);

        final FontMetrics metrics = gr.getFontMetrics();
        
        // why is +2 needed here? Are there fonts which need more correction?
        final int baseline = metrics.getAscent() + 2;  
        
        final int tileCount = editor.getTileSet().size();
        
        if(align==1)
        {
            for(char letter=0; letter < tileCount; letter ++)
            {
                editor.imageClicked(letter);

                data[0] = letter;
                gr.drawChars(data, 0, 1, 0, baseline);
            }
        }
        else
        {
            int maxWidth = 0;
            for(char letter=0; letter < tileCount; letter ++)
            {
                final int w = metrics.charWidth(letter);
                if(w > maxWidth) maxWidth = w;
            }

            for(char letter=0; letter < tileCount; letter ++)
            {
                editor.imageClicked(letter);
                
                final int w = metrics.charWidth(letter);

                data[0] = letter;
                gr.drawChars(data, 0, 1, (maxWidth-w)/2, baseline);
            }
        }

        editor.imageClicked(0);
        editor.setCursor(oldCursor);
    }

    /**
     * User clicked a new location
     */
    @Override
    public void firstClick(Graphics gr, int x, int y) 
    {
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
     * @param editor The tileset editor which is calling this tool.
     */
    @Override
    public void setEditor(TilesetEditor editor)
    {
        this.editor = editor;
    }

    /** Creates a new instance of CrossTool */
    public FontTool()
    {
    }   
}
