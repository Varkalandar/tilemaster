/*
 * File: TileDescriptor.java
 * Creation: ???
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.tile;

import itemizer.item.AbstractItem;
import itemizer.item.ItemConfiguration;
import itemizer.item.Triplet;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;

/**
 * Tile metadata container.
 * 
 * @author Hj. Malthaner
 */
public class TileDescriptor
{
    private AbstractItem ait;
    
    public BufferedImage img;
    public int offX, offY;
    public int footX, footY;

    /**
     * Must be unique among a set. Only 0 is allowed
     * as value to use multiple.
     */
    public int tileId;

    public String getString(int index)
    {
        return ait.getString(index);
    }

    public int getInt(int index)
    {
        return ait.getInt(index);
    }

    public TileDescriptor()
    {
        ait = new AbstractItem(null, "nothing");
    }

    public TileDescriptor(ItemConfiguration config, String [] strings, int [] ints)
    {
        ait = new AbstractItem(config, "data", strings, ints, new Triplet[0]);
    }

    public TileDescriptor(ItemConfiguration config, BufferedReader reader) throws IOException
    {
        String line;

        line = reader.readLine();
        if(!"Tile Description".equals(line)) 
        {
            throw new IOException("Missing header: " + line);
        }

        // Version
        String version = reader.readLine();
        if(!"v.1".equals(version) &&
           !"v.2".equals(version) &&
           !"v.3".equals(version) &&
           !"v.4".equals(version) &&
           !"v.5".equals(version)) 
        {
            throw new IOException("Unsupported version: '" + version + "'");
        }

        if("v.3".equals(version) ||
           "v.4".equals(version) || 
           "v.5".equals(version)) 
        {
            line = reader.readLine();
            tileId = Integer.parseInt(line);
        }

        if("v.2".equals(version) ||
           "v.3".equals(version) ||
           "v.4".equals(version) || 
           "v.5".equals(version)) 
        {
            if("v.5".equals(version)) 
            {
                // skip size info
                reader.readLine();
            }
            
            line = reader.readLine();
            String [] parts = line.split(" ");

            offX = Integer.parseInt(parts[0]);
            offY = Integer.parseInt(parts[1]);
            
            footX = 0;
            footY = 0;
            
            if("v.4".equals(version) ||
               "v.5".equals(version))
            {
                line = reader.readLine();
                parts = line.split(" ");

                footX = Integer.parseInt(parts[0]);
                footY = Integer.parseInt(parts[1]);                
            }
        }
        

        if("v.3".equals(version) ||
           "v.4".equals(version) ||
           "v.5".equals(version)) 
        {
            do
            {
                // read additional lines till end of header marker is found
                line = reader.readLine();
                if(line == null) 
                {
                    throw new IOException("Missing 'End Of Header' marker for tile descriptor id=" + tileId);
                }
            } 
            while(!"End Of Header".equals(line));
        }
        
        ait = new AbstractItem(config, reader);
    }

    public void write(Writer writer) throws IOException
    {
        writer.write("Tile Description\n");
        writer.write("v.5\n");
        writer.write("" + tileId + "\n");
        writer.write("" + img.getWidth() + " " + img.getHeight() + "\n");
        writer.write("" + offX + " " + offY + "\n");
        writer.write("" + footX + " " + footY + "\n");
        writer.write("End Of Header\n");

        ait.write(writer);
    }

    public void writeXML(ItemConfiguration tileConfiguration, Writer writer) throws IOException
    {
        writer.write("    <Tile>\n      <Description>\n");
        writer.write("        <version>4</version>\n");
        writer.write("        <id>" + tileId + "</id>\n");
        writer.write("        <width>" + img.getWidth() + "</width>\n        <height>" + img.getHeight() + "</height>\n");
        writer.write("        <offsetX>" + offX + "</offsetX>\n        <offsetY>" + offY + "</offsetY>\n");
        writer.write("        <footX>" + footX + "</footX>\n        <footY>" + footY + "</footY>\n");
        writer.write("      </Description>\n");
        
        writer.write("      <Metadata>\n");

        for(int n=0; n<tileConfiguration.intLabels.length; n++)
        {
            writer.write("        <int>" + ait.getInt(n) + "</int>\n");
        }
        for(int n=0; n<tileConfiguration.stringLabels.length; n++)
        {
            writer.write("        <string>" + ait.getString(n) + "</string>\n");
        }

        writer.write("      </Metadata>\n    </Tile>\n");
    }
}
