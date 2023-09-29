/*
 * File: ExampleSheetIO.java
 * Creation: 2010_06_07
 * 
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 */

package tilemaster.io;

import itemizer.editor.ui.SimpleMessageBox;
import itemizer.item.ItemConfiguration;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.imageio.ImageIO;
import tilemaster.tile.TileSet;


/**
 * Tile sheet file IO. Can read and write tile sheet files.
 *
 * @author Hj. Malthaner
 */
public class ExampleSheetIO implements FileTypeIO {

    static {
        IOPluginBroker.registerHandler(".sheet", new ExampleSheetIO());
    }

    /**
     * Row stride, that is the amount of tiles in one row
     * of the tile sheet.
     */
    final int stride = 8;
    
    /**
     * Reads images from a tile set file/folder.
     *
     * @author Hj. Malthaner
     */
    @Override
    public TileSet read(String filename) throws IOException
    {
        TileSet result = null;

        SimpleMessageBox box = new SimpleMessageBox(null,
                                                    "Tile Raster",
                                                    "Please enter the tile raster used in this image:",
                                                    "64x64");
        box.setVisible(true);
        box.dispose();

        String input = box.getInput();
        String [] parts = input.split("x");
        if(parts.length == 2) {
            final int rasterW = Integer.parseInt(parts[0]);
            final int rasterH = Integer.parseInt(parts[1]);

            File catalog = new File(filename);

            BufferedReader reader = new BufferedReader(new FileReader(catalog));
            result = new TileSet(new ItemConfiguration(), 0);
            result.read(reader);

            BufferedImage sheet = ImageIO.read(new File(makeSheetName(filename)));

            for(int i=0; i<result.size(); i++) {
                int x = (i % stride) * rasterW;
                int y = (i / stride) * rasterH;
                result.get(i).img = crop(sheet.getSubimage(x, y, rasterW, rasterH));
            }
        }
        
        return result;
    }


    /**
     * Writes images to a PNG file.
     *
     * @author Hj. Malthaner
     */
    @Override
    public void write(String filename, TileSet tileSet) throws IOException
    {
        SimpleMessageBox box = new SimpleMessageBox(null,
                                                    "Tile Raster",
                                                    "Please enter the tile raster used in this image:",
                                                    "64x64");
        box.setVisible(true);
        box.dispose();

        String input = box.getInput();
        String [] parts = input.split("x");
        if(parts.length == 2) {
            final int rasterW = Integer.parseInt(parts[0]);
            final int rasterH = Integer.parseInt(parts[1]);

            FileWriter writer = new FileWriter(filename);
            tileSet.write(writer);
            writer.close();
            
            final String format = "PNG";

            BufferedImage sheet =
                    new BufferedImage(rasterW * stride,
                                      rasterH * (tileSet.size() + stride - 1) / stride,
                                      BufferedImage.TYPE_INT_ARGB);

            Graphics gr = sheet.getGraphics();
            
            // Hajo: Build tile sheet
            for(int i=0; i<tileSet.size(); i++) {
                int x = (i % stride) * rasterW;
                int y = (i / stride) * rasterH;

                gr.drawImage(tileSet.get(i).img, x, y, null);
            }


            // Hajo: Save tile sheet
            ImageIO.write(sheet,
                          format,
                          new File(makeSheetName(filename)));

        }
    }


    /** 
     * Creates a new instance of ExampleSheetIO
     */
    private ExampleSheetIO()
    {
    }


    /**
     * Semi-crop (right, bottom) real image from a larger image with
     * empty borders.
     *
     * @param canvas The orginal image
     * @return the semi-cropped image
     */
    private BufferedImage crop(final BufferedImage canvas)
    {
        int maxX = 0;
        int maxY = 0;

        // Hajo: Scan for bounds
        for(int y=0; y<canvas.getHeight(); y++) {
            for(int x=0; x<canvas.getWidth(); x++) {
                final int argb = canvas.getRGB(x, y);
                final int a = (argb >>> 24) & 255;

                if(a > 127) {
                    if(x > maxX)
                    {
                        maxX = x;
                    }
                    if(y > maxY)
                    {
                        maxY = y;
                    }
                }
            }
        }

        final int width = maxX + 1;
        final int height = maxY + 1;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for(int y=0; y<height; y++) {
            for(int x=0; x<width; x++) {
                final int argb = canvas.getRGB(x, y);
                final int a = (argb >>> 24) & 255;

                if(a > 127) {
                    img.setRGB(x, y, argb);
                } else {
                    img.setRGB(x, y, 0);
                }
            }
        }

        return img;
    }

    /**
     * Creates the name of the tile sheet image file.
     * 
     * @param filename The catalog file name.
     * @return The tile sheet image file name.
     */
    private String makeSheetName(String filename)
    {
        return filename.substring(0, filename.lastIndexOf('.')) + ".png";
    }
}
