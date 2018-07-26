package io.pivotal.pal.wehaul.fleet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.pal.wehaul.fleet.domain.command.FleetTruck;
import io.pivotal.pal.wehaul.fleet.domain.command.FleetTruckStatus;
import io.pivotal.pal.wehaul.fleet.domain.command.MakeModel;
import io.pivotal.pal.wehaul.fleet.domain.command.TruckInfoLookupClient;
import io.pivotal.pal.wehaul.fleet.domain.command.event.FleetTruckEvent;
import io.pivotal.pal.wehaul.fleet.domain.command.event.FleetTruckPurchased;
import io.pivotal.pal.wehaul.fleet.domain.command.event.FleetTruckReturnedFromInspection;
import io.pivotal.pal.wehaul.fleet.domain.query.FleetTruckUpdatedEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FleetTruckEventSourcedRepositoryTest {

    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<Iterable<FleetTruckEventStoreEntity>> eventEntitiesCaptor;

    @Mock
    private TruckInfoLookupClient mockTruckInfoLookupClient;
    @Mock
    private FleetTruckEventStoreRepository mockEventStoreRepository;
    @Mock
    private ApplicationEventPublisher mockApplicationEventPublisher;

    @InjectMocks
    private FleetTruckEventSourcedRepository fleetTruckRepository;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper().disable(WRITE_DATES_AS_TIMESTAMPS).findAndRegisterModules();
        when(mockTruckInfoLookupClient.getMakeModelByVin(any())).thenReturn(new MakeModel("some-make", "some-model"));
    }

    @Test
    public void save_withNewEvents() throws IOException {
        FleetTruck fleetTruck = new FleetTruck("some-vin", 1000, new MakeModel("some-make", "some-model"));
        fleetTruck.returnFromInspection("some-notes", 2000);


        FleetTruck savedFleetTruck = fleetTruckRepository.save(fleetTruck);


        assertThat(savedFleetTruck).isEqualToComparingOnlyGivenFields(fleetTruck, "vin");

        verify(mockEventStoreRepository).save(eventEntitiesCaptor.capture());
        List<FleetTruckEventStoreEntity> savedEvents = stream(eventEntitiesCaptor.getValue().spliterator(), false)
                .collect(toList());

        assertThat(savedEvents).hasSize(2);
        assertThat(savedEvents).extracting(se -> se.getKey().getVin()).containsOnly(fleetTruck.getVin());
        assertThat(savedEvents).extracting(se -> se.getKey().getVersion()).containsExactly(0, 1);
        assertThat(savedEvents.get(0).getEventClass()).isEqualTo(FleetTruckPurchased.class);
        assertThat(savedEvents.get(1).getEventClass()).isEqualTo(FleetTruckReturnedFromInspection.class);

        FleetTruckPurchased firstEvent = objectMapper.readValue(
                savedEvents.get(0).getData(),
                FleetTruckPurchased.class
        );
        assertThat(firstEvent).isEqualToIgnoringGivenFields(
                new FleetTruckPurchased("some-vin", "some-make", "some-model", 1000),
                "id", "createdDate"
        );

        FleetTruckReturnedFromInspection secondEvent = objectMapper.readValue(
                savedEvents.get(1).getData(),
                FleetTruckReturnedFromInspection.class
        );
        assertThat(secondEvent).isEqualToIgnoringGivenFields(
                new FleetTruckReturnedFromInspection("some-vin", 2000, "some-notes"),
                "id", "createdDate"
        );
    }

    @Test
    public void save_withExistingEvents() throws IOException {
        FleetTruckPurchased existingEvent = new FleetTruckPurchased("some-vin", "some-make", "some-model", 1000);
        FleetTruckEventStoreEntity existingEventStoreEntity = new FleetTruckEventStoreEntity(
                new FleetTruckEventStoreEntityKey("some-vin", 0),
                FleetTruckPurchased.class,
                objectMapper.writeValueAsString(existingEvent)
        );
        when(mockEventStoreRepository.findAllByKeyVinOrderByKeyVersion(any())).thenReturn(singletonList(existingEventStoreEntity));

        FleetTruck fleetTruck = new FleetTruck("some-vin", 2000, new MakeModel("some-make", "some-model")); // second event


        fleetTruckRepository.save(fleetTruck);


        verify(mockEventStoreRepository).findAllByKeyVinOrderByKeyVersion("some-vin");
        verify(mockEventStoreRepository).save(eventEntitiesCaptor.capture());
        List<FleetTruckEventStoreEntity> savedEvents = stream(eventEntitiesCaptor.getValue().spliterator(), false)
                .collect(toList());

        assertThat(savedEvents).hasSize(1);
        assertThat(savedEvents.get(0).getKey().getVersion()).isEqualTo(1);
    }

    @Test
    public void save_publishesEvent() {
        FleetTruck mockFleetTruck = mock(FleetTruck.class);
        FleetTruckEvent mockFleetTruckEvent1 = new FleetTruckPurchased("vin", "make", "model", 0);
        FleetTruckEvent mockFleetTruckEvent2 = new FleetTruckReturnedFromInspection("vin", 0, "notes");
        when(mockFleetTruck.fleetDomainEvents())
                .thenReturn(Arrays.asList(mockFleetTruckEvent1, mockFleetTruckEvent2));
        when(mockFleetTruck.getMakeModel()).thenReturn(new MakeModel("make", "model"));
        when(mockFleetTruck.getStatus()).thenReturn(FleetTruckStatus.INSPECTABLE);

        FleetTruck savedFleetTruck = fleetTruckRepository.save(mockFleetTruck);

        assertThat(savedFleetTruck).isSameAs(mockFleetTruck);

        verify(mockApplicationEventPublisher).publishEvent(mockFleetTruckEvent1);
        verify(mockApplicationEventPublisher).publishEvent(mockFleetTruckEvent2);
        verify(mockApplicationEventPublisher).publishEvent(new FleetTruckUpdatedEvent(mockFleetTruck));
    }

    @Test
    public void findOne() throws JsonProcessingException {
        FleetTruckPurchased existingEvent1 =
                new FleetTruckPurchased("some-vin", "some-make", "some-model", 1000);
        FleetTruckEventStoreEntity existingEventStoreEntity1 = new FleetTruckEventStoreEntity(
                new FleetTruckEventStoreEntityKey("some-vin", 0),
                FleetTruckPurchased.class,
                objectMapper.writeValueAsString(existingEvent1)
        );

        FleetTruckReturnedFromInspection existingEvent2 =
                new FleetTruckReturnedFromInspection("some-vin", 2000, "some-notes");
        FleetTruckEventStoreEntity existingEventStoreEntity2 = new FleetTruckEventStoreEntity(
                new FleetTruckEventStoreEntityKey("some-vin", 1),
                FleetTruckReturnedFromInspection.class,
                objectMapper.writeValueAsString(existingEvent2)
        );

        when(mockEventStoreRepository.findAllByKeyVinOrderByKeyVersion(any()))
                .thenReturn(asList(existingEventStoreEntity1, existingEventStoreEntity2));


        FleetTruck fleetTruck = fleetTruckRepository.findOne("some-vin");


        assertThat(fleetTruck.getVin()).isEqualTo("some-vin");
        assertThat(fleetTruck.getStatus()).isEqualTo(FleetTruckStatus.INSPECTABLE);
        assertThat(fleetTruck.getOdometerReading()).isEqualTo(2000);
        assertThat(fleetTruck.getMakeModel()).isEqualTo(new MakeModel("some-make", "some-model"));

        assertThat(fleetTruck.getInspections()).hasSize(1);
        assertThat(fleetTruck.getInspections().get(0).getTruckVin()).isEqualTo("some-vin");
        assertThat(fleetTruck.getInspections().get(0).getOdometerReading()).isEqualTo(2000);
        assertThat(fleetTruck.getInspections().get(0).getNotes()).isEqualTo("some-notes");

        assertThat(fleetTruck.fleetDomainEvents()).isEmpty();

        verify(mockEventStoreRepository).findAllByKeyVinOrderByKeyVersion("some-vin");
    }

    @Test
    public void findOne_notFound() {
        when(mockEventStoreRepository.findAllByKeyVinOrderByKeyVersion(any())).thenReturn(emptyList());

        FleetTruck found = fleetTruckRepository.findOne("bad-vin");

        assertThat(found).isNull();
    }
}
