package io.pivotal.pal.wehaul.config;

import io.pivotal.pal.wehaul.domain.Truck;
import io.pivotal.pal.wehaul.domain.TruckInfoLookupClient;
import io.pivotal.pal.wehaul.domain.TruckSizeLookupClient;
import io.pivotal.pal.wehaul.impl.InMemoryTruckInfoLookupClient;
import io.pivotal.pal.wehaul.impl.InMemoryTruckSizeLookupClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public Truck.Factory truckFactory(TruckInfoLookupClient truckInfoLookupClient,
                                      TruckSizeLookupClient truckSizeLookupClient) {
        return new Truck.Factory(truckInfoLookupClient, truckSizeLookupClient);
    }

    @Bean
    public TruckInfoLookupClient truckInfoLookupClient() {
        return new InMemoryTruckInfoLookupClient();
    }

    @Bean
    public TruckSizeLookupClient truckSizeLookupClient() {
        return new InMemoryTruckSizeLookupClient();
    }
}