package io.pivotal.pal.wehaul.fleet.domain;

public class TruckSinceInspection {

    private String truckVin;
    private Integer lastInspectionDistance;

    public TruckSinceInspection(String truckVin, Integer lastInspectionDistance) {
        this.truckVin = truckVin;
        this.lastInspectionDistance = lastInspectionDistance;
    }

    TruckSinceInspection() {
        // default constructor
    }

    public String getTruckVin() {
        return truckVin;
    }

    public Integer getLastInspectionDistance() {
        return lastInspectionDistance;
    }

    @Override
    public String toString() {
        return "TruckSinceInspection{" +
                "truckVin=" + truckVin +
                ", lastInspectionDistance=" + lastInspectionDistance +
                '}';
    }
}
