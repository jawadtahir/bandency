package de.tum.i13.datasets.airquality;

import de.tum.i13.bandency.Payload;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PrepareAirQualityDataset {
    private final DateTimeFormatter simplepattern;
    private final AirqualityFileAccess afa;

    public PrepareAirQualityDataset(AirqualityFileAccess afa) {
        this.afa = afa;

        this.simplepattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }


    /*
    Datetime parsing errors
    finished file: /home/chris/data/luftdaten/2017-01_sds011.zip cnt: 4322561 errors: 3624689
    finished file: /home/chris/data/luftdaten/2019-12_sds011.zip cnt: 136469165 errors: 5501
    finished file: /home/chris/data/luftdaten/2020-01_sds011.zip cnt: 177764816 errors: 13058
    finished file: /home/chris/data/luftdaten/2020-02_sds011.zip cnt: 170673483 errors: 10874
    finished file: /home/chris/data/luftdaten/2020-03_sds011.zip cnt: 167741646 errors: 3530
    finished file: /home/chris/data/luftdaten/2020-04_sds011.zip cnt: 179651550 errors: 838
    */





    public AirqualityDataset prepareDataset() throws IOException {



        //ArrayList<Payload> payloads = readBetween(LocalDateTime.of(2019, 1, 1, 0, 0, 0), LocalDateTime.of(2019, 1, 2, 0, 0, 0));
        //ArrayList<Payload> payloads = readBetween(LocalDateTime.of(2015, 1, 1, 0, 0, 0), LocalDateTime.of(2021, 1, 2, 0, 0, 0));
        //ArrayList<Payload> payloads = readBetween(LocalDateTime.of(2019, 12, 1, 0, 0, 0), LocalDateTime.of(2021, 1, 2, 0, 0, 0));
        AirQualityParser aqp = new AirQualityParser(LocalDateTime.of(2020, 1, 1, 1, 5, 0), LocalDateTime.of(2020, 1, 1, 1, 6, 0), afa);

        Payload p = null;
        long cnt = 0;
        while(aqp.hasMoreElements()) {
            p = aqp.nextElement();
            ++cnt;
            if((cnt%1_000_000) == 0) {
                System.out.println("cnt: " + cnt + " payload: " + p);
            }
        }
        System.out.println("cnt: " + cnt + " payload: " + p);

        return new AirqualityDataset();
    }
}
