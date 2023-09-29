package tilemaster.editor.paintingtools.filler.gradients;

public class FuncV2 extends FuncLinear
{
    @Override
    public double calc(int l1, int l2)
    {
        double f = super.calc(l1, l2);
        return (0.5 - Math.abs(0.5 - (f*f))) * 2.0;
    }
}
