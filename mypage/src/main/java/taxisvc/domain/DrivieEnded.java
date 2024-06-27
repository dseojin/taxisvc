package taxisvc.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.Data;
import taxisvc.infra.AbstractEvent;

@Data
public class DrivieEnded extends AbstractEvent {

    private Long driveId;
    private String driverName;
    private Long callId;
    private String driveStatus;
    private String taxiNum;
}
