package org.makumba.commons;

import java.util.Vector;

public class SingletonReleaser {
    
    private static Vector<SingletonHolder> holders = new Vector<SingletonHolder>();
    
    public static void register(SingletonHolder holder) {
        holders.add(holder);
    }
    
    public static void releaseAll() {
        for(SingletonHolder holder : holders) {
            holder.release();
        }
    }

}
