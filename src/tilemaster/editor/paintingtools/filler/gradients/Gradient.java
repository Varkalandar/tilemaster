package tilemaster.editor.paintingtools.filler.gradients;

/**
 * Interface for classes which can calculate gradients in RGB space.
 * 
 * @author Hj. Malthaner
 */
public interface Gradient
{
    /**
     * Calculate a RGB gradient from rgb1 to rgb2.
     *
     * @param rgb1 First RGB value.
     * @param rgb2 Second RGB value.
     * @param f Must be in range [0..1]
     * @return Calculated RGB value.
     */
    public int calcRGB(int rgb1, int rgb2, double f);
}
