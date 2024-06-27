package taxisvc.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;

//<<< EDA / CQRS
@Entity
@Table(name = "CallView_table")
@Data
public class CallView {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private Long callId;
    private String callStatus;
    private String userName;
    private String driverName;
    private String taxiNum;
    private String driveStatus;
}
