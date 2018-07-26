package io.pivotal.pal.wehaul.service;

import io.pivotal.pal.wehaul.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FleetServiceTest {

    @Mock
    private Truck.Factory mockTruckFactory;
    @Mock
    private TruckRepository mockTruckRepository;
    @Mock
    private TruckInspectionRepository mockTruckInspectionRepository;
    @Mock
    private DistanceSinceLastInspectionRepository mockDistanceSinceLastInspectionRepository;
    @Captor
    private ArgumentCaptor<Truck> truckCaptor;
    @Captor
    private ArgumentCaptor<TruckInspection> truckInspectionCaptor;

    private FleetService fleetService;

    @Before
    public void setUp() {
        fleetService = new FleetService(
                mockTruckFactory,
                mockTruckRepository,
                mockTruckInspectionRepository,
                mockDistanceSinceLastInspectionRepository
        );
    }

    @Test
    public void buyTruck() {
        Truck mockTruck = mock(Truck.class);
        when(mockTruckFactory.buyTruck(any(), anyInt())).thenReturn(mockTruck);

        fleetService.buyTruck("test-0001", 1000);

        InOrder inOrder = inOrder(mockTruckFactory, mockTruckRepository);
        inOrder.verify(mockTruckFactory).buyTruck("test-0001", 1000);
        inOrder.verify(mockTruckRepository).save(truckCaptor.capture());

        Truck savedTruck = truckCaptor.getValue();
        assertThat(savedTruck).isSameAs(mockTruck);
    }

    @Test
    public void sendForInspection() {
        Truck mockTruck = mock(Truck.class);
        when(mockTruckRepository.findOne(any())).thenReturn(mockTruck);

        fleetService.sendForInspection("some-vin");

        InOrder inOrder = inOrder(mockTruck, mockTruckRepository);
        inOrder.verify(mockTruckRepository).findOne("some-vin");
        inOrder.verify(mockTruck).sendForInspection();
        inOrder.verify(mockTruckRepository).save(mockTruck);
    }

    @Test
    public void sendForInspection_whenNoTruckFound() {
        String vin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetService.sendForInspection(vin))
                .withMessage(String.format("No truck found with VIN=%s", vin));

        verify(mockTruckRepository).findOne(vin);
        verifyNoMoreInteractions(mockTruckRepository);
    }

    @Test
    public void returnFromInspection() {
        Truck mockTruck = mock(Truck.class);
        when(mockTruckRepository.findOne(any())).thenReturn(mockTruck);

        fleetService.returnFromInspection("some-vin", "some-notes", 2);

        InOrder inOrder = inOrder(mockTruck, mockTruckRepository, mockTruckInspectionRepository);
        inOrder.verify(mockTruckRepository).findOne("some-vin");
        inOrder.verify(mockTruck).returnFromInspection(2);
        inOrder.verify(mockTruckRepository).save(mockTruck);

        inOrder.verify(mockTruckInspectionRepository).save(truckInspectionCaptor.capture());

        TruckInspection createdEntry = truckInspectionCaptor.getValue();
        assertThat(createdEntry.getTruckVin()).isEqualTo("some-vin");
        assertThat(createdEntry.getOdometerReading()).isEqualTo(2);
        assertThat(createdEntry.getNotes()).isEqualTo("some-notes");
    }

    @Test
    public void returnFromInspection_whenNoTruckFound() {
        String vin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetService.returnFromInspection(vin, "some-notes", 5000))
                .withMessage(String.format("No truck found with VIN=%s", vin));
    }

    @Test
    public void findAllTruckSinceInspections() {
        DistanceSinceLastInspection mockDistanceSinceLastInspection1 = mock(DistanceSinceLastInspection.class);
        DistanceSinceLastInspection mockDistanceSinceLastInspection2 = mock(DistanceSinceLastInspection.class);
        List<DistanceSinceLastInspection> toBeReturned =
                Arrays.asList(mockDistanceSinceLastInspection1, mockDistanceSinceLastInspection2);
        when(mockDistanceSinceLastInspectionRepository.findAllDistanceSinceLastInspections())
                .thenReturn(toBeReturned);

        Collection<DistanceSinceLastInspection> distanceSinceLastInspections = fleetService.findAllDistanceSinceLastInspections();
        assertThat(distanceSinceLastInspections).hasSameElementsAs(toBeReturned);

        verify(mockDistanceSinceLastInspectionRepository).findAllDistanceSinceLastInspections();
    }

    @Test
    public void findAll() {
        Truck mockTruck1 = mock(Truck.class);
        Truck mockTruck2 = mock(Truck.class);
        List<Truck> toBeReturned = Arrays.asList(mockTruck1, mockTruck2);
        when(mockTruckRepository.findAll()).thenReturn(toBeReturned);

        Collection<Truck> trucks = fleetService.findAll();
        assertThat(trucks).hasSameElementsAs(toBeReturned);

        verify(mockTruckRepository).findAll();
    }
}
