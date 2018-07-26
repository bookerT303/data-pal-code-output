package io.pivotal.pal.wehaul.fleet.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class FleetTruckTest {

    @Test
    public void buyTruck() {
        FleetTruck truck = FleetTruck.buyTruck("test-0001", 100);

        assertThat(truck.getVin()).isEqualTo("test-0001");
        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.IN_INSPECTION);
        assertThat(truck.getOdometerReading()).isEqualTo(100);
    }

    @Test
    public void buyTruck_negativeOdometer() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> FleetTruck.buyTruck("test-0001", -1))
                .withMessage("Cannot buy a truck with negative odometer reading");
    }

    @Test
    public void returnFromInspection() {
        FleetTruck truck = FleetTruck.buyTruck("test-0001", 100);

        truck.returnFromInspection(100);

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.INSPECTABLE);
        assertThat(truck.getOdometerReading()).isEqualTo(100);
    }

    @Test
    public void sendForInspection() {
        FleetTruck truck = FleetTruck.buyTruck("test-0001", 100);
        truck.returnFromInspection(100);

        truck.sendForInspection();

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.IN_INSPECTION);
    }

    @Test
    public void removeFromYard() {
        FleetTruck truck = FleetTruck.buyTruck("test-0001", 100);
        truck.returnFromInspection(100);

        truck.removeFromYard();

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.NOT_INSPECTABLE);
    }

    @Test
    public void returnToYard() {
        FleetTruck truck = FleetTruck.buyTruck("test-0001", 100);
        truck.returnFromInspection(100);
        truck.removeFromYard();

        truck.returnToYard(200);

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.INSPECTABLE);
        assertThat(truck.getOdometerReading()).isEqualTo(300);
    }

    @Test
    public void returnFromInspection_whenNotInInspection() {
        FleetTruck truck = FleetTruck.buyTruck("test-0001", 100);
        truck.returnFromInspection(100);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.returnFromInspection(100))
                .withMessage("Truck is not currently in inspection");
    }

    @Test
    public void returnFromInspection_withLowerOdometerReading() {
        FleetTruck truck = FleetTruck.buyTruck("test-0001", 100);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> truck.returnFromInspection(99))
                .withMessage("Odometer reading cannot be less than previous reading");
    }

    @Test
    public void sendForInspection_whenAnythingButInspectable() {
        FleetTruck truck = FleetTruck.buyTruck("test-0001", 100);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.sendForInspection())
                .withMessage("Truck cannot be inspected");
    }

    @Test
    public void removeFromYard_whenAnythingButInspectable() {
        FleetTruck truck = FleetTruck.buyTruck("test-0001", 100);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.removeFromYard())
                .withMessage("Cannot prevent truck inspection");
    }

    @Test
    public void returnToYard_whenAnythingButNotInspectable() {
        FleetTruck truck = FleetTruck.buyTruck("test-0001", 100);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.returnToYard(200))
                .withMessage("Cannot allow truck inspection");
    }
}
