package taxisvc.domain;

import java.util.*;
import lombok.*;
import taxisvc.domain.*;
import taxisvc.infra.AbstractEvent;

@Data
@ToString
public class CallCancelled extends AbstractEvent {

    private Long callId;
    private String userId;
    private String userName;
    private String callStatus;
    private Float distance;
}
