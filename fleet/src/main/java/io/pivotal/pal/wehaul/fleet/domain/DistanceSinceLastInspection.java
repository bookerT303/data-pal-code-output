package io.pivotal.pal.wehaul.fleet.domain;

public class DistanceSinceLastInspection {

    private String truckVin;
    private Integer lastInspectionDistance;

    public DistanceSinceLastInspection(String truckVin, Integer lastInspectionDistance) {
        this.truckVin = truckVin;
        this.lastInspectionDistance = lastInspectionDistance;
    }

    DistanceSinceLastInspection() {
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
        return "DistanceSinceLastInspection{" +
                "truckVin=" + truckVin +
                ", lastInspectionDistance=" + lastInspectionDistance +
                '}';
    }
}
