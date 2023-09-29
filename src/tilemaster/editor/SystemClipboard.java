/*
 * File: SystemClipboard.java
 * Creation: 2012/10/03
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * System clipboard access methods.
 * 
 * @author Hj. Malthaner
 */
public class SystemClipboard implements ClipboardOwner
{
    private static final SystemClipboard self = new SystemClipboard();
    
    public static void paste(TilesetEditor editor)
    {
        System.err.println("Paste called.");
        
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        // System.err.println("Got clipboard: " + clipboard);
        
        final Transferable transferable = clipboard.getContents(null);

        System.err.println("Got transferable: " + transferable);

        try 
        {
            if(transferable != null)
            {
                if(transferable.isDataFlavorSupported(DataFlavor.imageFlavor))
                {
                    final Image img = (Image)transferable.getTransferData(DataFlavor.imageFlavor);

                    // System.err.println("Pasting image: " + img);

                    editor.askImportImage(img);
                }
                else
                {
                    System.err.println("No image in clipboard.");
                }
            }
            else
            {
                System.err.println("No content in clipboard.");
            }            
        }
        catch (UnsupportedFlavorException ex) 
        {
            ex.printStackTrace();
        }
        catch (IOException ex) 
        {
            ex.printStackTrace();
        }        
    }
    
    /**
     * This method writes a image to the system clipboard.
     */
    public static void copy(Image image) 
    {
        System.err.println("Copy called: " + image);

        final ImageTransfer imageTransfer = new ImageTransfer(image);
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(imageTransfer, self);
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents)
    {
        System.err.println("SystemClipboard.lostOwnership called: " + contents);
    }
    
    /**
    * This class is used to hold an image while on the clipboard.
    */
    public static class ImageTransfer implements Transferable 
    {
        private final Image image;

        public ImageTransfer(Image image) 
        {
            this.image = image;
        }

        /**
         * @return supported flavors
         */
        @Override
        public DataFlavor[] getTransferDataFlavors() 
        {
            return new DataFlavor[] 
            {
                DataFlavor.imageFlavor
            };
        }

        /**
         * @return true if flavor is supported
         */
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) 
        {
            return DataFlavor.imageFlavor.equals(flavor);
        }

        /**
         * @return the transferred image
         */
        @Override
        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException, IOException 
        {
            if(DataFlavor.imageFlavor.equals(flavor)) 
            {
                return image;
            }
            else
            {
                throw new UnsupportedFlavorException(flavor);
            }
        }
    }    
}
