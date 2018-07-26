package io.pivotal.pal.wehaul.config;

import io.pivotal.pal.wehaul.adapter.FleetTruckEventSourcedRepository;
import io.pivotal.pal.wehaul.event.store.FleetTruckEventStoreRepository;
import io.pivotal.pal.wehaul.fleet.domain.FleetTruckRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public FleetTruckRepository eventPublishingFleetTruckRepository(FleetTruckEventStoreRepository eventStoreRepository,
                                                                    ApplicationEventPublisher applicationEventPublisher) {

        return new FleetTruckEventSourcedRepository(eventStoreRepository, applicationEventPublisher);
    }
}
