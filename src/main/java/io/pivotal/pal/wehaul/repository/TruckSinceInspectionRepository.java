package io.pivotal.pal.wehaul.repository;

import io.pivotal.pal.wehaul.domain.TruckSinceInspection;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public class TruckSinceInspectionRepository {

    private static final String SQL_QUERY =
        "SELECT ti.truck_vin, t.odometer_reading - MAX(ti.odometer_reading) " +
            "FROM truck_inspection ti, truck t " +
            "WHERE t.vin = ti.truck_vin " +
            "GROUP BY ti.truck_vin, t.odometer_reading";

    private final JdbcOperations jdbcOperations;

    public TruckSinceInspectionRepository(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    public Collection<TruckSinceInspection> findAllTruckSinceInspections() {

        return jdbcOperations.query(
            SQL_QUERY,
            (rs, rowNum) -> new TruckSinceInspection(
                rs.getString(1),
                rs.getInt(2)
            )
        );
    }
}
