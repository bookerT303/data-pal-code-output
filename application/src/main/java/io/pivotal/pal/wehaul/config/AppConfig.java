package io.pivotal.pal.wehaul.config;

import io.pivotal.pal.wehaul.fleet.domain.FleetTruck;
import io.pivotal.pal.wehaul.fleet.domain.TruckInfoLookupClient;
import io.pivotal.pal.wehaul.rental.domain.RentalTruck;
import io.pivotal.pal.wehaul.rental.domain.TruckSizeLookupClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public FleetTruck.Factory fleetTruckFactory(TruckInfoLookupClient truckInfoLookupClient) {
        return new FleetTruck.Factory(truckInfoLookupClient);
    }

    @Bean
    public RentalTruck.Factory rentalTruckFactory(TruckSizeLookupClient truckSizeLookupClient) {
        return new RentalTruck.Factory(truckSizeLookupClient);
    }
}
