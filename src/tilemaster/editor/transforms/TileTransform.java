/*
 * File: TileTransform.java
 * Creation: ???
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.transforms;

import java.awt.image.BufferedImage;
import tilemaster.editor.TilesetEditor;

/**
 *
 * @author Hj. Malthaner
 */
public interface TileTransform
{

    /**
     * If this transform requires user input, ask for details.
     * @param parent The parent frame.
     * @return true to execute the transform, flase to cancel.
     */
    public boolean askUserData(TilesetEditor parent);

    /**
     * Transform the image in the desired way
     * @param canvas The image to transform.
     * @return The new image
     */
    public BufferedImage transform(BufferedImage canvas);
}
