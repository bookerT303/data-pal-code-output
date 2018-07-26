package io.pivotal.pal.wehaul.fleet.domain;

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
public class FleetTruckTest {

    @Mock
    private TruckInfoLookupClient mockTruckInfoLookupClient;

    private FleetTruck.Factory fleetTruckFactory;

    @Before
    public void setUp() {
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        when(mockTruckInfoLookupClient.getMakeModelByVin(any())).thenReturn(makeModel);
        fleetTruckFactory = new FleetTruck.Factory(mockTruckInfoLookupClient);
    }

    @Test
    public void returnFromInspection() {
        String vin = "test-0001";
        FleetTruck truck = fleetTruckFactory.buyTruck(vin, 100);

        truck.returnFromInspection("notes", 100);

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.INSPECTABLE);
        assertThat(truck.getOdometerReading()).isEqualTo(100);
    }

    @Test
    public void sendForInspection() {
        String vin = "test-0001";
        FleetTruck truck = fleetTruckFactory.buyTruck(vin, 100);
        truck.returnFromInspection("notes", 100);

        truck.sendForInspection();

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.IN_INSPECTION);
    }

    @Test
    public void removeFromYard() {
        String vin = "test-0001";
        FleetTruck truck = fleetTruckFactory.buyTruck(vin, 100);
        truck.returnFromInspection("notes", 100);

        truck.removeFromYard();

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.NOT_INSPECTABLE);
    }

    @Test
    public void returnToYard() {
        String vin = "test-0001";
        FleetTruck truck = fleetTruckFactory.buyTruck(vin, 100);
        truck.returnFromInspection("notes", 100);
        truck.removeFromYard();

        truck.returnToYard(200);

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.INSPECTABLE);
        assertThat(truck.getOdometerReading()).isEqualTo(300);
    }

    @Test
    public void returnFromInspection_whenNotInInspection() {
        String vin = "test-0001";
        FleetTruck truck = fleetTruckFactory.buyTruck(vin, 100);
        truck.returnFromInspection("notes", 100);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.returnFromInspection("notes", 100))
                .withMessage("Truck is not currently in inspection");
    }

    @Test
    public void returnFromInspection_withLowerOdometerReading() {
        String vin = "test-0001";
        FleetTruck truck = fleetTruckFactory.buyTruck(vin, 100);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> truck.returnFromInspection("notes", 99))
                .withMessage("Odometer reading cannot be less than previous reading");
    }

    @Test
    public void sendForInspection_whenAnythingButInspectable() {
        String vin = "test-0001";
        FleetTruck truck = fleetTruckFactory.buyTruck(vin, 100);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.sendForInspection())
                .withMessage("Truck cannot be inspected");
    }

    @Test
    public void removeFromYard_whenAnythingButInspectable() {
        String vin = "test-0001";
        FleetTruck truck = fleetTruckFactory.buyTruck(vin, 100);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.removeFromYard())
                .withMessage("Cannot prevent truck inspection");
    }

    @Test
    public void returnToYard_whenAnythingButNotInspectable() {
        String vin = "test-0001";
        FleetTruck truck = fleetTruckFactory.buyTruck(vin, 100);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.returnToYard(200))
                .withMessage("Cannot allow truck inspection");
    }
}
