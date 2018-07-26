package io.pivotal.pal.wehaul.service;

import io.pivotal.pal.wehaul.rental.domain.*;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class RentalService {

    private final RentalTruckRepository rentalTruckRepository;
    private final RentalRepository rentalRepository;
    private final RentalTruck.Factory rentalTruckFactory;

    public RentalService(RentalTruckRepository rentalTruckRepository,
                         RentalRepository rentalRepository,
                         RentalTruck.Factory rentalTruckFactory) {
        this.rentalTruckRepository = rentalTruckRepository;
        this.rentalRepository = rentalRepository;
        this.rentalTruckFactory = rentalTruckFactory;
    }

    @Transactional
    public RentalTruck createRental(String customerName) {
        RentalTruck truck = rentalTruckRepository.findTop1ByStatus(RentalTruckStatus.RENTABLE);
        if (truck == null) {
            throw new IllegalStateException("No trucks available to rent");
        }

        truck.reserve();
        rentalTruckRepository.save(truck);

        Rental rental = new Rental(customerName, truck.getVin());
        rentalRepository.save(rental);

        return truck;
    }

    public void pickUp(UUID confirmationNumber) {
        RentalTruck rentalTruck = rentalTruckRepository.findOneByRentalConfirmationNumber(confirmationNumber);
        if (rentalTruck == null) {
            throw new IllegalArgumentException(String.format("No rental found for id=%s", confirmationNumber));
        }

        rentalTruck.pickUp();

        rentalTruckRepository.save(rentalTruck);
    }

    @Transactional
    public RentalTruck dropOff(UUID confirmationNumber) {
        RentalTruck rentalTruck = rentalTruckRepository.findOneByRentalConfirmationNumber(confirmationNumber);
        if (rentalTruck == null) {
            throw new IllegalArgumentException(String.format("No rental found for id=%s", confirmationNumber));
        }

        rentalTruck.dropOff();
        rentalTruckRepository.save(rentalTruck);

        rentalRepository.delete(confirmationNumber);

        return rentalTruck;
    }

    public Collection<Rental> findAll() {

        return StreamSupport
            .stream(rentalRepository.findAll().spliterator(), false)
            .collect(Collectors.toList());
    }

    public void createRentableTruck(String vin, String make, String model) {
        RentalTruck truck = rentalTruckFactory.createRentableTruck(vin, make, model);
        rentalTruckRepository.save(truck);
    }

    public void removeRentableTruck(String vin) {
        RentalTruck truck = rentalTruckRepository.findOne(vin);
        if (truck == null) {
            throw new IllegalArgumentException(String.format("No truck found with vin=%s", vin));
        }

        truck.preventRenting();
        rentalTruckRepository.save(truck);
    }

    public void addRentableTruck(String vin) {
        RentalTruck truck = rentalTruckRepository.findOne(vin);
        if (truck == null) {
            throw new IllegalArgumentException(String.format("No truck found with vin=%s", vin));
        }

        truck.allowRenting();
        rentalTruckRepository.save(truck);
    }
}
