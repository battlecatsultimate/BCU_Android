package com.mandarin.bcu.io;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class Writer {

    public static boolean check(File f) {
        boolean suc = true;
        if (!f.getParentFile().exists())
            suc &= f.getParentFile().mkdirs();
        if (suc)
            try {
                if (!f.exists())
                    if (f.isDirectory())
                        suc &= f.mkdir();
                    else
                        suc &= f.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
                suc = false;
            }
        return suc;
    }

    public static PrintStream newFile(String str) {
        File f = new File(str);
        check(f);
        PrintStream out = null;
        try {
            out = new PrintStream(f, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

}