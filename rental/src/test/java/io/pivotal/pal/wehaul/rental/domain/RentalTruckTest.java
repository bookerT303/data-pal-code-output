package io.pivotal.pal.wehaul.rental.domain;

import io.pivotal.pal.wehaul.rental.domain.event.RentalTruckDroppedOff;
import io.pivotal.pal.wehaul.rental.domain.event.RentalTruckReserved;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(MockitoJUnitRunner.class)
public class RentalTruckTest {

    @Test
    public void requiredArgsCtor() {
        RentalTruck truck =
                new RentalTruck("test-0001", RentalTruckSize.LARGE);

        assertThat(truck.getVin()).isEqualTo("test-0001");
        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RENTABLE);
        assertThat(truck.getSize()).isEqualTo(RentalTruckSize.LARGE);
    }

    @Test
    public void reserve() {
        RentalTruck truck =
                new RentalTruck("test-0001", RentalTruckSize.LARGE);
        String customerName = "some-customer-name";

        truck.reserve(customerName);

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RESERVED);
        assertThat(truck.getRental()).isNotNull();
        assertThat(truck.getRental().getCustomerName()).isEqualTo(customerName);
        assertThat(truck.getDomainEvents().size()).isEqualTo(1);
        assertThat(truck.getDomainEvents().get(0))
                .isEqualToComparingOnlyGivenFields(new RentalTruckReserved(truck), "vin");
    }

    @Test
    public void pickUp() {
        RentalTruck truck =
                new RentalTruck("test-0001", RentalTruckSize.LARGE);
        String customerName = "some-customer-name";
        truck.reserve(customerName);

        truck.pickUp();

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RENTED);
    }

    @Test
    public void dropOff() {
        RentalTruck truck =
                new RentalTruck("test-0001", RentalTruckSize.LARGE);
        String customerName = "some-customer-name";
        truck.reserve(customerName);
        truck.pickUp();

        int distanceTraveled = 100;
        truck.dropOff(distanceTraveled);

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RENTABLE);
        assertThat(truck.getRental()).isNull();
        assertThat(truck.getDomainEvents().size()).isEqualTo(2);
        assertThat(truck.getDomainEvents().get(1))
                .isEqualToComparingOnlyGivenFields(
                        new RentalTruckDroppedOff(truck, distanceTraveled), "vin", "distanceTraveled"
                );
    }

    @Test
    public void preventRenting() {
        RentalTruck truck =
                new RentalTruck("test-0001", RentalTruckSize.LARGE);

        truck.preventRenting();

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.NOT_RENTABLE);
    }

    @Test
    public void allowRenting() {
        RentalTruck truck =
                new RentalTruck("test-0001", RentalTruckSize.LARGE);

        truck.preventRenting();

        truck.allowRenting();

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RENTABLE);
    }

    @Test
    public void reserve_whenAnythingButRentable() {
        RentalTruck truck =
                new RentalTruck("test-0001", RentalTruckSize.LARGE);
        String customerName = "some-customer-name";
        truck.preventRenting();

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.reserve(customerName))
                .withMessage("Truck cannot be reserved");

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.NOT_RENTABLE);
    }

    @Test
    public void pickUp_whenNotReserved() {
        RentalTruck truck =
                new RentalTruck("test-0001", RentalTruckSize.LARGE);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.pickUp())
                .withMessage("Only reserved trucks can be picked up");

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RENTABLE);
    }

    @Test
    public void dropOff_whenNotPickedUp() {
        RentalTruck truck =
                new RentalTruck("test-0001", RentalTruckSize.LARGE);
        String customerName = "some-customer-name";
        truck.reserve(customerName);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.dropOff(100))
                .withMessage("Only rented trucks can be dropped off");

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RESERVED);
    }

    @Test
    public void preventRenting_whenAnythingButRentable() {
        RentalTruck truck =
                new RentalTruck("test-0001", RentalTruckSize.LARGE);
        String customerName = "some-customer-name";
        truck.reserve(customerName);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.preventRenting())
                .withMessage("Truck cannot be prevented from renting");

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RESERVED);
    }

    @Test
    public void allowRenting_whenAlreadyRentable() {
        RentalTruck truck =
                new RentalTruck("test-0001", RentalTruckSize.LARGE);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.allowRenting())
                .withMessage("Truck is not rentable");

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RENTABLE);
    }
}
