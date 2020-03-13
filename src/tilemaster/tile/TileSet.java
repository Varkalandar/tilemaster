/*
 * File: TileSet.java
 * Creation: 2010_05_26
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */


package tilemaster.tile;

import itemizer.item.ItemConfiguration;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 *
 * @author Hj. Malthaner
 */
public class TileSet
{
    private final ArrayList <TileDescriptor> tiles;
    private final ItemConfiguration tileConfiguration;

    public int rasterX;
    public int rasterY;

    public ItemConfiguration getTileConfiguration()
    {
        return tileConfiguration;
    }

    public TileDescriptor get(int i)
    {
        return tiles.get(i);
    }

    /**
     * Computes the tile index for the gives id.
     * @return the tile index for the given id or -1 if there is
     * no matching id in the set.
     */
    public int numberFromId(int id)
    {
        for(int i=0; i<tiles.size(); i++)
        {
            if(tiles.get(i).tileId == id) return i;
        }
        
        return -1;
    }
    
    public void add(TileDescriptor tld)
    {
        tiles.add(tld);
    }

    /**
     * Inserts the specified tile at the specified position in this list.
     * Shifts the tile currently at that position (if any) and any
     * subsequent tile to the right (adds one to their indices).
     *
     * @param i Insert position
     * @param tld Tile to insert
     */
    public void insert(int i, TileDescriptor tld)
    {
        tiles.add(i, tld);
    }

    /**
     * Removes the tile at the specified position in this list.
     * Shifts any subsequent elements to the left.
     *
     * @param i Index if tile to remove
     */
    public void remove(int i)
    {
        tiles.remove(i);
    }

    public void set(int i, TileDescriptor tld)
    {
        tiles.set(i, tld);
    }

    public int size()
    {
        return tiles.size();
    }


    public TileDescriptor createTileDescriptor()
    {
        ItemConfiguration config = getTileConfiguration();

        final String [] strings = new String [config.stringLabels.length];
        final int [] ints = new int [config.intLabels.length];

        for(int i=0; i<strings.length; i++) {
            strings[i] = "";
        }

        return new TileDescriptor(config, strings, ints);
    }

    public TileSet(ItemConfiguration tileConfiguration, int size)
    {
        this.tileConfiguration = tileConfiguration;
        tiles = new ArrayList();

        for(int i=0; i<size; i++) {
            add(null);
        }
    }

    public void read(BufferedReader reader) throws IOException
    {
        // Header
        String line = reader.readLine();
        if(!"Tile Catalog".equals(line)) 
        {
            throw new IOException("Wrong header: " + line);
        }

        // Version
        line = reader.readLine();
        if(!"v.1".equals(line) &&
           !"v.2".equals(line)) 
        {
            throw new IOException("Wrong version: " + line);
        }

        if("v.2".equals(line)) {
            line = reader.readLine();
            rasterX = Integer.parseInt(line);

            line = reader.readLine();
            rasterY = Integer.parseInt(line);

            line = reader.readLine();
            if(!"End Of Header".equals(line)) {
                throw new IOException("Wrong header: " + line);
            }
        }
        
        tileConfiguration.read(reader);

        line = reader.readLine();
        final int n = Integer.parseInt(line);

        // tiles = new ArrayList<TileDescriptor>(n);
        tiles.clear();
        
        for(int i=0; i<n; i++) {
            TileDescriptor tld = new TileDescriptor(tileConfiguration, reader);
            tiles.add(tld);
        }

        reader.close();
    }


    public void write(Writer writer) throws IOException
    {
        writer.write("Tile Catalog\n");
        writer.write("v.2\n");
        writer.write("" + rasterX + "\n");
        writer.write("" + rasterY + "\n");
        writer.write("End Of Header\n");
        tileConfiguration.write(writer);

        writer.write("" + tiles.size() + "\n");

        for(int i=0; i<tiles.size(); i++) 
        {
            tiles.get(i).write(writer);
        }
    }

    public void writeXML(Writer writer) throws IOException
    {
        writer.write("<TileCatalog>\n");
        writer.write("  <version>2</version>\n");
        writer.write("  <Header>\n");
        writer.write("    <rasterX>" + rasterX + "</rasterX>\n");
        writer.write("    <rasterY>" + rasterY + "</rasterY>\n");
        writer.write("    <count>" + tiles.size() + "</count>\n");
        writer.write("  </Header>\n  <Configuration>\n");

        for(int n=0; n<tileConfiguration.intLabels.length; n++)
        {
            writer.write("    <intLabel>" + tileConfiguration.intLabels[n] + "</intLabel>\n");
        }
        for(int n=0; n<tileConfiguration.stringLabels.length; n++)
        {
            writer.write("    <stringLabel>" + tileConfiguration.stringLabels[n] + "</stringLabel>\n");
        }


        writer.write("  </Configuration>\n  <Tiles>\n");

        for(int i=0; i<tiles.size(); i++) 
        {
            tiles.get(i).writeXML(tileConfiguration, writer);
        }
        
        writer.write("  </Tiles>\n");
        writer.write("</TileCatalog>\n");
    }

}
