package io.pivotal.pal.wehaul.domain;

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
public class TruckTest {

    @Mock
    private TruckInfoLookupClient mockTruckInfoLookupClient;
    @Mock
    private TruckSizeLookupClient mockTruckSizeLookupClient;

    private Truck.Factory truckFactory;

    @Before
    public void setUp() {
        truckFactory = new Truck.Factory(mockTruckInfoLookupClient, mockTruckSizeLookupClient);
        when(mockTruckInfoLookupClient.getMakeModelByVin(any()))
                .thenReturn(new MakeModel("some-make", "some-model"));
        when(mockTruckSizeLookupClient.getSizeByMakeModel(any()))
                .thenReturn(TruckSize.LARGE);
    }

    @Test
    public void returnFromInspection() {
        Truck truck = truckFactory.buyTruck("test-0001", 0);

        int odometerReading = 1;
        truck.returnFromInspection(odometerReading);

        assertThat(truck.getStatus()).isEqualTo(TruckStatus.RENTABLE);
        assertThat(truck.getOdometerReading()).isEqualTo(odometerReading);
    }

    @Test
    public void reserve() {
        Truck truck = truckFactory.buyTruck("test-0001", 0);
        truck.returnFromInspection(1);

        truck.reserve();

        assertThat(truck.getStatus()).isEqualTo(TruckStatus.RESERVED);
    }

    @Test
    public void pickUp() {
        Truck truck = truckFactory.buyTruck("test-0001", 0);
        truck.returnFromInspection(1);
        truck.reserve();

        truck.pickUp();

        assertThat(truck.getStatus()).isEqualTo(TruckStatus.RENTED);
    }

    @Test
    public void returnToService() {
        Truck truck = truckFactory.buyTruck("test-0001", 0);
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
        Truck truck = truckFactory.buyTruck("test-0001", 0);
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
                    truckFactory.buyTruck(vin, odometerReading);
                })
                .withMessage("Cannot buy a truck with negative odometer reading");
    }

    @Test
    public void reserveTruck_whenNotRentable() {
        Truck truck = truckFactory.buyTruck("test-0001", 0);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.reserve())
                .withMessage("Truck cannot be reserved");
    }

    @Test
    public void pickUp_whenNotReserved() {
        Truck truck = truckFactory.buyTruck("test-0001", 0);
        truck.returnFromInspection(1);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.pickUp())
                .withMessage("Only reserved trucks can be picked up");
    }

    @Test
    public void returnToService_withLowerOdometerReading() {
        Truck truck = truckFactory.buyTruck("test-0001", 100);
        truck.returnFromInspection(101);
        truck.reserve();
        truck.pickUp();

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> truck.returnToService(99))
                .withMessage("Odometer reading cannot be less than previous reading");
    }

    @Test
    public void returnToService_whenNotRented() {
        Truck truck = truckFactory.buyTruck("test-0001", 100);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.returnToService(101))
                .withMessage("Truck is not currently rented");
    }

    @Test
    public void sendForInspection_whenNotRentable() {
        Truck truck = truckFactory.buyTruck("test-0001", 0);
        truck.returnFromInspection(1);
        truck.reserve();

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.sendForInspection())
                .withMessage("Truck cannot be inspected");
    }

    @Test
    public void returnFromInspection_whenNotInInspection() {
        Truck truck = truckFactory.buyTruck("test-0001", 0);
        truck.returnFromInspection(1);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.returnFromInspection(100))
                .withMessage("Truck is not currently in inspection");
    }

    @Test
    public void returnFromInspection_withLowerOdometerReading() {
        Truck truck = truckFactory.buyTruck("test-0001", 100);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> truck.returnFromInspection(0))
                .withMessage("Odometer reading cannot be less than previous reading");
    }
}
