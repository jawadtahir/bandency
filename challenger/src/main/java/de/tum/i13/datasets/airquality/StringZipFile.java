package de.tum.i13.datasets.airquality;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class StringZipFile {

    private final File file;

    public StringZipFile(File file) {
        this.file = file;
    }

    public StringZipFileIterator open() throws IOException {


        ZipFile zipFile = new ZipFile(this.file);
        ZipEntry zipEntry = zipFile.entries().nextElement();
        InputStream stream = zipFile.getInputStream(zipEntry);
        InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);

        return new StringZipFileIterator(zipFile, zipEntry, stream, isr, br);
    }
}
