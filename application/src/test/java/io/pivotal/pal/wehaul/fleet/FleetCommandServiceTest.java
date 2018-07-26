package io.pivotal.pal.wehaul.fleet;

import io.pivotal.pal.wehaul.fleet.domain.command.*;
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
public class FleetCommandServiceTest {

    @Mock
    private TruckInfoLookupClient mockTruckInfoLookupClient;
    @Mock
    private FleetTruckCommandRepository mockFleetTruckCommandRepository;

    @Captor
    private ArgumentCaptor<FleetTruck> fleetTruckCaptor;

    private FleetCommandService fleetCommandService;

    @Before
    public void setUp() {
        fleetCommandService = new FleetCommandService(mockTruckInfoLookupClient, mockFleetTruckCommandRepository);
    }

    @Test
    public void buyTruck() {
        MakeModel makeModel = new MakeModel("some-make", "some-model");
        when(mockTruckInfoLookupClient.getMakeModelByVin(any())).thenReturn(makeModel);

        fleetCommandService.buyTruck("test-0001", 1000);

        InOrder inOrder = inOrder(mockTruckInfoLookupClient, mockFleetTruckCommandRepository);
        inOrder.verify(mockTruckInfoLookupClient).getMakeModelByVin("test-0001");
        inOrder.verify(mockFleetTruckCommandRepository).save(fleetTruckCaptor.capture());

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
        when(mockFleetTruckCommandRepository.findOne(any())).thenReturn(mockTruck);

        fleetCommandService.returnTruckFromInspection("some-vin", "some-notes", 2);

        InOrder inOrder = inOrder(mockTruck, mockFleetTruckCommandRepository);
        inOrder.verify(mockFleetTruckCommandRepository).findOne("some-vin");
        inOrder.verify(mockTruck).returnFromInspection("some-notes", 2);
        inOrder.verify(mockFleetTruckCommandRepository).save(mockTruck);
    }

    @Test
    public void sendTruckForInspection() {
        FleetTruck mockTruck = mock(FleetTruck.class);
        when(mockFleetTruckCommandRepository.findOne(any())).thenReturn(mockTruck);

        fleetCommandService.sendTruckForInspection("some-vin");

        InOrder inOrder = inOrder(mockTruck, mockFleetTruckCommandRepository);
        inOrder.verify(mockFleetTruckCommandRepository).findOne("some-vin");
        inOrder.verify(mockTruck).sendForInspection();
        inOrder.verify(mockFleetTruckCommandRepository).save(mockTruck);
    }

    @Test
    public void removeTruckFromYard() {
        FleetTruck mockTruck = mock(FleetTruck.class);
        when(mockTruck.getVin()).thenReturn("some-vin");
        when(mockFleetTruckCommandRepository.findOne(any())).thenReturn(mockTruck);

        fleetCommandService.removeTruckFromYard(mockTruck.getVin());

        InOrder inOrder = inOrder(mockTruck, mockFleetTruckCommandRepository);
        inOrder.verify(mockFleetTruckCommandRepository).findOne("some-vin");
        inOrder.verify(mockTruck).removeFromYard();
        inOrder.verify(mockFleetTruckCommandRepository).save(mockTruck);
    }

    @Test
    public void returnTruckToYard() {
        FleetTruck mockTruck = mock(FleetTruck.class);
        when(mockTruck.getVin()).thenReturn("some-vin");
        when(mockFleetTruckCommandRepository.findOne(any())).thenReturn(mockTruck);

        fleetCommandService.returnTruckToYard(mockTruck.getVin(), 200);

        InOrder inOrder = inOrder(mockTruck, mockFleetTruckCommandRepository);
        inOrder.verify(mockFleetTruckCommandRepository).findOne("some-vin");
        inOrder.verify(mockTruck).returnToYard(200);
        inOrder.verify(mockFleetTruckCommandRepository).save(mockTruck);
    }

    @Test
    public void returnTruckFromInspection_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetCommandService.returnTruckFromInspection(truckVin, "some-notes", 5000))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockFleetTruckCommandRepository, never()).save(any(FleetTruck.class));
    }

    @Test
    public void sendTruckForInspection_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetCommandService.sendTruckForInspection(truckVin))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockFleetTruckCommandRepository, never()).save(any(FleetTruck.class));
    }

    @Test
    public void removeTruckFromYard_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetCommandService.removeTruckFromYard(truckVin))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockFleetTruckCommandRepository, never()).save(any(FleetTruck.class));
    }

    @Test
    public void returnTruckToYard_whenNoTruckFound() {
        String truckVin = "cant-find-me";
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> fleetCommandService.returnTruckToYard(truckVin, 100))
                .withMessage(String.format("No truck found with VIN=%s", truckVin));

        verify(mockFleetTruckCommandRepository, never()).save(any(FleetTruck.class));
    }
}
