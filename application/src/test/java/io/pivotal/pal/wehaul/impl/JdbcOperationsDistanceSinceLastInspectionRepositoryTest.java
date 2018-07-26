package io.pivotal.pal.wehaul.impl;

import io.pivotal.pal.wehaul.domain.*;
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
public class JdbcOperationsDistanceSinceLastInspectionRepositoryTest {

    @Autowired
    private Truck.Factory truckFactory;

    @Autowired
    private TruckRepository truckRepository;

    @Autowired
    private TruckInspectionRepository truckInspectionRepository;

    @Autowired
    private DistanceSinceLastInspectionRepository distanceSinceLastInspectionRepository;

    @Test
    public void findAllDistanceSinceLastInspections() {
        String vin = "test-0001";
        Truck truck = truckFactory.buyTruck(vin, 4000);
        truckRepository.save(truck);

        TruckInspection inspection = TruckInspection.createTruckInspection(vin, 2000, "bad stuff");
        truckInspectionRepository.save(inspection);

        Collection<DistanceSinceLastInspection> distanceSinceLastInspections =
                distanceSinceLastInspectionRepository.findAllDistanceSinceLastInspections();

        assertThat(distanceSinceLastInspections)
                .hasSize(1)
                .extracting(DistanceSinceLastInspection::getTruckVin)
                .containsExactly(vin);
        assertThat(distanceSinceLastInspections)
                .extracting(DistanceSinceLastInspection::getLastInspectionDistance)
                .containsExactly(2000);
    }
}
