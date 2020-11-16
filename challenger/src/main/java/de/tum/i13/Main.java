package de.tum.i13;

import de.tum.i13.bandency.Batch;
import de.tum.i13.bandency.Payload;
import de.tum.i13.datasets.airquality.AirqualityDataset;
import de.tum.i13.datasets.airquality.AirqualityFileAccess;
import de.tum.i13.datasets.airquality.AirqualityToBatch;
import de.tum.i13.datasets.airquality.PrepareAirQualityDataset;
import org.tinylog.Logger;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static de.tum.i13.Util.convertPBTimestamp;

public class Main {

    public static void main(String[] args) {

        try {

            Logger.info("Challenger Service");

            LocalDateTime ld = LocalDateTime.ofEpochSecond(1580515200L, 0, ZoneOffset.UTC);
            System.out.println(ld);

            /*
            PrepareLocationDataset pld = new PrepareLocationDataset(Path.of("/home/chris/data/challenge"));
            Logger.info("Loading Locationdata");
            LocationDataset ld = pld.loadData();
             */

            Logger.info("Loading AirQualityDataset");
            AirqualityFileAccess afa = new AirqualityFileAccess(Path.of("/home/chris/data/luftdaten"));
            //PrepareAirQualityDataset paqd = new PrepareAirQualityDataset(afa);
            //AirqualityDataset airqualityDataset = paqd.prepareDataset();

            AirqualityToBatch atb = new AirqualityToBatch(5000, LocalDateTime.of(2020, 1, 1, 0, 0, 0), LocalDateTime.of(2020, 3, 5, 1, 0, 0), afa);

            Batch b = null;
            long cnt = 0;
            while(atb.hasMoreElements()) {
                b = atb.nextElement();
                ++cnt;
                if((cnt % 20_000) == 0) {
                    System.out.println("cnt: " + cnt + " curr_size: " + b.getCurrentCount() + " last: " + b.getLastyearCount());
                    if(b.getCurrentCount() > 0 && b.getLastyearCount() > 0) {
                        Payload firstCurrent = b.getCurrentList().get(0);
                        Payload fistLastYear = b.getLastyearList().get(0);

                        System.out.println("cnt: " + cnt + " curr: " + convertPBTimestamp(firstCurrent.getTimestamp()) + " last: " + convertPBTimestamp(fistLastYear.getTimestamp()));
                    }
                }
            }

            System.out.println("cnt: " + cnt + " curr_size: " + b.getCurrentCount() + " last: " + b.getLastyearCount());





            //AirqualityToBatch atb = new AirqualityToBatch()

            /*
            Logger.info("Initializing Challenger Service");
            ChallengerServer cs = new ChallengerServer(ld);


            Logger.info("Initializing Service");
            Server server = ServerBuilder
                    .forPort(8081)
                    .addService(cs)
                    .build();

            server.start();
            Logger.info("Serving");
            server.awaitTermination();
             */
        } catch (Exception ex) {
            Logger.error(ex);
        }

        return;
    }
}
