package tilemaster.editor.paintingtools.filler.gradients;

/**
 * Color gradient base class with some helper methods.
 * 
 * @author Hj. Malthaner
 */
public abstract class GradientBase implements Gradient
{
    public int red(final int rgb)
    {
        return ((rgb >>> 16) & 0xFF);
    }

    public int green(final int rgb)
    {
        return ((rgb >>> 8) & 0xFF);
    }

    public int blue(final int rgb)
    {
        return (rgb & 0xFF);
    }
}
