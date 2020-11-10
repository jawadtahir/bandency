package de.tum.i13;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {

        PrepareLocationDataset pld = new PrepareLocationDataset(Path.of("/home/chris/data/challenge"));
        LocationDataset ld = pld.loadData();


        ChallengerServer cs = new ChallengerServer();



        return;
    }
}
