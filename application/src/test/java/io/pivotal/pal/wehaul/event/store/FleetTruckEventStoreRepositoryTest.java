package io.pivotal.pal.wehaul.event.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.pivotal.pal.wehaul.fleet.domain.event.FleetTruckPurchased;
import io.pivotal.pal.wehaul.fleet.domain.event.FleetTruckReturnedFromInspection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@RunWith(SpringRunner.class)
@DataJpaTest
public class FleetTruckEventStoreRepositoryTest {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .findAndRegisterModules();

    @Autowired
    private TestEntityManager testEntityManager;
    @Autowired
    private FleetTruckEventStoreRepository eventStoreRepository;

    @Test
    public void findAllByKeyVin() throws JsonProcessingException {

        FleetTruckReturnedFromInspection truckReturnedFromInspectionEvent =
                new FleetTruckReturnedFromInspection("some-vin", 1000, "some-notes");
        String truckReturnedFromInspectionEventJson = objectMapper.writeValueAsString(truckReturnedFromInspectionEvent);
        FleetTruckEventStoreEntityKey eventStoreEntityKey1 = new FleetTruckEventStoreEntityKey("some-vin", 1);
        FleetTruckEventStoreEntity eventStoreEntity1 =
                new FleetTruckEventStoreEntity(eventStoreEntityKey1, FleetTruckReturnedFromInspection.class, truckReturnedFromInspectionEventJson);
        testEntityManager.persistAndFlush(eventStoreEntity1);

        FleetTruckPurchased truckPurchasedEvent =
                new FleetTruckPurchased("some-vin", "some-make", "some-model", 1000);
        String truckPurchasedEventJson = objectMapper.writeValueAsString(truckPurchasedEvent);
        FleetTruckEventStoreEntityKey eventStoreEntityKey0 = new FleetTruckEventStoreEntityKey("some-vin", 0);
        FleetTruckEventStoreEntity eventStoreEntity0 =
                new FleetTruckEventStoreEntity(eventStoreEntityKey0, FleetTruckPurchased.class, truckPurchasedEventJson);
        testEntityManager.persistAndFlush(eventStoreEntity0);


        List<FleetTruckEventStoreEntity> eventStoreEntities = eventStoreRepository.findAllByKeyVinOrderByKeyVersion("some-vin");

        assertThat(eventStoreEntities).hasSize(2);
        assertThat(eventStoreEntities)
                .extracting(es -> es.getKey().getVin())
                .containsOnly("some-vin");
        assertThat(eventStoreEntities)
                .extracting(es -> es.getKey().getVersion())
                .containsExactly(0, 1);
    }
}
