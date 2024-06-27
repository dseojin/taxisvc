package taxisvc.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.*;
import taxisvc.domain.*;
import taxisvc.infra.AbstractEvent;

//<<< DDD / Domain Event
@Data
@ToString
public class DriveNotAvailavled extends AbstractEvent {

    private Long driveId;
    private String driverName;
    private Long callId;
    private String driveStatus;
    private String taxiNum;

    public DriveNotAvailavled(Drive aggregate) {
        super(aggregate);
    }

    public DriveNotAvailavled() {
        super();
    }
}
//>>> DDD / Domain Event
