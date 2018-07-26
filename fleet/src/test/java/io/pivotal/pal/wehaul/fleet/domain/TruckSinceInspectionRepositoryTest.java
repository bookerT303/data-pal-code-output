package io.pivotal.pal.wehaul.fleet.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@DataJpaTest
@Import(TruckSinceInspectionRepository.class)
public class TruckSinceInspectionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TruckSinceInspectionRepository truckSinceInspectionRepository;

    @Test
    public void findAllSinceInspection() {
        String vin = "test-0001";
        FleetTruck truck = FleetTruck.buyTruck(vin, 4000);
        TruckInspection truckInspection = new TruckInspection(vin, 2000, "bad stuff");

        entityManager.persistAndFlush(truck);
        entityManager.persistAndFlush(truckInspection);

        Collection<TruckSinceInspection> truckSinceInspections =
                truckSinceInspectionRepository.findAllTruckSinceInspections();

        assertThat(truckSinceInspections)
                .hasSize(1)
                .extracting(TruckSinceInspection::getTruckVin)
                .containsExactly(vin);
        assertThat(truckSinceInspections)
                .extracting(TruckSinceInspection::getLastInspectionDistance)
                .containsExactly(2000);
    }
}
