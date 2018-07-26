package io.pivotal.pal.wehaul.rental.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RentalTruckFactoryTest {

    @Mock
    private TruckSizeLookupClient mockTruckSizeLookupClient;

    @Test
    public void createRentableTruck() {
        when(mockTruckSizeLookupClient.getSizeByMakeModel(any(), any()))
                .thenReturn(RentalTruckSize.LARGE);

        RentalTruck truck = new RentalTruck.Factory(mockTruckSizeLookupClient)
                .createRentableTruck("test-0001", "some-make", "some-model");

        assertThat(truck.getVin()).isEqualTo("test-0001");
        assertThat(truck.getStatus()).isEqualTo(RentalTruckStatus.RENTABLE);
        assertThat(truck.getSize()).isEqualTo(RentalTruckSize.LARGE);

        verify(mockTruckSizeLookupClient).getSizeByMakeModel("some-make", "some-model");
    }
}
