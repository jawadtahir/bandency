package org.debs.gc2023.datasets.disc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class ZipFileIndex {
    private List<File> files;

    public ZipFileIndex(List<File> files) {
        this.files = files;
    }

    public List<FileEntry> getZipFileMap() throws IOException {

        List<FileEntry> zipFileMap = new ArrayList<FileEntry>();

        for(File file : files) {
            try(
                ZipFile zipFile = new ZipFile(file);
            ) {
                var entries = Collections.list(zipFile.entries());
                var csvs = entries.stream()
                    .filter(a -> !a.getName().contains("MACOSX") && a.getName().endsWith(".csv"))
                    .sorted((b,c) -> b.getName().compareTo(c.getName()))
                    .map(a -> a.getName()).toList();
                zipFileMap.add(new FileEntry(file, csvs));
            }
        }

        return zipFileMap;
    }
}
