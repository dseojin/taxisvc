package taxisvc.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;
import taxisvc.PaymentApplication;
import taxisvc.domain.FarePaid;

@Entity
@Table(name = "Payment_table")
@Data
@SequenceGenerator(
  name = "PAYMENT_SEQ_GENERATOR", 
  sequenceName = "PAYMENT_SEQ", // 매핑할 데이터베이스 시퀀스 이름 
  initialValue = 1,
  allocationSize = 1)
//<<< DDD / Aggregate Root
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long payId;

    private Long callId;

    private BigDecimal fare;

    @PostPersist
    public void onPostPersist() {
        FarePaid farePaid = new FarePaid(this);
        farePaid.publishAfterCommit();
    }

    public static PaymentRepository repository() {
        PaymentRepository paymentRepository = PaymentApplication.applicationContext.getBean(
            PaymentRepository.class
        );
        return paymentRepository;
    }

    //<<< Clean Arch / Port Method
    public static void cancelPay(CallCancelled callCancelled) {
        
        System.out.println("##### call pay cancelled~~ #####");

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
