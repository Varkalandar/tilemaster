/*
 * File: FileListing.java
 * Creation: 2011_11_29
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class to list files in a directory.
 *
 * @author Hj. Malthaner
 */
public class FileListing
{

    /**
     * List files from a directory which match the given pattern.
     *
     * @param file A directory or a file in the directory to list.
     * @param pattern The pattern which must be matched by the file names.
     * @return A list of matching file names.
     */
    public List <String> listNames(File file, final String pattern)
    {
        if(file.isDirectory() == false)
        {
            file = file.getParentFile();
        }
        if(file.isDirectory() == false)
        {
            throw new IllegalArgumentException("Invalid path: " + file);
        }

        String [] filenames = file.list(new FilenameFilter() {
            public boolean accept(File dir, String name)
            {
                return name.toLowerCase().matches(pattern);
            }
        });

        return Arrays.asList(filenames);
    }

    /**
     * List files from a directory which match the given pattern.
     *
     * @param file A directory or a file in the directory to list.
     * @param pattern The pattern which must be matched by the file names.
     * @return A list of matching file names.
     */
    public List <File> listFiles(File file, final String pattern)
    {
        if(file.isDirectory() == false)
        {
            file = file.getParentFile();
        }
        if(file.isDirectory() == false)
        {
            throw new IllegalArgumentException("Invalid path: " + file);
        }

        File [] files = file.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name)
            {
                return name.toLowerCase().matches(pattern);
            }
        });

        return Arrays.asList(files);
    }

}
