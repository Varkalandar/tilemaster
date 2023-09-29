/*
 * File: TileCellRenderer.java
 * Creation: ???
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.editor.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.BevelBorder;
import tilemaster.tile.TileDescriptor;

public class TileCellRenderer extends JLabel implements ListCellRenderer
{


    public TileCellRenderer() {
        setOpaque(true);
        setMinimumSize(new Dimension(32, 32));
        setPreferredSize(new Dimension(64, 64));
        setHorizontalAlignment(CENTER);
        setBorder(new BevelBorder(BevelBorder.LOWERED));
    }

     public Component getListCellRendererComponent(JList list,
                                                   Object value,
                                                   int index,
                                                   boolean isSelected,
                                                   boolean cellHasFocus) {

         if(value instanceof TileDescriptor) {
             TileDescriptor tld = (TileDescriptor)value;

             setText("");
             setIcon(new ImageIcon(tld.img));
         } else {
             setText(value.toString());
             setIcon(null);
         }

         Color background;
         Color foreground;

         // check if this cell represents the current DnD drop location
         JList.DropLocation dropLocation = list.getDropLocation();
         if (dropLocation != null
                 && !dropLocation.isInsert()
                 && dropLocation.getIndex() == index) {

             background = Color.RED;
             foreground = Color.WHITE;

         // check if this cell is selected
         } else if (isSelected) {
             background = Color.GRAY;
             foreground = Color.WHITE;

         // unselected, and not the DnD drop location
         } else {
             background = Color.DARK_GRAY;
             foreground = Color.WHITE;
         };

         setBackground(background);
         setForeground(foreground);

         return this;
     }
 }
