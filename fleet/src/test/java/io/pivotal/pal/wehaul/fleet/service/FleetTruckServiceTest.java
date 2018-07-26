package io.pivotal.pal.wehaul.fleet.service;

import io.pivotal.pal.wehaul.fleet.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@DirtiesContext
@RunWith(MockitoJUnitRunner.class)
public class FleetTruckServiceTest {

    @Mock
    private FleetTruckRepository mockTruckRepository;
    @Mock
    private TruckInspectionRepository mockTruckInspectionRepository;
    @Mock
    private TruckSinceInspectionRepository mockTruckSinceInspectionRepository;
    @Captor
    private ArgumentCaptor<FleetTruck> truckCaptor;
    @Captor
    private ArgumentCaptor<TruckInspection> truckInspectionCaptor;

    private FleetTruckService fleetTruckService;

    @Before
    public void setUp() {
        fleetTruckService = new FleetTruckService(
            mockTruckRepository,
            mockTruckInspectionRepository,
            mockTruckSinceInspectionRepository
        );
    }

    @Test
    public void buyTruck() {
        fleetTruckService.buyTruck("test-0001", 1000);

        verify(mockTruckRepository).save(truckCaptor.capture());
        assertThat(truckCaptor.getValue().getVin()).isNotNull();
        assertThat(truckCaptor.getValue().getOdometerReading()).isEqualTo(1000);
    }

    @Test
    public void returnFromInspection() {
        FleetTruck truck = FleetTruck.buyTruck("test-0001", 0);
        when(mockTruckRepository.findOne(any())).thenReturn(truck);


        fleetTruckService.returnFromInspection(truck.getVin(), "some-notes", 2);


        InOrder inOrder = inOrder(mockTruckRepository, mockTruckInspectionRepository);
        inOrder.verify(mockTruckRepository).findOne(truck.getVin());
        inOrder.verify(mockTruckRepository).save(truck);

        assertThat(truck.getOdometerReading()).isEqualTo(2);

        inOrder.verify(mockTruckInspectionRepository).save(truckInspectionCaptor.capture());

        TruckInspection createdEntry = truckInspectionCaptor.getValue();
        assertThat(createdEntry).isNotNull();
        assertThat(createdEntry.getOdometerReading()).isEqualTo(2);
        assertThat(createdEntry.getNotes()).isEqualTo("some-notes");
        assertThat(createdEntry.getTruckVin()).isEqualTo(truck.getVin());
    }

    @Test
    public void sendForInspection() {
        FleetTruck truck = FleetTruck.buyTruck("test-0001", 0);
        truck.returnFromInspection(1);
        when(mockTruckRepository.findOne(any())).thenReturn(truck);


        fleetTruckService.sendForInspection(truck.getVin());


        InOrder inOrder = inOrder(mockTruckRepository);
        inOrder.verify(mockTruckRepository).findOne(truck.getVin());
        inOrder.verify(mockTruckRepository).save(truck);
    }

    @Test
    public void removeFromYard() {
        FleetTruck truck = FleetTruck.buyTruck("test-0001", 0);
        truck.returnFromInspection(100);
        when(mockTruckRepository.findOne(any())).thenReturn(truck);

        fleetTruckService.removeFromYard(truck.getVin());

        verify(mockTruckRepository).save(truckCaptor.capture());
        assertThat(truckCaptor.getValue().getStatus()).isEqualTo(FleetTruckStatus.NOT_INSPECTABLE);
    }

    @Test
    public void returnToYard() {
        FleetTruck truck = FleetTruck.buyTruck("test-0001", 0);
        truck.returnFromInspection(100);
        truck.removeFromYard();
        when(mockTruckRepository.findOne(any())).thenReturn(truck);

        fleetTruckService.returnToYard(truck.getVin(), 200);

        verify(mockTruckRepository).save(truckCaptor.capture());
        assertThat(truckCaptor.getValue().getStatus()).isEqualTo(FleetTruckStatus.INSPECTABLE);
        assertThat(truckCaptor.getValue().getOdometerReading()).isEqualTo(300);
    }

    @Test
    public void findAllTruckSinceInspections() {
        fleetTruckService.findAllTruckSinceInspections();

        verify(mockTruckSinceInspectionRepository).findAllTruckSinceInspections();
    }

    @Test
    public void returnFromInspection_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetTruckService.returnFromInspection(truckVin, "some-notes", 5000))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockTruckRepository, times(0)).save(any(FleetTruck.class));
        verify(mockTruckInspectionRepository, times(0)).save(any(TruckInspection.class));
    }

    @Test
    public void sendForInspection_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetTruckService.sendForInspection(truckVin))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockTruckRepository, times(0)).save(any(FleetTruck.class));
    }

    @Test
    public void removeFromYard_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetTruckService.removeFromYard(truckVin))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockTruckRepository, times(0)).save(any(FleetTruck.class));
    }

    @Test
    public void returnToYard_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetTruckService.returnToYard(truckVin, 100))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockTruckRepository, times(0)).save(any(FleetTruck.class));
    }
}
