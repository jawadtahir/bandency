package de.tum.i13.datasets.airquality;

import java.io.File;
import java.time.LocalDate;

public class AirqualityFile {
    private final int year;
    private final int month;
    private final String sensor;
    private final File file;

    public AirqualityFile(int year, int month, String sensor, File file) {

        this.year = year;
        this.month = month;
        this.sensor = sensor;
        this.file = file;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public String getSensor() {
        return sensor;
    }

    public File getFile() {
        return file;
    }

    public LocalDate getBeginDate() {
        return LocalDate.of(this.year, this.month, 1);
    }

    @Override
    public String toString() {
        return "AirqualityFile{" +
                "year=" + year +
                ", month=" + month +
                ", sensor='" + sensor + '\'' +
                ", file=" + file +
                '}';
    }
}
