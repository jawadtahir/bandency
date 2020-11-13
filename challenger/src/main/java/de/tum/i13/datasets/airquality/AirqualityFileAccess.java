package de.tum.i13.datasets.airquality;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AirqualityFileAccess {
    private final Path airqualityDataset;

    public AirqualityFileAccess(Path airqualityDataset) {
        this.airqualityDataset = airqualityDataset;
    }

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

    public DateConfig getDateConfigForDate(LocalDate date) {
        ArrayList<DateConfig> dateConfigs = getDateConfigs();
        return dateConfigs.stream().filter(dc -> dc.validFor(date)).findFirst().get();
    }

    private ArrayList<AirqualityFile> listSds011Files(final File folder) {
        ArrayList<AirqualityFile> files = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isFile()) {
                if(fileEntry.getName().contains("sds011")) {
                    String[] str = fileEntry.getName().replace(".zip", "").split("_");
                    String[] date = str[0].split("-");
                    int year = Integer.parseInt(date[0]);
                    int month = Integer.parseInt(date[1]);

                    LocalDate ld = LocalDate.of(year, month, 1);

                    AirqualityFile af = new AirqualityFile(year, month, "sds011", fileEntry, getDateConfigForDate(ld));
                    files.add(af);
                }
            }
        }

        return files;
    }

    public List<AirqualityFile> sortedDatasets() {
        ArrayList<AirqualityFile> files = listSds011Files(airqualityDataset.toFile());
        List<AirqualityFile> collect = files.stream()
                .sorted(Comparator.comparing(AirqualityFile::getBeginDate))
                .collect(Collectors.toList());

        return collect;
    }

}
