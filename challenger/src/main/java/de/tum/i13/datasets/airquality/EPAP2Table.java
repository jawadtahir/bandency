package de.tum.i13.datasets.airquality;

public enum EPAP2Table {
    GOOD(0F, 12F), MODERATE(12.1F, 35.4F), SENSITIVE_UNHEALTY(35.5F, 55.4F), UNHEALTY(55.5F, 150.4F), VERY_UNHEALTHY(150.5F, 250.4F), HAZARDS1(250.5F, 350.4F), HAZARDS2(350.5F, 500.4F), HAZARDAS3(500.5F, 99999.9F), FULL(0F, 500.5F);

    private  Float c_low;
    private  Float c_high;

    EPAP2Table(Float c_low, Float c_high){
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
