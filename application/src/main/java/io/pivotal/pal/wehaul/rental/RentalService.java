package io.pivotal.pal.wehaul.rental;

import io.pivotal.pal.wehaul.rental.domain.*;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class RentalService {

    private final TruckSizeLookupClient truckSizeLookupClient;
    private final RentalTruckRepository rentalTruckRepository;

    public RentalService(TruckSizeLookupClient truckSizeLookupClient,
                         RentalTruckRepository rentalTruckRepository) {
        this.truckSizeLookupClient = truckSizeLookupClient;
        this.rentalTruckRepository = rentalTruckRepository;
    }

    public void reserve(String customerName) {
        RentalTruck truck = rentalTruckRepository.findTop1ByStatus(RentalTruckStatus.RENTABLE);
        if (truck == null) {
            throw new IllegalStateException("No trucks available to rent");
        }

        truck.reserve(customerName);

        rentalTruckRepository.save(truck);
    }

    public void pickUp(UUID confirmationNumber) {
        RentalTruck rentalTruck = rentalTruckRepository.findOneByRentalConfirmationNumber(confirmationNumber);
        if (rentalTruck == null) {
            throw new IllegalArgumentException(String.format("No rental found for id=%s", confirmationNumber));
        }

        rentalTruck.pickUp();

        rentalTruckRepository.save(rentalTruck);
    }

    public void dropOff(UUID confirmationNumber, int distanceTraveled) {
        RentalTruck rentalTruck = rentalTruckRepository.findOneByRentalConfirmationNumber(confirmationNumber);
        if (rentalTruck == null) {
            throw new IllegalArgumentException(String.format("No rental found for id=%s", confirmationNumber));
        }

        rentalTruck.dropOff(distanceTraveled);

        rentalTruckRepository.save(rentalTruck);
    }

    public Collection<RentalTruck> findAll() {

        return StreamSupport
                .stream(rentalTruckRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    public void addTruck(String vin, String make, String model) {
        RentalTruckSize truckSize = truckSizeLookupClient.getSizeByMakeModel(make, model);

        RentalTruck truck = new RentalTruck(vin, truckSize);

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
