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
    private TruckInfoLookupClient mockTruckInfoLookupClient;
    @Mock
    private FleetTruckRepository mockFleetTruckRepository;
    @Captor
    private ArgumentCaptor<FleetTruck> fleetTruckCaptor;

    private FleetService fleetService;

    @Before
    public void setUp() {
        fleetService = new FleetService(mockTruckInfoLookupClient, mockFleetTruckRepository);
    }

    @Test
    public void buyTruck() {
        MakeModel makeModel = new MakeModel("some-make", "some-model");
        when(mockTruckInfoLookupClient.getMakeModelByVin(any())).thenReturn(makeModel);

        fleetService.buyTruck("test-0001", 1000);

        InOrder inOrder = inOrder(mockTruckInfoLookupClient, mockFleetTruckRepository);
        inOrder.verify(mockTruckInfoLookupClient).getMakeModelByVin("test-0001");
        inOrder.verify(mockFleetTruckRepository).save(fleetTruckCaptor.capture());

        FleetTruck savedTruck = fleetTruckCaptor.getValue();
        assertThat(savedTruck.getVin()).isEqualTo("test-0001");
        assertThat(savedTruck.getStatus()).isEqualTo(FleetTruckStatus.IN_INSPECTION);
        assertThat(savedTruck.getOdometerReading()).isEqualTo(1000);
        assertThat(savedTruck.getMakeModel()).isEqualTo(makeModel);
        assertThat(savedTruck.getInspections()).isNullOrEmpty();
    }

    @Test
    public void returnTruckFromInspection() {
        FleetTruck mockTruck = mock(FleetTruck.class);
        when(mockFleetTruckRepository.findOne(any())).thenReturn(mockTruck);

        fleetService.returnTruckFromInspection("some-vin", "some-notes", 2);

        InOrder inOrder = inOrder(mockTruck, mockFleetTruckRepository);
        inOrder.verify(mockFleetTruckRepository).findOne("some-vin");
        inOrder.verify(mockTruck).returnFromInspection("some-notes", 2);
        inOrder.verify(mockFleetTruckRepository).save(mockTruck);
    }

    @Test
    public void sendTruckForInspection() {
        FleetTruck mockTruck = mock(FleetTruck.class);
        when(mockFleetTruckRepository.findOne(any())).thenReturn(mockTruck);

        fleetService.sendTruckForInspection("some-vin");

        InOrder inOrder = inOrder(mockTruck, mockFleetTruckRepository);
        inOrder.verify(mockFleetTruckRepository).findOne("some-vin");
        inOrder.verify(mockTruck).sendForInspection();
        inOrder.verify(mockFleetTruckRepository).save(mockTruck);
    }

    @Test
    public void removeTruckFromYard() {
        FleetTruck mockTruck = mock(FleetTruck.class);
        when(mockTruck.getVin()).thenReturn("some-vin");
        when(mockFleetTruckRepository.findOne(any())).thenReturn(mockTruck);

        fleetService.removeTruckFromYard(mockTruck.getVin());

        InOrder inOrder = inOrder(mockTruck, mockFleetTruckRepository);
        inOrder.verify(mockFleetTruckRepository).findOne("some-vin");
        inOrder.verify(mockTruck).removeFromYard();
        inOrder.verify(mockFleetTruckRepository).save(mockTruck);
    }

    @Test
    public void returnTruckToYard() {
        FleetTruck mockTruck = mock(FleetTruck.class);
        when(mockTruck.getVin()).thenReturn("some-vin");
        when(mockFleetTruckRepository.findOne(any())).thenReturn(mockTruck);

        fleetService.returnTruckToYard(mockTruck.getVin(), 200);

        InOrder inOrder = inOrder(mockTruck, mockFleetTruckRepository);
        inOrder.verify(mockFleetTruckRepository).findOne("some-vin");
        inOrder.verify(mockTruck).returnToYard(200);
        inOrder.verify(mockFleetTruckRepository).save(mockTruck);
    }

    @Test
    public void returnTruckFromInspection_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetService.returnTruckFromInspection(truckVin, "some-notes", 5000))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockFleetTruckRepository, never()).save(any(FleetTruck.class));
    }

    @Test
    public void sendTruckForInspection_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetService.sendTruckForInspection(truckVin))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockFleetTruckRepository, never()).save(any(FleetTruck.class));
    }

    @Test
    public void removeTruckFromYard_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetService.removeTruckFromYard(truckVin))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockFleetTruckRepository, never()).save(any(FleetTruck.class));
    }

    @Test
    public void returnTruckToYard_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetService.returnTruckToYard(truckVin, 100))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockFleetTruckRepository, never()).save(any(FleetTruck.class));
    }

    @Test
    public void findAll() {
        FleetTruck mockTruck1 = mock(FleetTruck.class);
        FleetTruck mockTruck2 = mock(FleetTruck.class);
        List<FleetTruck> toBeReturned = Arrays.asList(mockTruck1, mockTruck2);
        when(mockFleetTruckRepository.findAll()).thenReturn(toBeReturned);

        Collection<FleetTruck> trucks = fleetService.findAll();
        assertThat(trucks).hasSameElementsAs(toBeReturned);

        verify(mockFleetTruckRepository).findAll();
    }
}
