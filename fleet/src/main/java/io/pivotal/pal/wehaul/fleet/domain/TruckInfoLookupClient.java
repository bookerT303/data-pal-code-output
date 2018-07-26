package io.pivotal.pal.wehaul.fleet.domain;

public interface TruckInfoLookupClient {

    MakeModel getMakeModelByVin(String vin);
}
