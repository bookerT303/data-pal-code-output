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
public class FleetServiceTest {

    @Mock
    private FleetTruckRepository mockFleetTruckRepository;
    @Mock
    private DistanceSinceLastInspectionRepository mockDistanceSinceLastInspectionRepository;
    @Mock
    private TruckInfoLookupClient mockTruckInfoLookupClient;
    @Captor
    private ArgumentCaptor<FleetTruck> fleetTruckCaptor;

    private FleetService fleetService;

    @Before
    public void setUp() {
        fleetService = new FleetService(
                mockFleetTruckRepository,
                mockDistanceSinceLastInspectionRepository,
                new FleetTruck.Factory(mockTruckInfoLookupClient));
    }

    @Test
    public void buyTruck() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        fleetService.buyTruck(vin, 1000);

        verify(mockFleetTruckRepository).save(fleetTruckCaptor.capture());
        assertThat(fleetTruckCaptor.getValue().getVin()).isNotNull();
        assertThat(fleetTruckCaptor.getValue().getOdometerReading()).isEqualTo(1000);
        assertThat(fleetTruckCaptor.getValue().getMakeModel()).isEqualTo(makeModel);
    }

    @Test
    public void returnTruckFromInspection() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        FleetTruck truck = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck(vin, 0);
        when(mockFleetTruckRepository.findOne(any())).thenReturn(truck);


        fleetService.returnTruckFromInspection(truck.getVin(), "some-notes", 2);


        InOrder inOrder = inOrder(mockFleetTruckRepository);
        inOrder.verify(mockFleetTruckRepository).findOne(truck.getVin());
        inOrder.verify(mockFleetTruckRepository).save(fleetTruckCaptor.capture());

        assertThat(truck.getOdometerReading()).isEqualTo(2);

        TruckInspection createdEntry = fleetTruckCaptor.getValue().getInspections().get(0);
        assertThat(createdEntry).isNotNull();
        assertThat(createdEntry.getOdometerReading()).isEqualTo(2);
        assertThat(createdEntry.getNotes()).isEqualTo("some-notes");
        assertThat(createdEntry.getTruckVin()).isEqualTo(truck.getVin());
    }

    @Test
    public void sendTruckForInspection() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        FleetTruck truck = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck(vin, 0);
        truck.returnFromInspection("notes", 1);
        when(mockFleetTruckRepository.findOne(any())).thenReturn(truck);


        fleetService.sendTruckForInspection(truck.getVin());


        InOrder inOrder = inOrder(mockFleetTruckRepository);
        inOrder.verify(mockFleetTruckRepository).findOne(truck.getVin());
        inOrder.verify(mockFleetTruckRepository).save(truck);
    }

    @Test
    public void removeTruckFromYard() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        FleetTruck truck = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck(vin, 0);
        truck.returnFromInspection("note", 100);
        when(mockFleetTruckRepository.findOne(any())).thenReturn(truck);

        fleetService.removeTruckFromYard(truck.getVin());

        verify(mockFleetTruckRepository).save(fleetTruckCaptor.capture());
        assertThat(fleetTruckCaptor.getValue().getStatus()).isEqualTo(FleetTruckStatus.NOT_INSPECTABLE);
    }

    @Test
    public void returnTruckToYard() {
        String vin = "test-0001";
        MakeModel makeModel = new MakeModel("TestTruckCo", "The Test One");
        when(mockTruckInfoLookupClient.getMakeModelByVin(vin)).thenReturn(makeModel);

        FleetTruck truck = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck(vin, 0);
        truck.returnFromInspection("notes", 100);
        truck.removeFromYard();
        when(mockFleetTruckRepository.findOne(any())).thenReturn(truck);

        fleetService.returnTruckToYard(truck.getVin(), 200);

        verify(mockFleetTruckRepository).save(fleetTruckCaptor.capture());
        assertThat(fleetTruckCaptor.getValue().getStatus()).isEqualTo(FleetTruckStatus.INSPECTABLE);
        assertThat(fleetTruckCaptor.getValue().getOdometerReading()).isEqualTo(300);
    }

    @Test
    public void findAllDistanceSinceLastInspections() {
        fleetService.findAllDistanceSinceLastInspections();

        verify(mockDistanceSinceLastInspectionRepository).findAll();
    }

    @Test
    public void returnTruckFromInspection_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetService.returnTruckFromInspection(truckVin, "some-notes", 5000))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockFleetTruckRepository, times(0)).save(any(FleetTruck.class));
    }

    @Test
    public void sendTruckForInspection_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetService.sendTruckForInspection(truckVin))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockFleetTruckRepository, times(0)).save(any(FleetTruck.class));
    }

    @Test
    public void removeTruckFromYard_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetService.removeTruckFromYard(truckVin))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockFleetTruckRepository, times(0)).save(any(FleetTruck.class));
    }

    @Test
    public void returnTruckToYard_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetService.returnTruckToYard(truckVin, 100))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockFleetTruckRepository, times(0)).save(any(FleetTruck.class));
    }
}
