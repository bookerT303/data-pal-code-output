package io.pivotal.pal.wehaul.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table
public class TruckInspection {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column
    private String truckVin;

    @Column
    private Integer odometerReading;

    @Column
    private String notes;

    TruckInspection() {
        // default constructor
    }

    public static TruckInspection createTruckInspection(String truckVin, int odometerReading, String notes) {
        TruckInspection entry = new TruckInspection();
        entry.id = UUID.randomUUID();
        entry.truckVin = truckVin;
        entry.odometerReading = odometerReading;
        entry.notes = notes;

        return entry;
    }

    public UUID getId() {
        return id;
    }

    public String getTruckVin() {
        return truckVin;
    }

    public Integer getOdometerReading() {
        return odometerReading;
    }

    public String getNotes() {
        return notes;
    }

    @Override
    public String toString() {
        return "TruckInspection{" +
                "id=" + id +
                ", truckVin=" + truckVin +
                ", odometerReading=" + odometerReading +
                ", notes='" + notes + '\'' +
                '}';
    }
}
