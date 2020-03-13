/*
 * File: SheetIO.java
 * Creation: 2010/06/07
 * 
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 *
 * This file is provided as an example how to write input/output
 * plugins for the Tilemaster tile set editor. 
 */

package tilemaster.io;

import itemizer.editor.ui.SimpleMessageBox;
import itemizer.item.ItemConfiguration;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import tilemaster.tile.TileSet;


/**
 * Tile sheet file IO. Can read and write tile sheet files.
 *
 * @author Hj. Malthaner
 */
public class SheetIO implements FileTypeIO {

    static 
    {
        IOPluginBroker.registerHandler(".sheet", new SheetIO());
    }

    /**
     * Row stride, that is the amount of tiles in one row
     * of the tile sheet.
     */
    private int stride = 8;
    
    /** WxHxN */
    private String raster = "64x64x8"; 
    
    /**
     * Reads images from a tile set file/folder.
     *
     * @author Hj. Malthaner
     */
    @Override
    public TileSet read(String filename) throws IOException
    {
        TileSet result;

        int rasterW = 64;
        int rasterH = 64;

        // Hajo: compatibility check for old sheeet files
        // without sheet header
        
        File catalog = new File(filename);

        BufferedReader reader = new BufferedReader(new FileReader(catalog));
        String line;
        boolean hasHeader = true;
        
        line = reader.readLine();

        if("Tile Catalog".equals(line)) 
        {
            // this must be an old sheet file without header?
            hasHeader = false;
            
            SimpleMessageBox box = new SimpleMessageBox(null,
                    "Tile Raster",
                    "Please enter the tile raster used in this image<br>"
                    + "in format WxHxN (N=tiles per row):",
                    raster);
            
            box.setVisible(true);
            box.dispose();

            String input = box.getInput();
            String [] parts = input.split("x");
            if(parts.length >= 2) 
            {
                raster = input;

                rasterW = Integer.parseInt(parts[0]);
                rasterH = Integer.parseInt(parts[1]);
            }
            
            // old sheet files always had stride 8
            stride = 8;
            
            reader.close();
        }        
        
        reader = new BufferedReader(new FileReader(catalog));
        
        if(hasHeader)
        {
            line = reader.readLine();
            if(!line.equals("Sheet Header Start"))
            {
                throw new IOException("Wrong sheet header start: " + line);
            }
            
            line = reader.readLine();
            if(!line.equals("v.1"))
            {
                throw new IOException("Unsupported sheet version: " + line);
            }

            line = reader.readLine();
            rasterW = Integer.parseInt(line);

            line = reader.readLine();
            rasterH = Integer.parseInt(line);
            
            line = reader.readLine();
            stride = Integer.parseInt(line);

            line = reader.readLine();
            if(!line.equals("Sheet Header End"))
            {
                throw new IOException("Wrong sheet header end: " + line);
            }
        }
            
        result = new TileSet(new ItemConfiguration(), 0);
        result.read(reader); // this call closes the reader.

        BufferedImage sheet = ImageIO.read(new File(makeSheetName(filename)));

        for(int i=0; i<result.size(); i++) 
        {
            int x = (i % stride) * rasterW;
            int y = (i / stride) * rasterH;
            result.get(i).img = crop(sheet.getSubimage(x, y, rasterW, rasterH));
        }

        raster = "" + rasterW + "x" + rasterH + "x" + stride;
        
        return result;
    }


    /**
     * Writes images to a tile set file/folder. Uses raw image data.
     *
     * @author Hj. Malthaner
     */
    @Override
    public void write(String filename, TileSet tileSet) throws IOException
    {
        SimpleMessageBox box = new SimpleMessageBox(null,
                "Tile Raster",
                "Please enter the tile raster to use for this set<br"
                + "in format WxHxN (N=tiles per row):",
                raster);
        
        box.setVisible(true);
        box.dispose();

        String input = box.getInput();
        String [] parts = input.split("x");
        if(parts.length == 3) 
        {		
            raster = input;
		
            final int rasterW = Integer.parseInt(parts[0]);
            final int rasterH = Integer.parseInt(parts[1]);
            stride = Integer.parseInt(parts[2]);

            FileWriter writer = new FileWriter(filename);
            
            writer.write("Sheet Header Start\n");
            writer.write("v.1\n");
            writer.write("" + rasterW + "\n");
            writer.write("" + rasterH + "\n");
            writer.write("" + stride + "\n");
            writer.write("Sheet Header End\n");
            
            tileSet.write(writer);
            writer.close();

            final String format = "PNG";

	    final int imgWidth = rasterW * stride;
	    final int imgHeight = rasterH * ((tileSet.size() + stride - 1) / stride);
	    
            BufferedImage sheet =
                    new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);

            Graphics gr = sheet.getGraphics();
            
            // Hajo: Build tile sheet
            for(int i=0; i<tileSet.size(); i++) 
            {
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
     * Creates a new instance of SheetIO
     */
    private SheetIO()
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
        for(int y=0; y<canvas.getHeight(); y++) 
        {
            for(int x=0; x<canvas.getWidth(); x++) 
            {
                final int argb = canvas.getRGB(x, y);
                final int a = (argb >>> 24) & 255;

                if(a > 127) {
                    if(x > maxX) maxX = x;
                    if(y > maxY) maxY = y;
                }
            }
        }

        final int width = maxX + 1;
        final int height = maxY + 1;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for(int y=0; y<height; y++) 
        {
            for(int x=0; x<width; x++) 
            {
                final int argb = canvas.getRGB(x, y);
                final int a = (argb >>> 24) & 255;

                if(a > 127) 
                {
                    img.setRGB(x, y, argb);
                }
                else
                {
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
