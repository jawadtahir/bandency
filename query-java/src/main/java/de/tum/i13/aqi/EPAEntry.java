package de.tum.i13.aqi;

public class EPAEntry {
    final double C_high;
    final double C_low;
    final double I_high;
    final double I_low;
    final String description;

    public EPAEntry(double c_high, double c_low, double i_high, double i_low, String description) {
        C_high = c_high;
        C_low = c_low;
        I_high = i_high;
        I_low = i_low;
        this.description = description;
    }

    public double getC_high() {
        return C_high;
    }

    public double getC_low() {
        return C_low;
    }

    public double getI_high() {
        return I_high;
    }

    public double getI_low() {
        return I_low;
    }

    public String getDescription() {
        return description;
    }

    public double calc(double C) {
        var fraction = (this.I_high - this.I_low) / (this.C_high - this.C_low);
        return (fraction * (C - this.C_low)) + this.I_low;
    }
}
