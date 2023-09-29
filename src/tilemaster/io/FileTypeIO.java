/*
 * File: FileTypeIO.java
 * Creation: 2010_05_26
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.io;

import java.io.IOException;
import tilemaster.tile.TileSet;

/**
 *
 * @author Hj. Malthaner
 */
public interface FileTypeIO {

    /**
     * Reads images from a tile set file/folder.
     *
     * @author Hj. Malthaner
     */
    public TileSet read(String filename) throws IOException;

    /**
     * Writes images to a tile set file/folder. Uses raw image data.
     *
     * @author Hj. Malthaner
     */
    public void write(String filename, TileSet tileSet) throws IOException;


}
