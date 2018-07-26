package io.pivotal.pal.wehaul.domain;

import javax.persistence.*;

@Entity
@Table
public class Truck {

    @Id
    private String vin;

    @Enumerated(EnumType.STRING)
    @Column
    private TruckStatus status;

    @Column
    private Integer odometerReading;

    @Embedded
    private MakeModel makeModel;

    @Enumerated(EnumType.STRING)
    @Column
    private TruckSize truckSize;

    Truck() {
        // default constructor
    }

    public void returnFromInspection(int odometerReading) {
        if (status != TruckStatus.IN_INSPECTION) {
            throw new IllegalStateException("Truck is not currently in inspection");
        }
        if (this.odometerReading > odometerReading) {
            throw new IllegalArgumentException("Odometer reading cannot be less than previous reading");
        }

        this.status = TruckStatus.RENTABLE;
        this.odometerReading = odometerReading;
    }

    public void reserve() {
        if (status != TruckStatus.RENTABLE) {
            throw new IllegalStateException("Truck cannot be reserved");
        }

        this.status = TruckStatus.RESERVED;
    }

    public void pickUp() {
        if (status != TruckStatus.RESERVED) {
            throw new IllegalStateException("Only reserved trucks can be picked up");
        }

        this.status = TruckStatus.RENTED;
    }

    public void returnToService(int odometerReading) {
        if (status != TruckStatus.RENTED) {
            throw new IllegalStateException("Truck is not currently rented");
        }
        if (this.odometerReading > odometerReading) {
            throw new IllegalArgumentException("Odometer reading cannot be less than previous reading");
        }

        this.status = TruckStatus.RENTABLE;
        this.odometerReading = odometerReading;
    }

    public void sendForInspection() {
        if (status != TruckStatus.RENTABLE) {
            throw new IllegalStateException("Truck cannot be inspected");
        }

        this.status = TruckStatus.IN_INSPECTION;
    }

    public String getVin() {
        return vin;
    }

    public TruckStatus getStatus() {
        return status;
    }

    public Integer getOdometerReading() {
        return odometerReading;
    }

    public MakeModel getMakeModel() {
        return makeModel;
    }

    public TruckSize getTruckSize() {
        return truckSize;
    }

    @Override
    public String toString() {
        return "Truck{" +
                "vin='" + vin + '\'' +
                ", status=" + status +
                ", odometerReading=" + odometerReading +
                ", makeModel=" + makeModel +
                ", truckSize=" + truckSize +
                '}';
    }

    public static class Factory {

        private final TruckInfoLookupClient truckInfoLookupClient;
        private final TruckSizeLookupClient truckSizeLookupClient;

        public Factory(TruckInfoLookupClient truckInfoLookupClient, TruckSizeLookupClient truckSizeLookupClient) {
            this.truckInfoLookupClient = truckInfoLookupClient;
            this.truckSizeLookupClient = truckSizeLookupClient;
        }

        public Truck buyTruck(String vin, int odometerReading) {
            if (odometerReading < 0) {
                throw new IllegalArgumentException("Cannot buy a truck with negative odometer reading");
            }
            Truck truck = new Truck();
            truck.vin = vin;
            truck.status = TruckStatus.IN_INSPECTION;
            truck.odometerReading = odometerReading;
            truck.makeModel = truckInfoLookupClient.getMakeModelByVin(vin);
            truck.truckSize = truckSizeLookupClient.getSizeByMakeModel(truck.getMakeModel());

            return truck;
        }

    }
}
