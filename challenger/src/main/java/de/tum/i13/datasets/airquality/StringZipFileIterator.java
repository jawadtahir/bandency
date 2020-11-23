package de.tum.i13.datasets.airquality;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class StringZipFileIterator implements Enumeration<String>, Closeable {


    private final ZipFile zipFile;
    private final ZipEntry zipEntry;
    private final InputStream stream;
    private final InputStreamReader isr;
    private final BufferedReader br;

    private boolean firstCall;

    String curr;

    public StringZipFileIterator(ZipFile zipFile, ZipEntry zipEntry, InputStream stream, InputStreamReader isr, BufferedReader br) {

        this.zipFile = zipFile;
        this.zipEntry = zipEntry;
        this.stream = stream;
        this.isr = isr;
        this.br = br;

        this.firstCall = true;
    }

    @Override
    public void close() throws IOException {
        this.br.close();
        this.isr.close();
        this.stream.close();
        this.zipFile.close();
    }

    @Override
    public boolean hasMoreElements() {
        if(firstCall) {
            try {
                curr = this.br.readLine();
            } catch (IOException e) {
            }
            firstCall = false;
        }

        return curr != null;
    }

    @Override
    public String nextElement() {
        if(firstCall) {
            try {
                curr = this.br.readLine();
            } catch (IOException e) {
            }
            firstCall = false;
        }

        String ret = curr;
        try {
            curr = this.br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }
}
