package tilemaster.file;

import java.io.File;

/**
 * File wrapper to provide a toString() method to be used in fixed-width lists.
 * 
 * @author Hj. Malthaner
 */
public class FileWrapper
{
    public final File file;

    public FileWrapper(File file)
    {
        this.file = file;
    }
    
    
    @Override
    public boolean equals(Object o)
    {
        if(o != null)
        {
            if(o instanceof File)
            {
                return this.file.equals(o);
            }
            
            if(o instanceof FileWrapper)
            {
                FileWrapper wrap = (FileWrapper)o;
                return this.file.equals(wrap.file);
            }
        }
        
        return false;
    }

    @Override
    public int hashCode()
    {
        return file.hashCode();
    }
    
    @Override
    public String toString()
    {
        String path = file.getPath();
        
        final int length = path.length();
        if(length > 32)
        {
            String left = path.substring(0, 15);
            String right = path.substring(length-15, length);

            path = left + " ... " + right;
        }

        return path;
    }
}
