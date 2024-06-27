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
//<<< DDD / Aggregate Root
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long payId;

    private Long callId;

    private BigDecimal fare;

    // public Long getPayId() {
    //     return payId;
    // }

    // public void setPayId(Long payId) {
    //     this.payId = payId;
    // }

    // public Long getCallId() {
    //     return callId;
    // }

    // public void setCallId(Long callId) {
    //     this.callId = callId;
    // }

    // public BigDecimal getFare() {
    //     return fare;
    // }

    // public void setFare(BigDecimal fare) {
    //     this.fare = fare;
    // }

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
