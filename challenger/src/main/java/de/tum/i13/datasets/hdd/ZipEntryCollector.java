package de.tum.i13.datasets.hdd;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.postgresql.core.SetupQueryRunner;
import org.tinylog.Logger;

import com.google.protobuf.Timestamp;

import de.tum.i13.bandency.DriveState;

public class ZipEntryCollector {

    private final BatchedCollector bl;
    private final BufferedReader br;

    private DateTimeFormatter dateTimeParser = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // 2022-10-01
    private final HashMap<String, Timestamp> dateCache;

    public ZipEntryCollector(BatchedCollector bl, BufferedReader br) {
        this.bl = bl;
        this.br = br;
        this.dateCache = new HashMap<>();
    }

    public boolean collect() throws Exception {
        // Lets do the parsing stuff here
        // date,serial_number,model,capacity_bytes,failure,smart_1_normalized,smart_1_raw,

        // attempt to reduce time and gcc
        var indexToSmart = new HashMap<Integer, Integer>(200, 0.98f);

        var normalized = new HashMap<Integer, Long>(200, 0.98f);
        var raw = new HashMap<Integer, Long>(200, 0.98f);
        var models = new HashSet<String>();

        var first = true;
        var cnt = 0;
        String line = br.readLine();
        while(line != null) {
            if(cnt % 10_000 == 0) {
                Logger.info("Imported " + cnt + " lines");
            }
            
            if(first) {
                first = false;
                String[] parts = line.split(",", -1);
                if(parts.length < 150) {
                    throw new Exception("Invalid header line: " + line);
                }

                for(int i = 0; i < parts.length; i++) {
                    var part = parts[i];
                    if(part.startsWith("smart")) {
                        String [] cols = part.split("_", -1);
                        if (cols.length != 3) {
                            throw new Exception("Invalid column name: " + part);
                        }

                        if(cols[2].equals("normalized")) {
                            indexToSmart.put(i, Integer.parseInt(cols[1]));
                        }
                        else if(cols[2].equals("raw")) {
                            indexToSmart.put(i, -Integer.parseInt(cols[1]));
                        }
                        else {
                            throw new Exception("Invalid column name: " + part);
                        }
                    }
                }
            } else {
                var ds = parseLine(line, indexToSmart, normalized, raw, models);
                if(ds != null) {
                    if (!bl.collectState(ds, models)) {
                        return false;
                    }
                } 
                normalized.clear();
                raw.clear();
            }

            line = br.readLine();
            ++cnt;
        }

        return true;
    }

    // date,serial_number,model,capacity_bytes,failure,smart_1_normalized,smart_1_raw,smart_2_normalized,smart_2_raw,smart_3_normalized,smart_3_raw,smart_4_normalized,smart_4_raw,smart_5_normalized,smart_5_raw,smart_7_normalized,smart_7_raw,smart_8_normalized,smart_8_raw,smart_9_normalized,smart_9_raw,smart_10_normalized,smart_10_raw,smart_11_normalized,smart_11_raw,smart_12_normalized,smart_12_raw,smart_13_normalized,smart_13_raw,smart_15_normalized,smart_15_raw,smart_16_normalized,smart_16_raw,smart_17_normalized,smart_17_raw,smart_18_normalized,smart_18_raw,smart_22_normalized,smart_22_raw,smart_23_normalized,smart_23_raw,smart_24_normalized,smart_24_raw,smart_160_normalized,smart_160_raw,smart_161_normalized,smart_161_raw,smart_163_normalized,smart_163_raw,smart_164_normalized,smart_164_raw,smart_165_normalized,smart_165_raw,smart_166_normalized,smart_166_raw,smart_167_normalized,smart_167_raw,smart_168_normalized,smart_168_raw,smart_169_normalized,smart_169_raw,smart_170_normalized,smart_170_raw,smart_171_normalized,smart_171_raw,smart_172_normalized,smart_172_raw,smart_173_normalized,smart_173_raw,smart_174_normalized,smart_174_raw,smart_175_normalized,smart_175_raw,smart_176_normalized,smart_176_raw,smart_177_normalized,smart_177_raw,smart_178_normalized,smart_178_raw,smart_179_normalized,smart_179_raw,smart_180_normalized,smart_180_raw,smart_181_normalized,smart_181_raw,smart_182_normalized,smart_182_raw,smart_183_normalized,smart_183_raw,smart_184_normalized,smart_184_raw,smart_187_normalized,smart_187_raw,smart_188_normalized,smart_188_raw,smart_189_normalized,smart_189_raw,smart_190_normalized,smart_190_raw,smart_191_normalized,smart_191_raw,smart_192_normalized,smart_192_raw,smart_193_normalized,smart_193_raw,smart_194_normalized,smart_194_raw,smart_195_normalized,smart_195_raw,smart_196_normalized,smart_196_raw,smart_197_normalized,smart_197_raw,smart_198_normalized,smart_198_raw,smart_199_normalized,smart_199_raw,smart_200_normalized,smart_200_raw,smart_201_normalized,smart_201_raw,smart_202_normalized,smart_202_raw,smart_206_normalized,smart_206_raw,smart_210_normalized,smart_210_raw,smart_218_normalized,smart_218_raw,smart_220_normalized,smart_220_raw,smart_222_normalized,smart_222_raw,smart_223_normalized,smart_223_raw,smart_224_normalized,smart_224_raw,smart_225_normalized,smart_225_raw,smart_226_normalized,smart_226_raw,smart_230_normalized,smart_230_raw,smart_231_normalized,smart_231_raw,smart_232_normalized,smart_232_raw,smart_233_normalized,smart_233_raw,smart_234_normalized,smart_234_raw,smart_235_normalized,smart_235_raw,smart_240_normalized,smart_240_raw,smart_241_normalized,smart_241_raw,smart_242_normalized,smart_242_raw,smart_244_normalized,smart_244_raw,smart_245_normalized,smart_245_raw,smart_246_normalized,smart_246_raw,smart_247_normalized,smart_247_raw,smart_248_normalized,smart_248_raw,smart_250_normalized,smart_250_raw,smart_251_normalized,smart_251_raw,smart_252_normalized,smart_252_raw,smart_254_normalized,smart_254_raw,smart_255_normalized,smart_255_raw
    private DriveState.Builder parseLine(String line, final HashMap<Integer, Integer> indexToSmart, HashMap<Integer, Long> normalized, HashMap<Integer, Long> raw, HashSet<String> models) throws Exception {
        String[] parts = line.split(",", -1);
        if(parts.length == 0) {
            return null;
        }

        if(parts.length < 150) {
            throw new Exception("Invalid line - " + parts.length + ": " + line);
        }

        DriveState.Builder ds = DriveState.newBuilder();
        // date, serial_number,model,capacity_bytes,failure,
        ds.setDate(parseDateTime(parts[0]));
        ds.setSerialNumber(parts[1]);

        var model = parts[2];
        if(!models.contains(model)) {
            models.add(model);
        }
        ds.setModel(model);
        ds.setCapacityBytes(Long.parseLong(parts[3]));
        ds.setFailure(Integer.parseInt(parts[4]));

        var sbNormalized = new StringBuilder();
        var sbRaw = new StringBuilder();
        Long smartvalue = -1l;

        for(int index = 5; index < parts.length; ++index) {
            var smartnumber = indexToSmart.get(index);

            smartvalue = -1l;
            if (!parts[index].isEmpty()) {
                smartvalue = Long.parseLong(parts[index]);
            }

            // skip empty values
            if(smartvalue == -1l) {
                continue;
            }

            if(smartnumber > 0) {
                //normalized.put(Integer.valueOf(smartnumber.intValue()), smartvalue);
                //ds.putNormalized(smartnumber, smartvalue);
                //ds.getNormalizedMap().put(smartnumber, smartvalue)
                sbNormalized.append(smartnumber);
                sbNormalized.append(':');
                sbNormalized.append(smartvalue);
                sbNormalized.append(',');
            }
            else {
                //raw.put(Integer.valueOf(-(smartnumber.intValue())), smartvalue);
                //ds.putRaw(-smartnumber, smartvalue);
                sbNormalized.append(-smartnumber);
                sbNormalized.append(':');
                sbNormalized.append(smartvalue);
                sbNormalized.append(',');
            }
        }

        ds.setNormalized(sbNormalized.toString());
        ds.setRaw(sbRaw.toString());

        //ds.putAllNormalized(normalized);
        //ds.putAllRaw(raw);

        // leaving this for now, but we need either a generic mapping for normalized/raw values or we need to type all this.

        return ds;
    }

    // Not cloning, is bad practice but we never modify it
    private Timestamp parseDateTime(String dateString) {
        if(dateCache.containsKey(dateString)) {
            return dateCache.get(dateString);
        }

        var dateTime = LocalDate.from(dateTimeParser.parse(dateString)).atStartOfDay(ZoneOffset.UTC);
        com.google.protobuf.Timestamp ts = com.google.protobuf.Timestamp.newBuilder()
                .setSeconds(dateTime.toEpochSecond())
                .setNanos(dateTime.getNano())
                .build();
        dateCache.put(dateString, ts);

        return ts;        
    }
}