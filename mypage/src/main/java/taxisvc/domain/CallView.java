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
@SequenceGenerator(
  name = "CALLVIEW_SEQ_GENERATOR", 
  sequenceName = "CALLVIEW_SEQ", // 매핑할 데이터베이스 시퀀스 이름 
  initialValue = 1,
  allocationSize = 1)
public class CallView {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private Long id;

    private Long callId;
    private Long driveId;
    private String callStatus;
    private String userName;
    private String driverName;
    private String taxiNum;
    private String driveStatus;
}
