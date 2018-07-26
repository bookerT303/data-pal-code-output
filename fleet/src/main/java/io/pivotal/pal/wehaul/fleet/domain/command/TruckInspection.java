package io.pivotal.pal.wehaul.fleet.domain.command;

import java.util.UUID;

import static org.springframework.util.StringUtils.isEmpty;

public class TruckInspection {

    private UUID id;

    private String truckVin;

    private Integer odometerReading;

    private String notes;

    TruckInspection() {
        // default constructor
    }

    public static TruckInspection createTruckInspection(String truckVin, int odometerReading, String notes) {
        if (odometerReading > 100_000 && isEmpty(notes)) {
            throw new IllegalStateException("Inspection notes are required on high mileage trucks");
        }

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
