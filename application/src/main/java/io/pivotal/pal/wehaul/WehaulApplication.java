package io.pivotal.pal.wehaul;

import io.pivotal.pal.wehaul.fleet.domain.FleetTruckRepository;
import io.pivotal.pal.wehaul.rental.domain.RentalTruckRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class WehaulApplication {

    public static void main(String[] args) {
        SpringApplication.run(WehaulApplication.class, args);
    }

    @Bean
    DatabasePopulator databasePopulator(FleetTruckRepository fleetTruckRepository,
                                        RentalTruckRepository rentalTruckRepository) {
        return new DatabasePopulator(fleetTruckRepository, rentalTruckRepository);
    }
}
