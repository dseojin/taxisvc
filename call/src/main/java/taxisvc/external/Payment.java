package taxisvc.external;

import java.util.Date;
import lombok.Data;

@Data
public class Payment {

    private Long payId;
    private Long callId;
    private Object fare;
}
