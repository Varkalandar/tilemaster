/*
 * File: TicaIO.java
 * Creation: 2010_05_26
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.io;

import itemizer.item.ItemConfiguration;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import javax.imageio.ImageIO;
import tilemaster.tile.TileDescriptor;
import tilemaster.tile.TileSet;


/**
 * Tile catalog IO. Can read and write tile catalogs.
 *
 * @author Hj. Malthaner
 */
public class TicaIO implements FileTypeIO
{
    public static final String CATALOG_XML = "catalog.xml";
    
    static
    {
        IOPluginBroker.registerHandler(".tica", new TicaIO());
    }

    /**
     * Reads images from a tile set file/folder.
     *
     * @param filename The name of the file to read.
     * @return The created tile set.
     * @throws IOException In case of IO errors.
     */
    @Override
    public TileSet read(String filename) throws IOException
    {
        File catalog = new File(filename);

        BufferedReader reader = new BufferedReader(new FileReader(catalog));
        TileSet result = new TileSet(new ItemConfiguration(), 0);
        result.read(reader);

        int tileCount = result.size();
        for(int i=0; i<tileCount; i++)
        {
            TileDescriptor tld = result.get(i);
            int id = tld.tileId;
            
            String imgname = "" + id + "-" + tld.getString(0) + ".png";
            System.err.println("Loading: " + imgname);

            File file = new File(catalog.getParentFile(), imgname);

            if(file.exists())
            {
                BufferedImage img = ImageIO.read(file);
                tld.img = img;
            }
            else
            {
                // pre-version 5 naming scheme
                imgname = "" + i + "," + id + "," + tld.getString(0) + ".png";
                System.err.println("Loading: " + imgname);
                file = new File(catalog.getParentFile(), imgname);
                
                if(file.exists())
                {
                    BufferedImage img = ImageIO.read(file);
                    tld.img = img;
                }
                else
                {
                    tld.img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                }
            }
        }
        
        /*
        FileListing listing = new FileListing();
        List <File> files = listing.listFiles(catalog, ".*\\.png");

        for(File file : files)
        {
            BufferedImage img = ImageIO.read(file);

            String [] parts = file.getName().split(",");
            String indexString = parts[0];
            int dotPos = indexString.indexOf('.');
            if(dotPos >= 0)
            {
                indexString = indexString.substring(0, dotPos);
            }

            int index = Integer.parseInt(indexString);
            result.get(index).img = img;
        }
        */
        return result;
    }


    /**
     * Writes images to a tile set folder.
     *
     * @param filename The name of the file to read.
     * @param tileSet The tiles to write.
     * @throws IOException In case of IO errors.
     */
    @Override
    public void write(String filename, TileSet tileSet) throws IOException
    {
        File catalogFile = new File(filename);
        File catalogDir = catalogFile.getParentFile();

        Writer writer = new FileWriter(filename);
        tileSet.write(writer);
        writer.close();

        FileOutputStream fos = new FileOutputStream(new File(catalogDir, CATALOG_XML));
        writer = new OutputStreamWriter(fos, "UTF-8");
        tileSet.writeXML(writer);
        writer.close();
        
        FileListing listing = new FileListing();
        List <File> files = listing.listFiles(catalogDir, ".*\\.png");

        // Remove old files
        for(File file : files)
        {
            file.delete();
        }

        final String format = "PNG";

        for(int i=0; i<tileSet.size(); i++)
        {
            final TileDescriptor tld = tileSet.get(i);

            // Hajo: skip empty images
            if(tld.img.getWidth() > 1)
            {
                final String name = tld.getString(0) != null ? tld.getString(0) : "";
                final String pngfile =
                        "" + tld.tileId + "-" + name + ".png";
                ImageIO.write(tld.img, format, new File(catalogDir, pngfile));
            }
            else
            {
                // System.err.println("Skipping empty tile " + i);                
            }
        }
    }


    /** Creates a new instance of TicaIO */
    private TicaIO()
    {
    }
}
