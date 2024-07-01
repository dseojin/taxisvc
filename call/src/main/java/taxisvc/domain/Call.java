package taxisvc.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.Data;
import taxisvc.CallApplication;
import taxisvc.domain.CallCancelled;
import taxisvc.domain.CallPlaced;

@Entity
@Table(name = "Call_table")
@Data
@SequenceGenerator(
  name = "CALL_SEQ_GENERATOR", 
  sequenceName = "CALL_SEQ", // 매핑할 데이터베이스 시퀀스 이름 
  initialValue = 1,
  allocationSize = 1)
//<<< DDD / Aggregate Root
public class Call {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long callId;

    private String userId;

    private String userName;

    private String callStatus;

    private Float distance;

    @PostPersist
    @Transactional
    public void onPostPersist() {
        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.
        System.out.println("##### onPostPersist() ##### this ::::: " + this);

        try{
            taxisvc.external.Payment payment = new taxisvc.external.Payment();
            payment.setCallId(callId);
            payment.setFare(distance * 10000);
            
            System.out.println("##### /payments/pay  call 1 #####");
            // mappings goes here
            CallApplication.applicationContext
                .getBean(taxisvc.external.PaymentService.class)
                .pay(payment);

            System.out.println("##### /payments/pay  call 2 #####");

            this.setCallStatus("driveRequest");
            repository().save(this);

            // CQRS를 위해 이벤트 발행이 필요하다
            CallPlaced callPlaced = new CallPlaced(this);
            callPlaced.publishAfterCommit();

            System.out.println("##### /payments/pay  call 3 #####");

        }catch(Exception e){
            System.out.println("##### /payments/pay  call  failed #####");

            payFailSatausSave(callId);
            
        }

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void payFailSatausSave(Long callId) {
        repository().findById(callId).ifPresent(call->{
            call.setCallStatus("payFail");
            System.out.println("##### /payments/pay  call  failed 22222 ##### call :::::" + call);
            repository().save(call);

        });
    }

    @PostUpdate
    @Transactional
    public void onPostUpdate() {
        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        System.out.println("##### onPostUpdate() ##### this :::::" +this);

        if("requestCancel".equals(this.getCallStatus())) {

            CallCancelled callCancelled = new CallCancelled(this);
            callCancelled.publishAfterCommit();

        }
        
        // repository().findById(callId).ifPresent(call->{

        //     if("requestCancel".equals(call.getCallStatus())) {

        //         CallCancelled callCancelled = new CallCancelled(call);
        //         callCancelled.publishAfterCommit();

        //     }

        // });

    }

    public static CallRepository repository() {
        CallRepository callRepository = CallApplication.applicationContext.getBean(
            CallRepository.class
        );
        return callRepository;
    }

    //<<< Clean Arch / Port Method
    @Transactional
    public static void cancelCall(DriveNotAvailavled driveNotAvailavled) {
        
        // 거리가 너무 멀면
        // callStatus를 callCancel로 바꾸고
        // callCancelled 이벤트 발행
        repository().findById(Long.valueOf(driveNotAvailavled.getCallId())).ifPresent(call->{
            
            call.setCallStatus("requestCancel");
            repository().save(call);

        });


    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    @Transactional
    public static void changeCallStatus(DriveStarted driveStarted) {
        repository().findById(Long.valueOf(driveStarted.getCallId())).ifPresent(call->{
            
            call.setCallStatus("driveStart");
            repository().save(call);

        });

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    @Transactional
    public static void changeCallStatus(DrivieEnded drivieEnded) {
        repository().findById(Long.valueOf(drivieEnded.getCallId())).ifPresent(call->{
            
            call.setCallStatus("driveComplete");
            repository().save(call);

        });

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
