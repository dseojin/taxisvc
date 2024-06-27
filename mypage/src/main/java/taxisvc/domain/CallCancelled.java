package taxisvc.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.Data;
import taxisvc.infra.AbstractEvent;

@Data
public class CallCancelled extends AbstractEvent {

    private Long callId;
    private String userId;
    private String userName;
    private String callStatus;
    private Float distance;
}
