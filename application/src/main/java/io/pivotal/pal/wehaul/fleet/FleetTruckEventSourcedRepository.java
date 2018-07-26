package io.pivotal.pal.wehaul.fleet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.pivotal.pal.wehaul.fleet.domain.command.FleetTruck;
import io.pivotal.pal.wehaul.fleet.domain.command.FleetTruckCommandRepository;
import io.pivotal.pal.wehaul.fleet.domain.command.event.FleetTruckEvent;
import io.pivotal.pal.wehaul.fleet.domain.query.FleetTruckUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FleetTruckEventSourcedRepository implements FleetTruckCommandRepository {

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
        List<FleetTruckEventStoreEntity> existingEventEntities = eventStoreRepository.findAllByKeyVinOrderByKeyVersion(fleetTruck.getVin());
        Integer currentVersion = existingEventEntities.stream()
                .map(e -> e.getKey().getVersion())
                .max(Comparator.naturalOrder())
                .orElse(-1);

        List<FleetTruckEvent> events = new ArrayList<>(fleetTruck.fleetDomainEvents());
        List<FleetTruckEventStoreEntity> eventEntities = new ArrayList<>();

        for (int i = 0; i < events.size(); i++) {
            String eventJson;
            FleetTruckEvent event = events.get(i);
            try {
                eventJson = objectMapper.writeValueAsString(event);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            FleetTruckEventStoreEntityKey eventEntityKey =
                    new FleetTruckEventStoreEntityKey(fleetTruck.getVin(), i + currentVersion + 1);
            FleetTruckEventStoreEntity eventEntity =
                    new FleetTruckEventStoreEntity(eventEntityKey, event.getClass(), eventJson);
            eventEntities.add(eventEntity);
        }

        eventStoreRepository.save(eventEntities);

        fleetTruck.fleetDomainEvents()
                .forEach(event -> eventPublisher.publishEvent(event));

        eventPublisher.publishEvent(new FleetTruckUpdatedEvent(fleetTruck));

        return fleetTruck;
    }

    @Override
    public FleetTruck findOne(String vin) {
        List<FleetTruckEventStoreEntity> eventEntities = eventStoreRepository.findAllByKeyVinOrderByKeyVersion(vin);

        if (eventEntities.size() < 1) {
            return null;
        }

        List<FleetTruckEvent> fleetTruckEvents = mapEntitiesToEvents(eventEntities);

        return new FleetTruck(fleetTruckEvents);
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
