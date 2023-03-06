package de.tum.i13.datasets.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class Utils {
    public static ArrayList<File> getFiles(File directory) {
        ArrayList<File> files = new ArrayList<>();
        if (directory.isDirectory()) {
            for(File f : directory.listFiles()) {
                if(f.isFile() && f.getName().endsWith(".zip")) {
                    files.add(f);
                }
            }
        }
        Collections.sort(files);
        return files;
    }
}
