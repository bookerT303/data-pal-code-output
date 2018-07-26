package io.pivotal.pal.wehaul.impl;

import io.pivotal.pal.wehaul.fleet.domain.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@DirtiesContext
@RunWith(SpringRunner.class)
@DataJpaTest
@Import(JdbcOperationsDistanceSinceLastInspectionRepository.class)
public class JdbcOperationsDistanceSinceLastInspectionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FleetTruck.Factory fleetTruckFactory;

    @Autowired
    private DistanceSinceLastInspectionRepository distanceSinceLastInspectionRepository;

    @Autowired
    private TruckInfoLookupClient truckInfoLookupClient;

    @Test
    public void findAllSinceInspection() {
        String vin = "test-0001";
        when(truckInfoLookupClient.getMakeModelByVin(any())).thenReturn(new MakeModel("make", "model"));
        FleetTruck truck = fleetTruckFactory.buyTruck("test-0001", 4000);
        TruckInspection truckInspection =
                TruckInspection.createTruckInspection(vin, 2000, "bad stuff");
        entityManager.persist(truckInspection);
        entityManager.persistAndFlush(truck);

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

    @TestConfiguration
    static class TestConfig {

        @Bean
        public FleetTruck.Factory truckFactory() {
            return new FleetTruck.Factory(truckInfoLookupClient);
        }

        @MockBean
        private TruckInfoLookupClient truckInfoLookupClient;
    }

}