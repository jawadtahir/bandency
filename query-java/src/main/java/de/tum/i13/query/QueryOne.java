package de.tum.i13.query;

import com.google.protobuf.Timestamp;
import de.tum.i13.Batch;
import de.tum.i13.Measurement;
import de.tum.i13.TopKCities;
import de.tum.i13.aqi.AQICalc;
import de.tum.i13.helper.TimestampHelper;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static de.tum.i13.helper.TimestampHelper.addTimeunit;
import static de.tum.i13.helper.TimestampHelper.isSmaller;

public class QueryOne {
    private final LocationLookup ll;
    private final AQICalc aqicalc;

    private Optional<Timestamp> nextCurrSnapshot;
    private Optional<Timestamp> nextLastSnapshot;

    private Timestamp lastEventCurr;

    private HashMap<String, MeanSlidingWindow> currentYear;
    private HashMap<String, MeanSlidingWindow> lastYear;

    private HashMap<String, FiveDaysAQISlidingWindow> avgCurrentYear;
    private HashMap<String, FiveDaysAQISlidingWindow> avgLastYear;

    private HashMap<String, MeanSlidingWindow> measuresForYear(Y y) {
        switch (y) {
            case LAST -> {
                return lastYear;
            }
            case CURRENT -> {
                return currentYear;
            }
        }
        return null;
    }

    private HashMap<String, FiveDaysAQISlidingWindow> avgMeasuresForYear(Y y) {
        switch (y) {
            case LAST -> {
                return avgLastYear;
            }
            case CURRENT -> {
                return avgCurrentYear;
            }
        }
        return null;
    }

    public QueryOne(LocationLookup ll) {
        this.ll = ll;
        this.aqicalc = new AQICalc();
        this.currentYear = new HashMap<>();
        this.lastYear = new HashMap<>();

        this.avgCurrentYear = new HashMap<>();
        this.avgLastYear = new HashMap<>();

        this.nextCurrSnapshot = Optional.empty();
        this.nextLastSnapshot = Optional.empty();

        this.lastEventCurr = null;
    }

    private int toRetAqi(double aqi) {
        return (int)Math.round(aqi*1000);
    }

    public Pair<Timestamp, ArrayList<TopKCities>> calculateTopKImproved(Batch batch) {

        //working throught the events
        Timestamp newLatest = processEvents(batch);
        if(newLatest != null) {
            lastEventCurr = newLatest;
        }

        //calculate tresholds
        Timestamp cityActiveTreshold = addTimeunit(lastEventCurr, TimeUnit.MINUTES, -15);
        Timestamp lastEventLast = addTimeunit(lastEventCurr, TimeUnit.DAYS, -365);

        //PriorityQueue<TopKCities> topKCities = new PriorityQueue<>(Comparator.comparingInt(TopKCities::getAverageAQIImprovement).reversed());
        ArrayList<TopKCities> topKCities = new ArrayList<>();

        for(String city : this.currentYear.keySet()) {
            MeanSlidingWindow current = this.currentYear.get(city);
            if(current.isActive(cityActiveTreshold) && this.lastYear.containsKey(city)) {
                MeanSlidingWindow last = this.lastYear.get(city);

                //for the current AQI needed
                current.resize(addTimeunit(lastEventCurr, TimeUnit.DAYS, -1));

                //resizing for the AQI improvement 5D
                if(this.avgCurrentYear.containsKey(city) && this.avgLastYear.containsKey(city)) {
                    FiveDaysAQISlidingWindow currentFiveDays = this.avgCurrentYear.get(city);
                    currentFiveDays.resize(addTimeunit(lastEventCurr, TimeUnit.DAYS, -5));

                    FiveDaysAQISlidingWindow lastFiveDays = this.avgLastYear.get(city);
                    lastFiveDays.resize(addTimeunit(lastEventCurr, TimeUnit.DAYS, -365 + -5));

                    if(currentFiveDays.size() > 0 && lastFiveDays.size() > 0) {
                        TopKCities topk = TopKCities.newBuilder()
                                .setCity(city)
                                .setCurrentAQIP1(toRetAqi(aqicalc.calculate(current.getMeanP1(), P.P1)))
                                .setCurrentAQIP2(toRetAqi(aqicalc.calculate(current.getMeanP2(), P.P25)))
                                .setAverageAQIImprovement(toRetAqi(lastFiveDays.getMean() - currentFiveDays.getMean()))
                                .build();
                        topKCities.add(topk);
                    }
                }
            }
        }

        topKCities.sort(Comparator.comparingInt(TopKCities::getAverageAQIImprovement).reversed());

        ArrayList<TopKCities> firstFifty = new ArrayList<>();
        int cnt = 1;
        for(TopKCities tk : topKCities) {
            if(cnt > 50)
                break;
            firstFifty.add(TopKCities.newBuilder(tk)
                    .setPosition(cnt).build());

            ++cnt;
        }

        return Pair.of(newLatest, firstFifty);
    }

    private Timestamp processEvents(Batch batch) {
        Timestamp highest = null;

        for(Measurement curr : batch.getCurrentList()) {
            if(highest == null) {
                highest = curr.getTimestamp();
            }
            if(!isSmaller(highest, curr.getTimestamp())) {
                highest = curr.getTimestamp();
            }

            //check if we have to do a 5 min snapshot
            if(nextCurrSnapshot.isEmpty()) {
                var nextCurr = TimestampHelper.nextSnapshot(curr.getTimestamp());
                nextCurrSnapshot = Optional.of(nextCurr);
                nextLastSnapshot = Optional.of(addTimeunit(nextCurr, TimeUnit.DAYS, -365));
            }

            while(isSmaller(nextCurrSnapshot.get(), curr.getTimestamp())) {
                //Make the snapshot, resize according to the nextSnapshot
                snapshotAQI(Y.CURRENT, nextCurrSnapshot.get());
                nextCurrSnapshot = Optional.of(TimestampHelper.nextSnapshot(nextCurrSnapshot.get()));
            }

            String location = this.ll.lookupLocation(curr.getLongitude(), curr.getLatitude());
            if(location != null) {
                currentYear.putIfAbsent(location, new MeanSlidingWindow());
                MeanSlidingWindow msw = currentYear.get(location);
                msw.addMesurement(curr);
            }
        }

        for(Measurement last : batch.getLastyearList()) {
            if(highest == null) {
                highest = last.getTimestamp();
            }

            Timestamp relativeHighest = addTimeunit(last.getTimestamp(), TimeUnit.DAYS, 365);
            if(!isSmaller(highest, relativeHighest)) {
                highest = relativeHighest;
            }

            while(isSmaller(nextLastSnapshot.get(), last.getTimestamp())) {
                //Make the snapshot, resize according to the nextSnapshot
                snapshotAQI(Y.LAST, nextLastSnapshot.get());
                nextLastSnapshot = Optional.of(TimestampHelper.nextSnapshot(nextLastSnapshot.get()));
            }

            String location = this.ll.lookupLocation(last.getLongitude(), last.getLatitude());
            if(location != null) {
                lastYear.putIfAbsent(location, new MeanSlidingWindow());
                MeanSlidingWindow msw = lastYear.get(location);
                msw.addMesurement(last);
            }
        }

        return highest;
    }

    private void snapshotAQI(Y year, Timestamp snapshotTs) {
        HashMap<String, MeanSlidingWindow> swl = measuresForYear(year);
        var resizeTimestamp = addTimeunit(snapshotTs, TimeUnit.DAYS, -1);
        for(String location : swl.keySet()) {
            MeanSlidingWindow msw = swl.get(location);
            msw.resize(resizeTimestamp);
            if(msw.hasElements()) {
                var mean_p1 = this.aqicalc.calculate(msw.getMeanP1(), P.P1);
                var mean_p2 = this.aqicalc.calculate(msw.getMeanP2(), P.P25);

                var avg = Math.max(mean_p1, mean_p2);
                avgMeasuresForYear(year).putIfAbsent(location, new FiveDaysAQISlidingWindow());
                FiveDaysAQISlidingWindow fdasw = avgMeasuresForYear(year).get(location);
                fdasw.addMeasurement(snapshotTs, avg);
            }
        }
    }
}
