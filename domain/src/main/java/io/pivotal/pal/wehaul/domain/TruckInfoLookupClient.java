package io.pivotal.pal.wehaul.domain;

public interface TruckInfoLookupClient {

    MakeModel getMakeModelByVin(String vin);
}
