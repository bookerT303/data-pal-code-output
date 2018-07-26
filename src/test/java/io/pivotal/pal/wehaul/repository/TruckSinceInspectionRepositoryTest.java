package io.pivotal.pal.wehaul.repository;

import io.pivotal.pal.wehaul.domain.Truck;
import io.pivotal.pal.wehaul.domain.TruckInspection;
import io.pivotal.pal.wehaul.domain.TruckSinceInspection;
import io.pivotal.pal.wehaul.domain.TruckStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@SpringBootTest
public class TruckSinceInspectionRepositoryTest {

    @Autowired
    private TruckRepository truckRepository;

    @Autowired
    private TruckInspectionRepository truckInspectionRepository;

    @Autowired
    private TruckSinceInspectionRepository truckSinceInspectionRepository;

    @Test
    public void findAllSinceInspection() {
        String vin = "test-0001";
        Truck truck = new Truck(vin, 4000);
        truckRepository.save(truck);

        TruckInspection inspection = new TruckInspection(vin, 2000, "bad stuff");
        truckInspectionRepository.save(inspection);

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
