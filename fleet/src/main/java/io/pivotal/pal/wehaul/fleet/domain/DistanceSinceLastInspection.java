package io.pivotal.pal.wehaul.fleet.domain;

public class DistanceSinceLastInspection {

    private final String truckVin;
    private final Integer distanceSinceLastInspection;

    public DistanceSinceLastInspection(String truckVin,
                                       Integer distanceSinceLastInspection) {
        this.truckVin = truckVin;
        this.distanceSinceLastInspection = distanceSinceLastInspection;
    }

    public String getTruckVin() {
        return truckVin;
    }

    public Integer getDistanceSinceLastInspection() {
        return distanceSinceLastInspection;
    }

    @Override
    public String toString() {
        return "DistanceSinceLastInspection{" +
                "truckVin=" + truckVin +
                ", distanceSinceLastInspection=" + distanceSinceLastInspection +
                '}';
    }
}
