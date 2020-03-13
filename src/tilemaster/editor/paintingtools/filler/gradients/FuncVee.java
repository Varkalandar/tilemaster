package tilemaster.editor.paintingtools.filler.gradients;

/**
 * A V-shaped range function.
 * 
 * @author Hj. Malthaner
 */
public class FuncVee extends FuncLinear
{
    @Override
    public double calc(int l1, int l2)
    {
        double f = super.calc(l1, l2);
        return (0.5 - Math.abs(0.5 - f)) * 2.0;
    }
}
