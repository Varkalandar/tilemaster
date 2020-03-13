/*
 * File: ImageSelectorInterface.java
 * Creation: 2010_05_25
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.ui;

/**
 * Interface for classes that want to react on color selections.
 * @author Hj. Malthaner
 */
public interface ImageSelectorInterface {

    /**
     * Called if an image is selected in the ImageSelector
     * 
     * @author Hj. Malthaner
     */
    public void imageClicked(int imageNumber);
}
