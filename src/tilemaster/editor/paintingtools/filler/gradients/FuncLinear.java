/*
 * File: FuncLinear.java
 * Creation: 2011_11_24
 *
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */
package tilemaster.editor.paintingtools.filler.gradients;

/**
 * A linear range function.
 *
 * @author Hj. Malthaner
 */
public class FuncLinear implements Func
{
    public double calc(final int distance1, final int distance2)
    {
        return (distance1) / (double)(distance1 + distance2 + 1);
    }
}
