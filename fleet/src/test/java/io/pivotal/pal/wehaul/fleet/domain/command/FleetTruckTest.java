package io.pivotal.pal.wehaul.fleet.domain.command;

import io.pivotal.pal.wehaul.fleet.domain.command.event.FleetTruckPurchased;
import io.pivotal.pal.wehaul.fleet.domain.command.event.FleetTruckReturnedFromInspection;
import io.pivotal.pal.wehaul.fleet.domain.command.event.FleetTruckSentForInspection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(MockitoJUnitRunner.class)
public class FleetTruckTest {

    @Test
    public void purchaseTruck() {
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        String vin = "test-0001";

        FleetTruck truck = new FleetTruck(vin, 100, makeModel);

        assertThat(truck.getVin()).isEqualTo(vin);
        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.IN_INSPECTION);
        assertThat(truck.getOdometerReading()).isEqualTo(100);
        assertThat(truck.getMakeModel()).isEqualTo(makeModel);

        FleetTruckPurchased expectedEvent =
                new FleetTruckPurchased(vin, makeModel.getMake(), makeModel.getModel(), 100);
        assertThat(truck.getDomainEvents().get(0)).isEqualToComparingFieldByField(expectedEvent);
    }

    @Test
    public void purchaseTruck_negativeOdometer() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new FleetTruck(
                        "test-0001",
                        -1,
                        new MakeModel("some-make", "some-model")
                ))
                .withMessage("Cannot buy a truck with negative odometer reading");
    }

    @Test
    public void returnFromInspection() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        FleetTruck truck = new FleetTruck(vin, 100, makeModel);

        truck.returnFromInspection("notes", 100);

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.INSPECTABLE);
        assertThat(truck.getOdometerReading()).isEqualTo(100);
        assertThat(truck.getDomainEvents()).hasSize(2);

        FleetTruckReturnedFromInspection expectedEvent = new FleetTruckReturnedFromInspection(
                vin,
                100,
                "notes"
        );
        assertThat(truck.getDomainEvents().get(1)).isEqualToComparingFieldByField(expectedEvent);
    }

    @Test
    public void sendForInspection() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        FleetTruck truck = new FleetTruck(vin, 100, makeModel);
        truck.returnFromInspection("notes", 100);

        truck.sendForInspection();

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.IN_INSPECTION);
        assertThat(truck.getDomainEvents()).hasSize(3);

        FleetTruckSentForInspection event = new FleetTruckSentForInspection(vin);
        assertThat(truck.getDomainEvents().get(2)).isEqualToComparingFieldByField(event);
    }

    @Test
    public void removeFromYard() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        FleetTruck truck = new FleetTruck(vin, 100, makeModel);
        truck.returnFromInspection("notes", 100);

        truck.removeFromYard();

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.NOT_INSPECTABLE);
    }

    @Test
    public void returnToYard() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        FleetTruck truck = new FleetTruck(vin, 100, makeModel);
        truck.returnFromInspection("notes", 100);
        truck.removeFromYard();

        truck.returnToYard(200);

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.INSPECTABLE);
        assertThat(truck.getOdometerReading()).isEqualTo(300);
    }

    @Test
    public void returnFromInspection_whenNotInInspection() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        FleetTruck truck = new FleetTruck(vin, 100, makeModel);
        truck.returnFromInspection("notes", 100);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.returnFromInspection("notes", 100))
                .withMessage("Truck is not currently in inspection");
    }

    @Test
    public void returnFromInspection_withLowerOdometerReading() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        FleetTruck truck = new FleetTruck(vin, 100, makeModel);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> truck.returnFromInspection("notes", 99))
                .withMessage("Odometer reading cannot be less than previous reading");
    }

    @Test
    public void returnFromInspection_whenHighMileageAndBlankInspectionNotes() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        FleetTruck truck = new FleetTruck(vin, 100_000, makeModel);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.returnFromInspection("", 100_100))
                .withMessage("Inspection notes are required on high mileage trucks");
    }

    @Test
    public void sendForInspection_whenAnythingButInspectable() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        FleetTruck truck = new FleetTruck(vin, 100, makeModel);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.sendForInspection())
                .withMessage("Truck cannot be inspected");
    }

    @Test
    public void removeFromYard_whenAnythingButInspectable() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        FleetTruck truck = new FleetTruck(vin, 100, makeModel);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.removeFromYard())
                .withMessage("Cannot prevent truck inspection");
    }

    @Test
    public void returnToYard_whenAnythingButNotInspectable() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        FleetTruck truck = new FleetTruck(vin, 100, makeModel);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.returnToYard(200))
                .withMessage("Cannot allow truck inspection");
    }
}
