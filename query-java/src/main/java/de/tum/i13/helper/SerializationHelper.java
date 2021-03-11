package de.tum.i13.helper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class SerializationHelper {

    public static void writeTooFile(String filename, Object obj) throws IOException {
        String tempfile = filename + ".temp";
        FileOutputStream fout = new FileOutputStream(tempfile);
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        oos.writeObject(obj);

        Files.move(Path.of(tempfile), Path.of(filename), StandardCopyOption.REPLACE_EXISTING);
    }

    public static Object fromFile(String filename) throws IOException, ClassNotFoundException {
        FileInputStream streamIn = new FileInputStream(filename);
        ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
        return objectinputstream.readObject();
    }
}
