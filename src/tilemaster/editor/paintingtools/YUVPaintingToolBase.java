/*
 * File: YUVPaintingToolBase.java
 * Creation: 2012/09/11
 *
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */

package tilemaster.editor.paintingtools;

/**
 *
 * @author Hj. Malthaner
 */
public abstract class YUVPaintingToolBase extends PaintingToolBase
{
    protected void rgbToYuv(double [] yuv, int rgb)
    {
        int R = (rgb & 0xFF0000) >> 16;
        int G = (rgb & 0xFF00) >> 8;
        int B = (rgb & 0xFF);
        
        double Y = R *  .299000 + G *  .587000 + B *  .114000;
        double U = R * -.168736 + G * -.331264 + B *  .500000 + 128;
        double V = R *  .500000 + G * -.418688 + B * -.081312 + 128;     
        
        yuv[0] = Y;
        yuv[1] = U;
        yuv[2] = V;
    }
    
    
    protected int yuvToRGB(double [] yuv)
    {
        double Y = yuv[0];
        double U = yuv[1];
        double V = yuv[2];
        
        int R = (int)(Y + 1.4075 * (V - 128));
        int G = (int)(Y - 0.3455 * (U - 128) - (0.7169 * (V - 128)));
        int B = (int)(Y + 1.7790 * (U - 128));
        
        if(R < 0) R=0;
        if(R > 255) R=255;
        if(G < 0) G=0;
        if(G > 255) G=255;
        if(B < 0) B=0;
        if(B > 255) B=255;
        
        return (R << 16) | (G << 8) | B;
    }

    protected void yuvRotate(double[] yuv, double angle, double factor)
    {
        double U = (yuv[1] - 128) * factor;
        double V = (yuv[2] - 128) * factor;

        double cos = Math.cos(angle * Math.PI / 180.0);
        double sin = Math.sin(angle * Math.PI / 180.0);
        
        double u = U * cos - V * sin;
        double v = U * sin + V * cos;
    
        yuv[1] = u + 128;
        yuv[2] = v + 128;
    }
}
