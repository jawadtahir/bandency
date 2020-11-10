package de.tum.i13;

import org.tinylog.Logger;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {

        try {

            Logger.info("Challenger Service");

            PrepareLocationDataset pld = new PrepareLocationDataset(Path.of("/home/chris/data/challenge"));
            Logger.info("Loading Locationdata");
            LocationDataset ld = pld.loadData();


            Logger.info("Initializing Challenger Service");
            ChallengerServer cs = new ChallengerServer(ld);
        } catch (Exception ex) {
            Logger.error(ex);
        }

        return;
    }
}
