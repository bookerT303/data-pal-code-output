package io.pivotal.pal.wehaul.rental.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class RentalTruckTest {

    @Test
    public void createRentableTruck() {
        RentalTruck truck = RentalTruck.createRentableTruck("test-0001");

        assertThat(truck.getVin()).isEqualTo("test-0001");
        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RENTABLE);
    }

    @Test
    public void reserve() {
        RentalTruck truck = RentalTruck.createRentableTruck("test-0001");

        truck.reserve();

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RESERVED);
    }

    @Test
    public void pickUp() {
        RentalTruck truck = RentalTruck.createRentableTruck("test-0001");
        truck.reserve();

        truck.pickUp();

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RENTED);
    }

    @Test
    public void dropOff() {
        RentalTruck truck = RentalTruck.createRentableTruck("test-0001");
        truck.reserve();
        truck.pickUp();

        truck.dropOff();

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RENTABLE);
    }

    @Test
    public void preventRenting() {
        RentalTruck truck = RentalTruck.createRentableTruck("test-0001");

        truck.preventRenting();

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.NOT_RENTABLE);
    }

    @Test
    public void allowRenting() {
        RentalTruck truck = RentalTruck.createRentableTruck("test-0001");
        truck.preventRenting();

        truck.allowRenting();

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RENTABLE);
    }

    @Test
    public void reserve_whenAnythingButRentable() {
        RentalTruck truck = RentalTruck.createRentableTruck("test-0001");
        truck.preventRenting();

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.reserve())
                .withMessage("Truck cannot be reserved");

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.NOT_RENTABLE);
    }

    @Test
    public void pickUp_whenNotReserved() {
        RentalTruck truck = RentalTruck.createRentableTruck("test-0001");

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.pickUp())
                .withMessage("Only reserved trucks can be picked up");

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RENTABLE);
    }

    @Test
    public void dropOff_whenNotPickedUp() {
        RentalTruck truck = RentalTruck.createRentableTruck("test-0001");
        truck.reserve();

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.dropOff())
                .withMessage("Only rented trucks can be dropped off");

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RESERVED);
    }

    @Test
    public void preventRenting_whenAnythingButRentable() {
        RentalTruck truck = RentalTruck.createRentableTruck("test-0001");
        truck.reserve();

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.preventRenting())
                .withMessage("Truck cannot be prevented from renting");

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RESERVED);
    }

    @Test
    public void allowRenting_whenAlreadyRentable() {
        RentalTruck truck = RentalTruck.createRentableTruck("test-0001");

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.allowRenting())
                .withMessage("Truck is not rentable");

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RENTABLE);
    }
}
