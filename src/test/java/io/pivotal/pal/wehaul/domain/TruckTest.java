package io.pivotal.pal.wehaul.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TruckTest {

    @Test
    public void buyTruck() {
        String vin = "test-0001";
        int odometerReading = 0;
        Truck truck = Truck.buyTruck(vin, odometerReading);

        assertThat(truck.getVin()).isNotNull();
        assertThat(truck.getStatus()).isEqualTo(TruckStatus.IN_INSPECTION);
        assertThat(truck.getOdometerReading()).isEqualTo(odometerReading);
    }

    @Test
    public void returnFromInspection() {
        Truck truck = Truck.buyTruck("test-0001", 0);

        int odometerReading = 1;
        truck.returnFromInspection(odometerReading);

        assertThat(truck.getStatus()).isEqualTo(TruckStatus.RENTABLE);
        assertThat(truck.getOdometerReading()).isEqualTo(odometerReading);
    }

    @Test
    public void reserve() {
        Truck truck = Truck.buyTruck("test-0001", 0);
        truck.returnFromInspection(1);

        truck.reserve();

        assertThat(truck.getStatus()).isEqualTo(TruckStatus.RESERVED);
    }

    @Test
    public void pickUp() {
        Truck truck = Truck.buyTruck("test-0001", 0);
        truck.returnFromInspection(1);
        truck.reserve();

        truck.pickUp();

        assertThat(truck.getStatus()).isEqualTo(TruckStatus.RENTED);
    }

    @Test
    public void returnToService() {
        Truck truck = Truck.buyTruck("test-0001", 0);
        truck.returnFromInspection(1);
        truck.reserve();
        truck.pickUp();

        int odometerReading = 101;
        truck.returnToService(odometerReading);

        assertThat(truck.getStatus()).isEqualTo(TruckStatus.RENTABLE);
        assertThat(truck.getOdometerReading()).isEqualTo(odometerReading);
    }

    @Test
    public void sendForInspection() {
        Truck truck = Truck.buyTruck("test-0001", 0);
        truck.returnFromInspection(1);

        truck.sendForInspection();

        assertThat(truck.getStatus()).isEqualTo(TruckStatus.IN_INSPECTION);
    }

    @Test
    public void buyTruck_negativeOdometer() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> {
                String vin = "test-0001";
                int odometerReading = -1;
                Truck.buyTruck(vin, odometerReading);
            })
            .withMessage("Cannot buy a truck with negative odometer reading");
    }

    @Test
    public void reserveTruck_whenNotRentable() {
        Truck truck = Truck.buyTruck("test-0001", 0);

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> truck.reserve())
            .withMessage("Truck cannot be reserved");
    }

    @Test
    public void pickUp_whenNotReserved() {
        Truck truck = Truck.buyTruck("test-0001", 0);
        truck.returnFromInspection(1);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.pickUp())
                .withMessage("Only reserved trucks can be picked up");
    }

    @Test
    public void returnToService_withLowerOdometerReading() {
        Truck truck = Truck.buyTruck("test-0001", 100);
        truck.returnFromInspection(101);
        truck.reserve();
        truck.pickUp();

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> truck.returnToService(99))
            .withMessage("Odometer reading cannot be less than previous reading");
    }

    @Test
    public void returnToService_whenNotRented() {
        Truck truck = Truck.buyTruck("test-0001", 100);

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> truck.returnToService(101))
            .withMessage("Truck is not currently rented");
    }

    @Test
    public void sendForInspection_whenNotRentable() {
        Truck truck = Truck.buyTruck("test-0001", 0);
        truck.returnFromInspection(1);
        truck.reserve();

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> truck.sendForInspection())
            .withMessage("Truck cannot be inspected");
    }

    @Test
    public void returnFromInspection_whenNotInInspection() {
        Truck truck = Truck.buyTruck("test-0001", 0);
        truck.returnFromInspection(1);

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> truck.returnFromInspection(100))
            .withMessage("Truck is not currently in inspection");
    }

    @Test
    public void returnFromInspection_withLowerOdometerReading() {
        Truck truck = Truck.buyTruck("test-0001", 100);

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> truck.returnFromInspection(0))
            .withMessage("Odometer reading cannot be less than previous reading");
    }
}
