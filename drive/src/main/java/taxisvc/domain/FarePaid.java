package taxisvc.domain;

import java.util.*;
import lombok.*;
import taxisvc.domain.*;
import taxisvc.infra.AbstractEvent;

@Data
@ToString
public class FarePaid extends AbstractEvent {

    private Long payId;
    private Long callId;
    private Object fare;
}
