/*
 * File: IdPool.java
 * Creation: ???
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.tile;

import java.util.TreeSet;

/**
 * Helper class to allocate tile IDs
 * 
 * @author Hj. Malthaner
 */
public class IdPool
{

    private TreeSet <Integer> pool = new TreeSet();

    public void add(int id)
    {
        pool.add(id);
    }

    public int allocateNextId()
    {
        int high = pool.last();
        int next = high + 1;
        add(next);

        return next;
    }

    public boolean contains(int id)
    {
        return pool.contains(id);
    }
}
