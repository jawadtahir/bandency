package de.tum.i13.datasets.airquality;

import de.tum.i13.bandency.Payload;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.tinylog.Logger;

public class PrepareAirQualityDataset {
    private final Path airqualityDataset;
    private final DateTimeFormatter simplepattern;

    public PrepareAirQualityDataset(Path airqualityDataset) {

        this.airqualityDataset = airqualityDataset;

        this.simplepattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    private ArrayList<AirqualityFile> listSds011Files(final File folder) {
        ArrayList<AirqualityFile> files = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isFile()) {
                if(fileEntry.getName().contains("sds011")) {
                    String[] str = fileEntry.getName().replace(".zip", "").split("_");
                    String[] date = str[0].split("-");
                    AirqualityFile af = new AirqualityFile(Integer.parseInt(date[0]), Integer.parseInt(date[1]), "sds011", fileEntry);
                    files.add(af);
                }
            }
        }

        return files;
    }

    private ArrayList<AirqualityFile> sortedDatasets() {
        ArrayList<AirqualityFile> files = listSds011Files(airqualityDataset.toFile());
        List<AirqualityFile> collect = files.stream()
                .sorted(Comparator.comparing(AirqualityFile::getBeginDate))
                .collect(Collectors.toList());

        return files;
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

    public ArrayList<DateConfig> getDateConfigs() {
        //2016-07 - 2017-01 => 2016-07-12T18:40:11.914121+00
        //2017-02 - 2019-11 => 2017-02-01T00:00:00
        //2019-12 - 2020-04 => 1576800164.320, 1577836978.323 => We skip these ...
        //2020-05 - 2020-11 => 2020-05-01T00:00:00 //Some occational crap like. 2020-090007T20:55:34.00

        ArrayList<DateConfig> dataConfigs = new ArrayList<>();

        dataConfigs.add(new DateConfig(LocalDate.of(2016, 7, 1), LocalDate.of(2017, 1, 1),  DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        dataConfigs.add(new DateConfig(LocalDate.of(2017, 2, 1), LocalDate.of(2020, 12, 1),  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

        return dataConfigs;
    }

    public Optional<DateConfig> getDateConfigForDate(LocalDate date) {
        ArrayList<DateConfig> dateConfigs = getDateConfigs();

        return dateConfigs.stream().filter(dc -> dc.validFor(date)).findFirst();
    }


    private ArrayList<Payload> readBetween(LocalDateTime from, LocalDateTime to) throws IOException {

        ArrayList<AirqualityFile> sortedAirQualityFiles = sortedDatasets();
        LocalDate fromDate = LocalDate.of(from.getYear(), from.getMonth(), 1);

        List<AirqualityFile> dataFiles = sortedAirQualityFiles.stream()
                .filter(a -> a.getBeginDate().isAfter(fromDate) || a.getBeginDate().isEqual(fromDate))
                .sorted(Comparator.comparing(AirqualityFile::getBeginDate))
                .collect(Collectors.toList());


        ArrayList<Payload> payloads = new ArrayList<>();
        for(AirqualityFile df : dataFiles) {
            Optional<DateConfig> dateConfigForDate = getDateConfigForDate(df.getBeginDate());
            if(dateConfigForDate.isEmpty()) {
                Logger.error("no dateconfig for: " + df.getFile().getName());
                continue;
            }

            DateConfig dateParser = dateConfigForDate.get();

            long cnt = 0;
            long errcnt = 0;

            try(ZipFile zipFile = new ZipFile(df.getFile())) {
                ZipEntry zipEntry = zipFile.entries().nextElement();
                try(InputStream stream = zipFile.getInputStream(zipEntry);
                    InputStreamReader isr = new InputStreamReader(stream, StandardCharsets.UTF_8);
                    BufferedReader br = new BufferedReader(isr)) {

                    String firstLine = br.readLine();
                    if(df.getFile().getName().equalsIgnoreCase("2019-12_sds011.zip")) {
                        br.readLine(); //skip garbage
                    }
                    if(correctFormat(firstLine)) {
                        String curr = br.readLine();

                        while(curr != null) {
                            //"sensor_id;sensor_type;location;lat;lon;timestamp;P1;durP1;ratioP1;P2;durP2;ratioP2"
                            //0         ;1          ;2       ;3  ;4  ;5        ;6 ;7    ;8      ;9 ;10   ;11
                            //36251     ;SDS011     ;22290   ;57.706;11.940;1580515303916;0.3;6;;0.3;6;
                            String[] splitted = curr.split(";", -1);
                            if(splitted.length != 12) {
                                curr = br.readLine();
                                continue;
                            }

                            try {
                                LocalDateTime parsed = dateParser.parse(splitted[5]);
/*
                                if ((cnt % 1_000_000) == 0) {
                                    if (splitted.length == 12) {
                                        String formated = parsed.format(simplepattern);
                                        System.out.println("file: " + df.getFile().getName() + " datetimeformat: " + splitted[5] + " parsed: " + formated + " first: " + firstLine + " cnt: " + cnt + " errors: " + errcnt);
                                    } else {
                                        System.out.println("invalid: " + df.getFile().getName() + " length: " + splitted.length + " line: " + curr + " first: " + firstLine + " cnt: " + cnt + " errors: " + errcnt);
                                    }
                                }


 */
                            }catch (Exception ex) {
                                ++errcnt;
                            }
                            ++cnt;
                            curr = br.readLine();

                            /*




                            Timestamp ts = parseDate(splitted[5]);

                            Payload pl = Payload.newBuilder()
                                    .setTimestamp(ts)
                                    .setLatitude(Float.parseFloat(splitted[3]))
                                    .setLongitude(Float.parseFloat(splitted[4]))
                                    .setP1(Float.parseFloat(splitted[6]))
                                    .setP2(Float.parseFloat(splitted[9]))
                                    .build();

                            payloads.add(pl);


                           curr = br.readLine();
                             */
                        }
                    }


                    //System.out.println(firstLine);

                }
                //System.out.println("test");

                System.out.println("finished file: " + df.getFile() + " cnt: " + cnt + " errors: " + errcnt);
            }
        }

        return payloads;
    }

    private boolean correctFormat(String firstLine) {
        String[] split = firstLine.split(";");
        if(split.length == 12) {
            return firstLine.equalsIgnoreCase("sensor_id;sensor_type;location;lat;lon;timestamp;P1;durP1;ratioP1;P2;durP2;ratioP2");
        } else {
            return false;
        }
    }


    public AirqualityDataset prepareDataset() throws IOException {

        //ArrayList<Payload> payloads = readBetween(LocalDateTime.of(2019, 1, 1, 0, 0, 0), LocalDateTime.of(2019, 1, 2, 0, 0, 0));
        ArrayList<Payload> payloads = readBetween(LocalDateTime.of(2015, 1, 1, 0, 0, 0), LocalDateTime.of(2021, 1, 2, 0, 0, 0));
        //ArrayList<Payload> payloads = readBetween(LocalDateTime.of(2019, 12, 1, 0, 0, 0), LocalDateTime.of(2021, 1, 2, 0, 0, 0));
        //ArrayList<Payload> payloads = readBetween(LocalDateTime.of(2020, 1, 1, 0, 0, 0), LocalDateTime.of(2020, 12, 1, 0, 0, 0));


        return new AirqualityDataset();
    }
}
