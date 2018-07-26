package io.pivotal.pal.wehaul.domain;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TruckFactoryTest {

    @Mock
    private TruckInfoLookupClient mockTruckInfoLookupClient;
    @Mock
    private TruckSizeLookupClient mockTruckSizeLookupClient;

    private Truck.Factory truckFactory;

    @Before
    public void setUp() {
        truckFactory = new Truck.Factory(mockTruckInfoLookupClient, mockTruckSizeLookupClient);
    }

    @Test
    public void buyTruck() {
        MakeModel makeModel = new MakeModel("some-make", "some-model");
        when(mockTruckInfoLookupClient.getMakeModelByVin(any()))
                .thenReturn(makeModel);
        when(mockTruckSizeLookupClient.getSizeByMakeModel(any()))
                .thenReturn(TruckSize.LARGE);

        String vin = "test-0001";
        int odometerReading = 0;

        Truck truck = truckFactory.buyTruck(vin, odometerReading);

        assertThat(truck.getVin()).isEqualTo("test-0001");
        assertThat(truck.getStatus()).isEqualTo(TruckStatus.IN_INSPECTION);
        assertThat(truck.getOdometerReading()).isEqualTo(odometerReading);
        assertThat(truck.getMakeModel()).isEqualTo(makeModel);

        InOrder inOrder = inOrder(mockTruckInfoLookupClient, mockTruckSizeLookupClient);
        inOrder.verify(mockTruckInfoLookupClient).getMakeModelByVin("test-0001");
        inOrder.verify(mockTruckSizeLookupClient).getSizeByMakeModel(makeModel);
    }
}
