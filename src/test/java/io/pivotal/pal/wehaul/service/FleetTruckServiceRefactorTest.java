package io.pivotal.pal.wehaul.service;

import io.pivotal.pal.wehaul.domain.Truck;
import io.pivotal.pal.wehaul.domain.TruckStatus;
import io.pivotal.pal.wehaul.repository.TruckInspectionRepository;
import io.pivotal.pal.wehaul.repository.TruckRepository;
import io.pivotal.pal.wehaul.repository.TruckSinceInspectionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FleetTruckServiceRefactorTest {

    @Mock
    private TruckRepository mockTruckRepository;
    @Mock
    private TruckInspectionRepository mockTruckInspectionRepository;
    @Mock
    private TruckSinceInspectionRepository mockTruckSinceInspectionRepository;
    @Mock
    private Truck truck;

    private FleetTruckService truckService;

    @Before
    public void setUp() {
        truckService = new FleetTruckService(
            mockTruckRepository,
            mockTruckInspectionRepository,
            mockTruckSinceInspectionRepository
        );
    }

    @Test
    public void sendForInspection() {
        when(mockTruckRepository.findOne(any())).thenReturn(truck);
        when(truck.getStatus()).thenReturn(TruckStatus.RENTABLE);

        truckService.sendForInspection("test-0001");

        verify(truck).sendForInspection();
        verify(truck, times(0)).setStatus(any());
        verify(truck, times(0)).getStatus();
    }

    @Test
    public void returnFromInspection() {
        when(mockTruckRepository.findOne(any())).thenReturn(truck);
        when(truck.getStatus()).thenReturn(TruckStatus.IN_INSPECTION);
        when(truck.getOdometerReading()).thenReturn(100);

        truckService.returnFromInspection("test-0001", "some notes", 200);

        verify(truck).returnFromInspection(200);
        verify(truck, times(0)).setStatus(any());
        verify(truck, times(0)).setOdometerReading(any());
        verify(truck, times(0)).getStatus();
        verify(truck, times(0)).getOdometerReading();
    }
}
