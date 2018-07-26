package io.pivotal.pal.wehaul.fleet.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FleetTruckFactoryTest {

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
    }

    @Test
    public void buyTruck_negativeOdometer() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> new FleetTruck.Factory(mockTruckInfoLookupClient)
                        .buyTruck("test-0001", -1))
                .withMessage("Cannot buy a truck with negative odometer reading");
    }

}
