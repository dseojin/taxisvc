package taxisvc.domain;

import java.time.LocalDate;
import java.util.*;
import lombok.*;
import taxisvc.domain.*;
import taxisvc.infra.AbstractEvent;

//<<< DDD / Domain Event
@Data
@ToString
public class CallPlaced extends AbstractEvent {

    private Long callId;
    private String userId;
    private String userName;
    private String callStatus;
    private Float distance;

    public CallPlaced(Call aggregate) {
        super(aggregate);
    }

    public CallPlaced() {
        super();
    }
}
//>>> DDD / Domain Event
