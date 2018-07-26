package io.pivotal.pal.wehaul.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.pivotal.pal.wehaul.event.store.FleetTruckEventStoreEntity;
import io.pivotal.pal.wehaul.event.store.FleetTruckEventStoreRepository;
import io.pivotal.pal.wehaul.fleet.domain.FleetTruck;
import io.pivotal.pal.wehaul.fleet.domain.FleetTruckRepository;
import io.pivotal.pal.wehaul.fleet.domain.event.FleetTruckEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FleetTruckEventSourcedRepository implements FleetTruckRepository {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .findAndRegisterModules();

    private final FleetTruckEventStoreRepository eventStoreRepository;
    private final ApplicationEventPublisher eventPublisher;

    public FleetTruckEventSourcedRepository(FleetTruckEventStoreRepository eventStoreRepository,
                                            ApplicationEventPublisher eventPublisher) {
        this.eventStoreRepository = eventStoreRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public FleetTruck save(FleetTruck fleetTruck) {
        return null;
    }

    @Override
    public FleetTruck findOne(String vin) {
        return null;
    }

    @Override
    public List<FleetTruck> findAll() {
        Map<String, List<FleetTruckEventStoreEntity>> eventEntitiesByVin =
                eventStoreRepository.findAll(new Sort(Sort.Direction.ASC, "key.vin", "key.version"))
                        .stream()
                        .collect(Collectors.groupingBy(eventEntity -> eventEntity.getKey().getVin()));

        return eventEntitiesByVin.entrySet()
                .stream()
                .map(eventEntities -> mapEntitiesToEvents(eventEntities.getValue()))
                .map(FleetTruck::new)
                .sorted(Comparator.comparing(FleetTruck::getVin))
                .collect(Collectors.toList());
    }

    private List<FleetTruckEvent> mapEntitiesToEvents(List<FleetTruckEventStoreEntity> eventEntities) {
        return eventEntities.stream()
                .map(eventEntity -> {
                    try {
                        return (FleetTruckEvent) objectMapper.readValue(eventEntity.getData(), eventEntity.getEventClass());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }
}
