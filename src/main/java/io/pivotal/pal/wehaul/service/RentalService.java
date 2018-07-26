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

        truck.setStatus(TruckStatus.RESERVED);
        truckRepository.save(truck);

        Rental rental = new Rental(customerName, truck.getVin());
        return rentalRepository.save(rental);
    }

    @Transactional
    public void pickUp(UUID confirmationNumber) {
        Rental rental = rentalRepository.findOne(confirmationNumber);
        if (rental == null) {
            throw new IllegalArgumentException(String.format("No rental found for id=%s", confirmationNumber));
        }

        Truck truck = truckRepository.findOne(rental.getTruckVin());

        if (rental.getDistanceTraveled() != null) {
            throw new IllegalStateException("Rental has already been picked up");
        }

        rental.setDistanceTraveled(0);
        rentalRepository.save(rental);

        truck.setStatus(TruckStatus.RENTED);
        truckRepository.save(truck);
    }

    @Transactional
    public void dropOff(UUID confirmationNumber, int distanceTraveled) {
        Rental rental = rentalRepository.findOne(confirmationNumber);
        if (rental == null) {
            throw new IllegalArgumentException(String.format("No rental found for id=%s", confirmationNumber));
        }

        if (rental.getDistanceTraveled() == null) {
            throw new IllegalStateException("Cannot drop off before picking up rental");
        }
        if (rental.getDistanceTraveled() != 0) {
            throw new IllegalStateException("Rental is already dropped off");
        }
        rental.setDistanceTraveled(distanceTraveled);

        Truck truck = truckRepository.findOne(rental.getTruckVin());
        if (truck.getStatus() != TruckStatus.RENTED) {
            throw new IllegalStateException(
                String.format("Cannot dropOff truck while truck is %s", truck.getStatus())
            );
        }
        truck.setStatus(TruckStatus.RENTABLE);
        truck.setOdometerReading(truck.getOdometerReading() + distanceTraveled);

        truckRepository.save(truck);
        rentalRepository.save(rental);
    }

    public Collection<Rental> findAll() {
        Collection<Rental> rentals = new ArrayList<>();
        rentalRepository.findAll().forEach(rentals::add);
        return rentals;
    }
}
