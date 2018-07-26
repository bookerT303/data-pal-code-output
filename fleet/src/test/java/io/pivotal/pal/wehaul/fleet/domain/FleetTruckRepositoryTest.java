package io.pivotal.pal.wehaul.fleet.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@DataJpaTest
public class FleetTruckRepositoryTest {

    @Autowired
    private FleetTruckRepository truckRepository;
    @Mock
    private TruckInfoLookupClient mockTruckInfoLookupClient;

    @Test
    public void findTop1ByStatus() {
        truckRepository.deleteAll();
        FleetTruck truck1 = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck("test-0001", 1000);
        FleetTruck truck2 = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck("test-0002", 2000);
        FleetTruck truck3 = new FleetTruck.Factory(mockTruckInfoLookupClient).buyTruck("test-0003", 3000);
        truck3.returnFromInspection("notes", 4000);
        truckRepository.save(Arrays.asList(truck1, truck2, truck3));

        FleetTruck truck = truckRepository.findTop1ByStatus(FleetTruckStatus.INSPECTABLE);

        assertThat(truck).isNotNull().isEqualToComparingOnlyGivenFields(truck3, "vin");
    }

    @Test
    public void findTop1ByStatus_noneFound() {
        truckRepository.deleteAll();
        FleetTruck truck = truckRepository.findTop1ByStatus(FleetTruckStatus.INSPECTABLE);

        assertThat(truck).isNull();
    }

}
