package de.tum.i13.aqi;

import de.tum.i13.query.P;

import java.util.ArrayList;
import java.util.HashMap;

public class AQICalc {

    private final ArrayList<EPAEntry> p1Table;
    private final ArrayList<EPAEntry> p2Table;

    private final HashMap<P, ArrayList<EPAEntry>> hm;

    public AQICalc() {
        this.p1Table = new ArrayList<>();

        this.p1Table.add(new EPAEntry(54.0, 0.0, 50.0, 0.0, "good"));
        this.p1Table.add(new EPAEntry(154.0, 55.0, 100.0, 51.0, "moderate"));
        this.p1Table.add(new EPAEntry(254.0, 155.0, 150.0, 101.0, "unhealthy for sensitive groups"));
        this.p1Table.add(new EPAEntry(354.0, 255.0, 200.0, 151.0, "unhealthy"));
        this.p1Table.add(new EPAEntry(424.0, 355.0, 300.0, 201.0, "very unhealthy"));
        this.p1Table.add(new EPAEntry(504.0, 425.0, 400.0, 301.0, "hazardous"));
        this.p1Table.add(new EPAEntry(604.0, 505.0, 500.0, 401.0, "hazardous"));
        this.p1Table.add(new EPAEntry(99999.0, 605.0, 999.0, 501.0, "hazardous"));

        this.p2Table = new ArrayList<>();
        this.p2Table.add(new EPAEntry(12.0, 0.0, 50.0, 0.0, "good"));
        this.p2Table.add(new EPAEntry(35.4, 12.1, 100.0, 51.0, "moderate"));
        this.p2Table.add(new EPAEntry(55.4, 35.5, 150.0, 101.0, "unhealthy for sensitive groups"));
        this.p2Table.add(new EPAEntry(150.4, 55.5, 200.0, 151.0, "unhealthy"));
        this.p2Table.add(new EPAEntry(250.4, 150.5, 300.0, 201.0, "very unhealthy"));
        this.p2Table.add(new EPAEntry(350.4, 250.5, 400.0, 301.0, "hazardous"));
        this.p2Table.add(new EPAEntry(500.4, 350.5, 500.0, 401.0, "hazardous"));
        this.p2Table.add(new EPAEntry(99999.9, 500.5, 999.0, 501.0, "hazardous"));

        hm = new HashMap<>();
        hm.put(P.P1, this.p1Table);
        hm.put(P.P25, this.p2Table);
    }


    public EPAEntry getEPAEntry(double v, P p1) {
        double rounded = Math.round(v);
        if(p1 == P.P25) {
            v*=10.0;
            rounded = Math.round(v);
            rounded /= 10.0;
        }

        for(EPAEntry entry : hm.get(p1)) {
            if(rounded <= entry.C_high && rounded >= entry.C_low) {
                return entry;
            }
        }
        return null;
    }

    public double calculate(double v, P p) {
        return getEPAEntry(v, p).calc(v);
    }
}
