package io.pivotal.pal.wehaul.service;

import io.pivotal.pal.wehaul.fleet.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FleetTruckServiceTest {

    @Mock
    private FleetTruckRepository mockFleetTruckRepository;
    @Mock
    private DistanceSinceLastInspectionRepository mockDistanceSinceLastInspectionRepository;
    @Mock
    private TruckInfoLookupClient mockTruckInfoLookupClient;
    @Captor
    private ArgumentCaptor<FleetTruck> truckCaptor;

    private FleetTruckService fleetTruckService;

    @Before
    public void setUp() {
        fleetTruckService = new FleetTruckService(
                mockFleetTruckRepository,
                mockDistanceSinceLastInspectionRepository,
                new FleetTruck.Factory(mockTruckInfoLookupClient));
    }

    @Test
    public void buyTruck() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        fleetTruckService.buyTruck(vin, 1000);

        verify(mockFleetTruckRepository).save(truckCaptor.capture());
        assertThat(truckCaptor.getValue().getVin()).isNotNull();
        assertThat(truckCaptor.getValue().getOdometerReading()).isEqualTo(1000);
        assertThat(truckCaptor.getValue().getMakeModel()).isEqualTo(makeModel);
    }

    @Test
    public void returnFromInspection() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        FleetTruck truck = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck(vin, 0);
        when(mockFleetTruckRepository.findOne(any())).thenReturn(truck);


        fleetTruckService.returnFromInspection(truck.getVin(), "some-notes", 2);


        InOrder inOrder = inOrder(mockFleetTruckRepository);
        inOrder.verify(mockFleetTruckRepository).findOne(truck.getVin());
        inOrder.verify(mockFleetTruckRepository).save(truckCaptor.capture());

        assertThat(truck.getOdometerReading()).isEqualTo(2);

        TruckInspection createdEntry = truckCaptor.getValue().getInspections().get(0);
        assertThat(createdEntry).isNotNull();
        assertThat(createdEntry.getOdometerReading()).isEqualTo(2);
        assertThat(createdEntry.getNotes()).isEqualTo("some-notes");
        assertThat(createdEntry.getTruckVin()).isEqualTo(truck.getVin());
    }

    @Test
    public void sendForInspection() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        FleetTruck truck = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck(vin, 0);
        truck.returnFromInspection("notes", 1);
        when(mockFleetTruckRepository.findOne(any())).thenReturn(truck);


        fleetTruckService.sendForInspection(truck.getVin());


        InOrder inOrder = inOrder(mockFleetTruckRepository);
        inOrder.verify(mockFleetTruckRepository).findOne(truck.getVin());
        inOrder.verify(mockFleetTruckRepository).save(truck);
    }

    @Test
    public void removeFromYard() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        FleetTruck truck = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck(vin, 0);
        truck.returnFromInspection("note", 100);
        when(mockFleetTruckRepository.findOne(any())).thenReturn(truck);

        fleetTruckService.removeFromYard(truck.getVin());

        verify(mockFleetTruckRepository).save(truckCaptor.capture());
        assertThat(truckCaptor.getValue().getStatus()).isEqualTo(FleetTruckStatus.NOT_INSPECTABLE);
    }

    @Test
    public void returnToYard() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        FleetTruck truck = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck(vin, 0);
        truck.returnFromInspection("notes", 100);
        truck.removeFromYard();
        when(mockFleetTruckRepository.findOne(any())).thenReturn(truck);

        fleetTruckService.returnToYard(truck.getVin(), 200);

        verify(mockFleetTruckRepository).save(truckCaptor.capture());
        assertThat(truckCaptor.getValue().getStatus()).isEqualTo(FleetTruckStatus.INSPECTABLE);
        assertThat(truckCaptor.getValue().getOdometerReading()).isEqualTo(300);
    }

    @Test
    public void findAllDistanceSinceLastInspections() {
        fleetTruckService.findAllDistanceSinceLastInspections();

        verify(mockDistanceSinceLastInspectionRepository).findAll();
    }

    @Test
    public void returnFromInspection_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetTruckService.returnFromInspection(truckVin, "some-notes", 5000))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockFleetTruckRepository, times(0)).save(any(FleetTruck.class));
    }

    @Test
    public void sendForInspection_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetTruckService.sendForInspection(truckVin))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockFleetTruckRepository, times(0)).save(any(FleetTruck.class));
    }

    @Test
    public void removeFromYard_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetTruckService.removeFromYard(truckVin))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockFleetTruckRepository, times(0)).save(any(FleetTruck.class));
    }

    @Test
    public void returnToYard_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetTruckService.returnToYard(truckVin, 100))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockFleetTruckRepository, times(0)).save(any(FleetTruck.class));
    }
}
