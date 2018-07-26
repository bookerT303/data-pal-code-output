package io.pivotal.pal.wehaul.service;

import io.pivotal.pal.wehaul.rental.domain.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;
    private final RentalTruckRepository rentalTruckRepository;
    private final RentalTruck.Factory rentalTruckFactory;

    public RentalService(RentalRepository rentalRepository,
                         RentalTruckRepository rentalTruckRepository,
                         RentalTruck.Factory rentalTruckFactory) {
        this.rentalRepository = rentalRepository;
        this.rentalTruckRepository = rentalTruckRepository;
        this.rentalTruckFactory = rentalTruckFactory;
    }

    @Transactional
    public Rental createRental(String customerName) {
        RentalTruck truck = rentalTruckRepository.findTop1ByStatus(RentalTruckStatus.RENTABLE);
        if (truck == null) {
            throw new IllegalStateException("No trucks available to rent");
        }

        truck.reserve();
        rentalTruckRepository.save(truck);

        Rental rental = Rental.createRental(customerName, truck.getVin());
        return rentalRepository.save(rental);
    }

    @Transactional
    public void pickUp(UUID confirmationNumber) {
        Rental rental = rentalRepository.findOne(confirmationNumber);
        if (rental == null) {
            throw new IllegalArgumentException(String.format("No rental found for id=%s", confirmationNumber));
        }

        rental.pickUp();
        rentalRepository.save(rental);

        RentalTruck truck = rentalTruckRepository.findOne(rental.getTruckVin());
        truck.pickUp();
        rentalTruckRepository.save(truck);
    }

    @Transactional
    public Rental dropOff(UUID rentalId, int distanceTraveled) {
        Rental rental = rentalRepository.findOne(rentalId);
        if (rental == null) {
            throw new IllegalArgumentException(String.format("No rental found for id=%s", rentalId));
        }

        rental.dropOff(distanceTraveled);
        rentalRepository.save(rental);

        String vin = rental.getTruckVin();
        RentalTruck truck = rentalTruckRepository.findOne(vin);
        truck.dropOff();
        rentalTruckRepository.save(truck);

        return rental;
    }

    public Collection<Rental> findAll() {
        Collection<Rental> rentals = new ArrayList<>();
        rentalRepository.findAll().forEach(rentals::add);
        return rentals;
    }

    public void addTruck(String vin, String make, String model) {
        RentalTruck truck = rentalTruckFactory.createRentableTruck(vin, make, model);

        truck.preventRenting();

        rentalTruckRepository.save(truck);
    }

    public void preventRenting(String vin) {
        RentalTruck truck = rentalTruckRepository.findOne(vin);
        if (truck == null) {
            throw new IllegalArgumentException(String.format("No truck found with vin=%s", vin));
        }

        truck.preventRenting();
        rentalTruckRepository.save(truck);
    }

    public void allowRenting(String vin) {
        RentalTruck truck = rentalTruckRepository.findOne(vin);
        if (truck == null) {
            throw new IllegalArgumentException(String.format("No truck found with vin=%s", vin));
        }

        truck.allowRenting();
        rentalTruckRepository.save(truck);
    }
}
