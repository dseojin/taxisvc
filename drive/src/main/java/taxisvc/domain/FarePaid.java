package taxisvc.domain;

import java.math.BigDecimal;
import java.util.*;
import lombok.*;
import taxisvc.domain.*;
import taxisvc.infra.AbstractEvent;

@Data
@ToString
public class FarePaid extends AbstractEvent {

    private Long payId;
    private Long callId;
    private BigDecimal fare;
}
