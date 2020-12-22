package de.tum.i13.datasets.airquality;

public enum EPAP1Table {
    GOOD(0F, 54F), MODERATE(54F, 154F), SENSITIVE_UNHEALTY(155F, 254F), UNHEALTY(255F, 254F), VERY_UNHEALTHY(355F, 424F), HAZARDS1(425F, 504F), HAZARDS2(505F, 604F), HAZARDAS3(605F, 99999F), FULL(0F, 605F);

    private  Float c_low;
    private  Float c_high;

    private EPAP1Table(Float c_low, Float c_high){
        this.c_low = c_low;
        this.c_high = c_high;
    }

    public Float getC_low(){
        return this.c_low;
    }

    public Float getC_high(){
        return this.c_high;
    }
}
