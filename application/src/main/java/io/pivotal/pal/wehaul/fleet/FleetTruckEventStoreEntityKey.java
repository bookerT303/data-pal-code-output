package io.pivotal.pal.wehaul.fleet;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FleetTruckEventStoreEntityKey implements Serializable {

    @Column
    private String vin;

    @Column
    private Integer version;

    public FleetTruckEventStoreEntityKey(String vin, Integer version) {
        this.vin = vin;
        this.version = version;
    }

    FleetTruckEventStoreEntityKey() {
        // default constructor for JPA
    }

    public String getVin() {
        return vin;
    }

    public Integer getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FleetTruckEventStoreEntityKey that = (FleetTruckEventStoreEntityKey) o;
        return Objects.equals(vin, that.vin) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vin, version);
    }

    @Override
    public String toString() {
        return "FleetTruckEventStoreEntityKey{" +
                "vin=" + vin +
                ", version=" + version +
                '}';
    }
}
