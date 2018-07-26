package io.pivotal.pal.wehaul.fleet.domain;

import io.pivotal.pal.wehaul.fleet.domain.event.FleetTruckPurchased;
import io.pivotal.pal.wehaul.fleet.domain.event.FleetTruckReturnedFromInspection;
import io.pivotal.pal.wehaul.fleet.domain.event.FleetTruckSentForInspection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FleetTruckTest {

    @Mock
    private TruckInfoLookupClient mockTruckInfoLookupClient;

    @Test
    public void buyTruck() {
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        String vin = "test-0001";
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        FleetTruck truck = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck(vin, 100);

        assertThat(truck.getVin()).isEqualTo(vin);
        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.IN_INSPECTION);
        assertThat(truck.getOdometerReading()).isEqualTo(100);
        assertThat(truck.getMakeModel()).isEqualTo(makeModel);
        assertThat(truck.getDomainEvents().get(0))
                .isEqualToComparingOnlyGivenFields(new FleetTruckPurchased(truck), "vin", "make", "model");
    }

    @Test
    public void buyTruck_negativeOdometer() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new FleetTruck.Factory(mockTruckInfoLookupClient)
                        .buyTruck("test-0001", -1))
                .withMessage("Cannot buy a truck with negative odometer reading");
    }

    @Test
    public void returnFromInspection() {
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        String vin = "test-0001";
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        FleetTruck truck = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck(vin, 100);

        truck.returnFromInspection("notes", 100);

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.INSPECTABLE);
        assertThat(truck.getOdometerReading()).isEqualTo(100);
        assertThat(truck.getDomainEvents().size()).isEqualTo(2);
        assertThat(truck.getDomainEvents().get(1))
                .isEqualToComparingOnlyGivenFields(new FleetTruckReturnedFromInspection(truck), "vin");
    }

    @Test
    public void sendForInspection() {
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        String vin = "test-0001";
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        FleetTruck truck = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck(vin, 100);
        truck.returnFromInspection("notes", 100);

        truck.sendForInspection();

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.IN_INSPECTION);
        assertThat(truck.getDomainEvents().size()).isEqualTo(3);
        assertThat(truck.getDomainEvents().get(2))
                .isEqualToComparingOnlyGivenFields(new FleetTruckSentForInspection(truck), "vin");
    }

    @Test
    public void removeFromYard() {
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        String vin = "test-0001";
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        FleetTruck truck = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck(vin, 100);
        truck.returnFromInspection("notes", 100);

        truck.removeFromYard();

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.NOT_INSPECTABLE);
    }

    @Test
    public void returnToYard() {
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        String vin = "test-0001";
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        FleetTruck truck = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck(vin, 100);
        truck.returnFromInspection("notes", 100);
        truck.removeFromYard();

        truck.returnToYard(200);

        assertThat(truck.getStatus()).isEqualTo(FleetTruckStatus.INSPECTABLE);
        assertThat(truck.getOdometerReading()).isEqualTo(300);
    }

    @Test
    public void returnFromInspection_whenNotInInspection() {
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        String vin = "test-0001";
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        FleetTruck truck = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck(vin, 100);
        truck.returnFromInspection("notes", 100);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.returnFromInspection("notes", 100))
                .withMessage("Truck is not currently in inspection");
    }

    @Test
    public void returnFromInspection_withLowerOdometerReading() {
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        String vin = "test-0001";
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        FleetTruck truck = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck(vin, 100);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> truck.returnFromInspection("notes", 99))
                .withMessage("Odometer reading cannot be less than previous reading");
    }

    @Test
    public void returnFromInspection_whenHighMileageAndBlankInspectionNotes() {
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        String vin = "test-0001";
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        FleetTruck truck = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck(vin, 100_000);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.returnFromInspection("", 100_100))
                .withMessage("Inspection notes are required on high mileage trucks");
    }

    @Test
    public void sendForInspection_whenAnythingButInspectable() {
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        String vin = "test-0001";
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        FleetTruck truck = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck(vin, 100);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.sendForInspection())
                .withMessage("Truck cannot be inspected");
    }

    @Test
    public void removeFromYard_whenAnythingButInspectable() {
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        String vin = "test-0001";
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        FleetTruck truck = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck(vin, 100);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.removeFromYard())
                .withMessage("Cannot prevent truck inspection");
    }

    @Test
    public void returnToYard_whenAnythingButNotInspectable() {
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        String vin = "test-0001";
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        FleetTruck truck = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck(vin, 100);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> truck.returnToYard(200))
                .withMessage("Cannot allow truck inspection");
    }
}
