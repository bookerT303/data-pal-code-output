package io.pivotal.pal.wehaul.repository;

import io.pivotal.pal.wehaul.domain.Truck;
import io.pivotal.pal.wehaul.domain.TruckStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@DataJpaTest
public class TruckRepositoryTest {

    @Autowired
    private TruckRepository truckRepository;

    @Test
    public void findTop1ByStatus() {
        Truck truck1 = new Truck("test-0001", 1000);
        Truck truck2 = new Truck("test-0002", 2000);
        Truck truck3 = new Truck("test-0003", 3000);
        truck3.setStatus(TruckStatus.RENTABLE);
        truck3.setOdometerReading(4000);
        truckRepository.save(Arrays.asList(truck1, truck2, truck3));

        Truck truck = truckRepository.findTop1ByStatus(TruckStatus.RENTABLE);

        assertThat(truck).isNotNull().isEqualToComparingOnlyGivenFields(truck3, "vin");
    }

    @Test
    public void findTop1ByStatus_noneFound() {
        Truck truck = truckRepository.findTop1ByStatus(TruckStatus.RENTABLE);

        assertThat(truck).isNull();
    }
}
