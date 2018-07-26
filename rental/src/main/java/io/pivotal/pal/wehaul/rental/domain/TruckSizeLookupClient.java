package io.pivotal.pal.wehaul.rental.domain;

public interface TruckSizeLookupClient {
    RentalTruckSize getSizeByMakeModel(String make, String model);
}
