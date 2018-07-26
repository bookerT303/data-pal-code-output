package io.pivotal.pal.wehaul.rental.domain;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RentalTruckTest {

    @Mock
    private TruckSizeLookupClient mockTruckSizeLookupClient;

    private RentalTruck.Factory rentalTruckFactory;

    @Before
    public void setUp() {
        when(mockTruckSizeLookupClient.getSizeByMakeModel(any(), any())).thenReturn(RentalTruckSize.LARGE);

        rentalTruckFactory = new RentalTruck.Factory(mockTruckSizeLookupClient);
    }

    @Test
    public void reserve() {
        RentalTruck truck =
                rentalTruckFactory.createRentableTruck("test-0001", "some-make", "some-model");
        String customerName = "some-customer-name";

        truck.reserve(customerName);

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RESERVED);
        assertThat(truck.getRental()).isNotNull();
        assertThat(truck.getRental().getCustomerName()).isEqualTo(customerName);
    }

    @Test
    public void pickUp() {
        RentalTruck truck =
                rentalTruckFactory.createRentableTruck("test-0001", "some-make", "some-model");
        String customerName = "some-customer-name";
        truck.reserve(customerName);

        truck.pickUp();

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RENTED);
    }

    @Test
    public void dropOff() {
        RentalTruck truck =
                rentalTruckFactory.createRentableTruck("test-0001", "some-make", "some-model");
        String customerName = "some-customer-name";
        truck.reserve(customerName);
        truck.pickUp();

        truck.dropOff();

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RENTABLE);
        assertThat(truck.getRental()).isNull();
    }

    @Test
    public void preventRenting() {
        RentalTruck truck =
                rentalTruckFactory.createRentableTruck("test-0001", "some-make", "some-model");

        truck.preventRenting();

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.NOT_RENTABLE);
    }

    @Test
    public void allowRenting() {
        RentalTruck truck =
                rentalTruckFactory.createRentableTruck("test-0001", "some-make", "some-model");

        truck.preventRenting();

        truck.allowRenting();

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RENTABLE);
    }

    @Test
    public void reserve_whenAnythingButRentable() {
        RentalTruck truck =
                rentalTruckFactory.createRentableTruck("test-0001", "some-make", "some-model");
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
                rentalTruckFactory.createRentableTruck("test-0001", "some-make", "some-model");

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.pickUp())
                .withMessage("Only reserved trucks can be picked up");

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RENTABLE);
    }

    @Test
    public void dropOff_whenNotPickedUp() {
        RentalTruck truck =
                rentalTruckFactory.createRentableTruck("test-0001", "some-make", "some-model");
        String customerName = "some-customer-name";
        truck.reserve(customerName);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.dropOff())
                .withMessage("Only rented trucks can be dropped off");

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RESERVED);
    }

    @Test
    public void preventRenting_whenAnythingButRentable() {
        RentalTruck truck =
                rentalTruckFactory.createRentableTruck("test-0001", "some-make", "some-model");
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
                rentalTruckFactory.createRentableTruck("test-0001", "some-make", "some-model");

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.allowRenting())
                .withMessage("Truck is not rentable");

        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RENTABLE);
    }
}
