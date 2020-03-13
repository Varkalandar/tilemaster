package tilemaster.editor;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 *
 * @author Hj. Malthaner
 */
public class Brush 
{
    public enum Mode {PLAIN, IMAGE};
    
    public Mode mode;
    BufferedImage brush;
    
    public Brush()
    {
        mode = Mode.PLAIN;
    }
   
    void copyFrom(BufferedImage image) 
    {
        brush =  new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics gr = brush.createGraphics();
        gr.drawImage(image, 0, 0, null);
        mode = Mode.IMAGE;
    }
    
    public void draw(Graphics gr, int x, int y) 
    {
        if(mode == Mode.PLAIN)
        {
            gr.fillRect(x, y, 1, 1);
        }
        else
        {
            gr.drawImage(brush, x - brush.getWidth()/2, y - brush.getHeight()/2, null);
        }     
    }
}
