package io.pivotal.pal.wehaul.service;

import io.pivotal.pal.wehaul.domain.Truck;
import io.pivotal.pal.wehaul.domain.TruckInspection;
import io.pivotal.pal.wehaul.domain.TruckSinceInspection;
import io.pivotal.pal.wehaul.domain.TruckStatus;
import io.pivotal.pal.wehaul.repository.TruckInspectionRepository;
import io.pivotal.pal.wehaul.repository.TruckRepository;
import io.pivotal.pal.wehaul.repository.TruckSinceInspectionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FleetTruckServiceTest {

    @Mock
    private TruckRepository mockTruckRepository;
    @Mock
    private TruckInspectionRepository mockTruckInspectionRepository;
    @Mock
    private TruckSinceInspectionRepository mockTruckSinceInspectionRepository;
    @Captor
    private ArgumentCaptor<Truck> truckCaptor;
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
    public void sendForInspection() {
        // collaborator setup
        Truck truck = new Truck("test-0001", 0);
        truck.setStatus(TruckStatus.RENTABLE);
        truck.setOdometerReading(1);
        when(mockTruckRepository.findOne(any())).thenReturn(truck);

        // method under test
        fleetTruckService.sendForInspection(truck.getVin());

        // verifications
        InOrder inOrder = inOrder(mockTruckRepository);
        inOrder.verify(mockTruckRepository).findOne(truck.getVin());
        inOrder.verify(mockTruckRepository).save(truck);
    }

    @Test
    public void sendForInspection_whenNoTruckFound() {
        String vin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> fleetTruckService.sendForInspection(vin))
            .withMessage(String.format("No truck found with VIN=%s", vin));
    }

    @Test
    public void returnFromInspection() {
        // collaborator setup
        Truck truck = new Truck("test-0001", 0);
        when(mockTruckRepository.findOne(any())).thenReturn(truck);

        // method under test
        fleetTruckService.returnFromInspection(truck.getVin(), "some-notes", 2);

        // assertions + verifications
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
    public void returnFromInspection_whenNoTruckFound() {
        String vin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> fleetTruckService.returnFromInspection(vin, "some-notes", 5000))
            .withMessage(String.format("No truck found with VIN=%s", vin));
    }

    @Test
    public void findAllTruckSinceInspections() {
        TruckSinceInspection truckSinceInspection1 =
            new TruckSinceInspection("test-0001", 2000);
        TruckSinceInspection truckSinceInspection2 =
            new TruckSinceInspection("test-0001", 3000);
        when(mockTruckSinceInspectionRepository.findAllTruckSinceInspections())
            .thenReturn(Arrays.asList(truckSinceInspection1, truckSinceInspection2));

        fleetTruckService.findAllTruckSinceInspections();

        verify(mockTruckSinceInspectionRepository).findAllTruckSinceInspections();
    }
}
