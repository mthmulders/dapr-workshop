package dapr.fines.violation;

import dapr.fines.fines.FineCalculator;
import dapr.fines.vehicle.VehicleInfo;
import dapr.fines.vehicle.VehicleRegistrationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Component
@Slf4j
public class ViolationProcessor {
    private final FineCalculator fineCalculator;
    private final VehicleRegistrationClient vehicleRegistrationClient;

    public void processSpeedingViolation(final SpeedingViolation violation) {
        var fine = fineCalculator.calculateFine(violation.excessSpeed());
        var fineText = fine == -1 ? "to be decided by the prosecutor" : String.format("EUR %.2f", (float) fine);
        var vehicleInfo = vehicleRegistrationClient.getVehicleInfo(violation.licensePlate());

        // Send notification of fine by email
        // TODO

        log.info(constructLogMessage(violation, vehicleInfo, fineText));
    }

    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("LLLL, dd y");
    private final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private String constructLogMessage(final SpeedingViolation violation, final VehicleInfo vehicleInfo, final String fineText) {
        var date = DATE_FORMAT.format(violation.timestamp());
        var time = TIME_FORMAT.format(violation.timestamp());

        return String.format("""
                        Sent fine notification
                        \t\t\tTo %s, registered owner of license plate %s.
                        \t\t\tViolation of %d km/h detected on the %s road on %s at %s.
                        \t\t\tFine: %s.%n
                        """,
                vehicleInfo.ownerName(),
                violation.licensePlate(),
                violation.excessSpeed(),
                violation.roadId(),
                date,
                time,
                fineText
        );
    }

}
