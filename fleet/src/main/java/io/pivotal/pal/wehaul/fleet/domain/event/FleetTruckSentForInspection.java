package io.pivotal.pal.wehaul.fleet.domain.event;

import io.pivotal.pal.wehaul.fleet.domain.FleetTruck;

import java.time.LocalDateTime;
import java.util.Objects;

public class FleetTruckSentForInspection {

    private final String vin;
    private final LocalDateTime createdDate;

    public FleetTruckSentForInspection(FleetTruck truck) {
        this.vin = truck.getVin();
        this.createdDate = LocalDateTime.now();
    }

    public String getVin() {
        return vin;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FleetTruckSentForInspection that = (FleetTruckSentForInspection) o;
        return Objects.equals(vin, that.vin) &&
                Objects.equals(createdDate, that.createdDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vin, createdDate);
    }

    @Override
    public String toString() {
        return "FleetTruckSentForInspection{" +
                "vin='" + vin + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}
