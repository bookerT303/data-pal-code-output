package io.pivotal.pal.wehaul.service;

import io.pivotal.pal.wehaul.domain.Rental;
import io.pivotal.pal.wehaul.domain.Truck;
import io.pivotal.pal.wehaul.domain.TruckStatus;
import io.pivotal.pal.wehaul.repository.RentalRepository;
import io.pivotal.pal.wehaul.repository.TruckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;
    private final TruckRepository truckRepository;

    public RentalService(RentalRepository rentalRepository,
                         TruckRepository truckRepository) {
        this.rentalRepository = rentalRepository;
        this.truckRepository = truckRepository;
    }

    @Transactional
    public Rental createRental(String customerName) {
        Truck truck = truckRepository.findTop1ByStatus(TruckStatus.RENTABLE);
        if (truck == null) {
            throw new IllegalStateException("No trucks available to rent");
        }

        truck.reserve();
        truckRepository.save(truck);

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

        Truck truck = truckRepository.findOne(rental.getTruckVin());
        truck.pickUp();
        truckRepository.save(truck);
    }

    @Transactional
    public void dropOff(UUID confirmationNumber, int distanceTraveled) {
        Rental rental = rentalRepository.findOne(confirmationNumber);
        if (rental == null) {
            throw new IllegalArgumentException(String.format("No rental found for id=%s", confirmationNumber));
        }

        rental.dropOff(distanceTraveled);
        rentalRepository.save(rental);

        String vin = rental.getTruckVin();
        Truck truck = truckRepository.findOne(vin);
        truck.returnToService(truck.getOdometerReading() + distanceTraveled);
        truckRepository.save(truck);
    }

    public Collection<Rental> findAll() {
        Collection<Rental> rentals = new ArrayList<>();
        rentalRepository.findAll().forEach(rentals::add);
        return rentals;
    }
}
