package taxisvc.domain;

import java.util.*;
import lombok.*;
import taxisvc.domain.*;
import taxisvc.infra.AbstractEvent;

@Data
@ToString
public class DriveNotAvailavled extends AbstractEvent {

    private Long driveId;
    private String driverName;
    private Long callId;
    private String driveStatus;
    private String taxiNum;
}
