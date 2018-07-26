package io.pivotal.pal.wehaul.rental.service;

import io.pivotal.pal.wehaul.rental.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;
    private final RentalTruckRepository rentalTruckRepository;

    public RentalService(RentalRepository rentalRepository,
                         RentalTruckRepository rentalTruckRepository) {
        this.rentalRepository = rentalRepository;
        this.rentalTruckRepository = rentalTruckRepository;
    }

    @Transactional
    public Rental createRental(String customerName) {
        // TODO: implement me using tests and previous implementation as guides
        return null;
    }

    @Transactional
    public void pickUp(UUID confirmationNumber) {
        // TODO: implement me using tests and previous implementation as guides
    }

    @Transactional
    public Rental dropOff(UUID rentalId, int distanceTraveled) {
        // TODO: implement me using tests and previous implementation as guides
        return null;
    }

    public Collection<Rental> findAll() {
        Collection<Rental> rentals = new ArrayList<>();
        rentalRepository.findAll().forEach(rentals::add);
        return rentals;
    }

    public void createRentableTruck(String vin) {
        // TODO: implement this new method
    }

    public void removeRentableTruck(String vin) {
        // TODO: implement this new method
    }

    public void addRentableTruck(String vin) {
        // TODO: implement this new method
    }
}
