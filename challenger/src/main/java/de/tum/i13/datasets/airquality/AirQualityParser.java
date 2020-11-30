package de.tum.i13.datasets.airquality;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Timestamp;
import de.tum.i13.bandency.Payload;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

public class AirQualityParser implements Enumeration<Payload>, Closeable {

    private final LocalDateTime from;
    private final Timestamp from_ts;
    private final LocalDateTime to;
    private final Timestamp to_ts;
    private final AirqualityFileAccess afa;
    private final Queue<AirqualityFile> dataFiles;
    private long cnt = 0;
    private long errcnt = 0;
    private long parseerror = 0;
    private final long skippedInFile = 0;

    private boolean firstCall;
    private Payload curr;
    private StringZipFileIterator szfi;

    public AirQualityParser(LocalDateTime from, LocalDateTime to, AirqualityFileAccess afa) {
        this.from = from;
        this.from_ts = convert(from);
        this.to = to;
        this.to_ts = convert(to);
        this.afa = afa;

        this.dataFiles = new ArrayDeque<>();
        this.dataFiles.addAll(ensureSetup());

        this.firstCall = true;
    }

    private Timestamp convert(LocalDateTime ldt) {
        return com.google.protobuf.Timestamp.newBuilder().setSeconds(ldt.toEpochSecond(ZoneOffset.UTC)).setNanos(ldt.getNano()).build();
    }

    private int compare(Timestamp t1, Timestamp t2) {
        if(t1.getSeconds() == t2.getSeconds() && t1.getNanos() == t2.getNanos())
            return 0;

        if(t1.getSeconds() < t2.getSeconds()) //well, we only do seconds anyway
            return -1;
        else
            return 1;
    }

    private boolean isHeaderCorrectFormat(String firstLine) {
        String[] split = firstLine.split(";");
        if(split.length == 12) {
            return firstLine.equalsIgnoreCase("sensor_id;sensor_type;location;lat;lon;timestamp;P1;durP1;ratioP1;P2;durP2;ratioP2");
        } else {
            return false;
        }
    }

    private List<AirqualityFile> ensureSetup() {
        List<AirqualityFile> sortedAirQualityFiles = afa.sortedDatasets();
        LocalDate fromDate = LocalDate.of(from.getYear(), from.getMonth(), 1);

        List<AirqualityFile> dataFiles = sortedAirQualityFiles.stream()
                .filter(a -> a.getBeginDate().isAfter(fromDate) || a.getBeginDate().isEqual(fromDate))
                .sorted(Comparator.comparing(AirqualityFile::getBeginDate))
                .collect(Collectors.toList());

        return dataFiles;
    }

    private void setupNewFile() throws IOException {
        if(!this.dataFiles.isEmpty()) {
            if(this.szfi != null) {
                this.szfi.close();
            }
            this.szfi = new StringZipFile(this.dataFiles.peek().getFile()).open();
            String firstLine = this.szfi.nextElement(); //remove heading;
            if (!isHeaderCorrectFormat(firstLine)) {
                throw new IOException("invalid data");
            }

            if (this.dataFiles.peek().getFile().getName().equalsIgnoreCase("2019-12_sds011.zip")) {
                szfi.nextElement(); //skip garbage
            }
        }
    }

    private void parseNext() {
        while (curr == null && szfi != null) {
            String nextElement = "";
            try {
                nextElement = szfi.nextElement();
                if(nextElement == null) {
                    this.dataFiles.poll();
                    setupNewFile();
                    nextElement = szfi.nextElement();
                }

                Payload p = parseFromString(nextElement);
                if(p == null) {
                    ++parseerror;
                    continue;
                }

                if(compare(p.getTimestamp(), from_ts) < 0) {
                    continue;
                }
                if(compare(p.getTimestamp(), to_ts) > 0) {
                    return;
                }

                curr = p;

            } catch (NumberFormatException nfex) {
                //System.out.println("could not parse numbers: " + nextElement);
                ++parseerror;
                continue;
            } catch (Exception ex) {
                ++errcnt;
            }
        }
    }

    private Payload parseFromString(String line) {
        if(line == null)
            return null;

        String[] splitted = line.split(";", -1);
        LocalDateTime parsed = this.dataFiles.peek().getDateConfigForDate().parse(splitted[5]);

        //continue only if fields are not empty
        if(splitted[3].length() == 0 || splitted[4].length() == 0 || splitted[6].length() == 0 || splitted[9].length() == 0)
            return null;

        com.google.protobuf.Timestamp ts = com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(parsed.toEpochSecond(ZoneOffset.UTC))
                .setNanos(parsed.getNano())
                .build();

        Payload pl = Payload.newBuilder()
                .setTimestamp(ts)
                .setLatitude(Float.parseFloat(splitted[3]))
                .setLongitude(Float.parseFloat(splitted[4]))
                .setP1(Float.parseFloat(splitted[6]))
                .setP2(Float.parseFloat(splitted[9]))
                .build();

        //validate that a value is set
        if(pl.getLatitude() == 0.0 || pl.getLongitude() == 0.0 || pl.getP1() == 0.0 || pl.getP2() == 0.0)
            return null;

        return pl;
    }

    @Override
    public boolean hasMoreElements() {
        if(this.firstCall) {
            try {
                setupNewFile();
                parseNext();
                this.firstCall = false;
            } catch (IOException e) {
            }
        }
        if(this.dataFiles.isEmpty())
            return false;

        return curr != null;
    }

    @Override
    public Payload nextElement() {
        if(this.firstCall) {
            try {
                setupNewFile();
                parseNext();
                this.firstCall = false;
            } catch (IOException e) {
            }
        }
        Payload forReturn = curr;
        curr = null;
        parseNext();

        ++cnt;
        return forReturn;
    }

    @Override
    public void close() throws IOException {
        this.szfi.close();
    }
}
