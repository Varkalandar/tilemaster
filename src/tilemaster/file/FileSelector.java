/*
 * File: FileSelector.java
 * Creation: 29.12.2011
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.file;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;


/**
 * Convenience wrapper for JFileChoosers.
 * 
 * @author Hj. Malthaner
 */
public class FileSelector
{
    private static final Logger LOGGER = Logger.getLogger(FileSelector.class.getName());

    public enum Mode {OPEN, WRITE};

    private final DefaultListModel model = initPaths();


    /**
     * Open a file chooser with the given parameter set.
     * 
     * @param parent Parent component for the file dialog
     * @param title The dialog title
     * @param filterList A list like .gif|.png|.bmp
     * @param filterDescription A description for the filtered file types.
     * @param currentFile The last selected file or directory, accepts both. 
     *                    Pass null for current directory.
     * @param mode Open or save a file
     * @return The selected file or null if no selection was made.
     */
    public static File open(JComponent parent,
                            String title,
                            String filterList,
                            String filterDescription,
                            File currentFile,
                            Mode mode)
    {
        try
        {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            JFileChooser jfc = new JFileChooser();

            try
            {
                if(currentFile == null)
                {
                    jfc.setCurrentDirectory(new File("./"));
                }
                else
                {
                    if(currentFile.isDirectory())
                    {
                        jfc.setCurrentDirectory(currentFile);
                    }
                    else
                    {
                        jfc.setCurrentDirectory(currentFile.getParentFile());
                    }
                }
            }
            catch (Exception ex)
            {
                LOGGER.log(Level.SEVERE, null, ex);
            }

            String filterNames = filterList.replaceAll("\\|", ", ");

            jfc.setDialogTitle(title);
            jfc.setFileFilter(new SimpleFileFilter(filterList,
                                                   filterDescription + " (" + filterNames + ")"));

            parent.setCursor(Cursor.getDefaultCursor());

            int status;
            if(mode == Mode.OPEN)
            {
                status = jfc.showOpenDialog(parent);
            }
            else
            {
                status = jfc.showSaveDialog(parent);
            }

            if(status == JFileChooser.APPROVE_OPTION)
            {
                parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                return jfc.getSelectedFile();
            }

        }  
        catch (HeadlessException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        } 
        finally
        {
            parent.setCursor(Cursor.getDefaultCursor());
        }

        return null;
    }

    
    public DefaultListModel getPathModel()
    {
        return model;
    }
    
    
    /**
     * Open a file chooser with the given parameter set.
     *
     * @param parent Parent component for the file dialog
     * @param title The dialog title
     * @param filterList A list like .gif|.png|.bmp
     * @param filterDescription A description for the filtered file types.
     * @param currentFile The last selected file or directory, accepts both.
     *                    Pass null for current directory.
     * @param mode Open or save a file
     * @return The selected file or null if no selection was made.
     */
    public File selectFile(JComponent parent,
                           String title,
                           String filterList,
                           String filterDescription,
                           File currentFile,
                           Mode mode)
    {
        return selectFile(parent, title, filterList, filterDescription,
                          currentFile, null, mode);
    }

    /**
     * Open a file chooser with the given parameter set.
     *
     * @param parent Parent component for the file dialog
     * @param title The dialog title
     * @param filterList A list like .gif|.png|.bmp
     * @param filterDescription A description for the filtered file types.
     * @param currentFile The last selected file or directory, accepts both.
     *                    Pass null for current directory.
     * @param mode Open or save a file
     * @param selectedFile File preset.
     * @return The selected file or null if no selection was made.
     */
    public File selectFile(JComponent parent,
                           String title,
                           String filterList,
                           String filterDescription,
                           File currentFile,
                           File selectedFile,
                           Mode mode)
    {
        try
        {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            final JFileChooser jfc = new JFileChooser();
            jfc.setPreferredSize(new Dimension(640, 480));
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            
            try
            {
                if(selectedFile != null)
                {
                    jfc.setSelectedFile(selectedFile);
                }
                
                if(currentFile == null)
                {
                    jfc.setCurrentDirectory(new File("./"));
                }
                else
                {
                    if(currentFile.isDirectory())
                    {
                        jfc.setCurrentDirectory(currentFile);
                    }
                    else
                    {
                        jfc.setCurrentDirectory(currentFile.getParentFile());
                    }
                }
            }
            catch (Exception ex)
            {
                LOGGER.log(Level.SEVERE, null, ex);
            }

            String filterNames = filterList.replaceAll("\\|", ", ");

            jfc.setDialogTitle(title);
            jfc.setFileFilter(new SimpleFileFilter(filterList,
                                                   filterDescription + " (" + filterNames + ")"));

            parent.setCursor(Cursor.getDefaultCursor());

            final JList list = new JList(model);
            list.setBorder(new EmptyBorder(0, 4, 0, 0));

            list.addListSelectionListener(new ListSelectionListener()
            {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                    if(e.getValueIsAdjusting() == false)
                    {
                        final Object o = list.getSelectedValue();
                        if(o != null)
                        {
                            if(o instanceof File)
                            {
                                jfc.setCurrentDirectory((File)o);
                            }
                            if(o instanceof FileWrapper)
                            {
                                FileWrapper wrap = (FileWrapper)o;
                                jfc.setCurrentDirectory(wrap.file);
                            }
                            if(o instanceof String)
                            {
                                File cur = jfc.getCurrentDirectory();
                                jfc.setCurrentDirectory(cur.getParentFile());
                            }
                            SwingUtilities.invokeLater(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    list.clearSelection();
                                }
                            });
                        }
                    }
                }
            });

            JScrollPane scrolly = new JScrollPane(list);
            scrolly.setPreferredSize(new Dimension(200, 128));

            jfc.setAccessory(scrolly);

            int status;
            if(mode == Mode.OPEN)
            {
                status = jfc.showOpenDialog(parent);
            }
            else
            {
                status = jfc.showSaveDialog(parent);
            }

            if(status == JFileChooser.APPROVE_OPTION)
            {
                parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                final File file = jfc.getSelectedFile();

                final File entry;
                if(!file.exists())
                {
                    entry = file.getParentFile();
                }
                else if(file.isFile())
                {
                    entry = file.getParentFile();
                }
                else
                {
                    entry = file;
                }

                final FileWrapper wrap = new FileWrapper(entry);
                if(model.contains(wrap))
                {
                    // already in the list, but used again
                    // -> top it
                    model.removeElement(wrap);
                    model.add(1, wrap);
                }
                else
                {                    
                    // Not yet in list -> add it
                    model.add(0, wrap);
                }

                return file;
            }
        }
        catch (HeadlessException ex)
        {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        finally
        {
            parent.setCursor(Cursor.getDefaultCursor());
        }

        return null;
    }

    
    /**
     * Preset path model with the standard system paths.
     * @return A filled path model.
     */
    private static DefaultListModel initPaths()
    {
        DefaultListModel model;
        model = new DefaultListModel();
        model.addElement("One Direcory Up");
        model.addElement(new FileWrapper(new File(new File("").getAbsolutePath())));

        final File [] roots = File.listRoots();
        for(File file : roots)
        {
            model.addElement(new FileWrapper(file));
        }
        return model;
    }

    /**
     * Filters files by extension. If filter string is null, all files will be shown.
     * A filter string like ".gif|.png|.jpg" allows to filter a selection of extensions.
     *
     * @author Hj. Malthaner
     */
    private static class SimpleFileFilter extends FileFilter
    {
        private final String filterString;
        private final String filterDescription;

        @Override
        public boolean accept(File file)
        {
            String ext = null;
            String s = file.getName();
            int i = s.lastIndexOf('.');

            if (i > 0 &&  i < s.length() - 1)
            {
                ext = s.substring(i).toLowerCase();
            }

            return filterString == null ||
                   (ext != null && filterString.contains(ext)) ||
                   file.isDirectory();
        }


        @Override
        public String getDescription()
        {
            return filterDescription;
        }


        /** 
         * Creates a new instance of SimpleFileFilter.
         *
         * @param fs A filter string like ".gif|.png|.jpg" to filter a selection of extensions.
         * @param fd An informal description of the files that will be shown.
         */
        public SimpleFileFilter(String fs, String fd)
        {
            filterString = fs;
            filterDescription = fd;
        }
    }
}
