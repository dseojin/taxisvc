package taxisvc.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

import org.springframework.transaction.annotation.Transactional;

import lombok.Data;
import taxisvc.CallApplication;
import taxisvc.domain.CallCancelled;
import taxisvc.domain.CallPlaced;

@Entity
@Table(name = "Call_table")
@Data
//<<< DDD / Aggregate Root
public class Call {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
        System.out.println("##### onPostPersist() #####");

        try{
            taxisvc.external.Payment payment = new taxisvc.external.Payment();
            payment.setCallId(callId);
            payment.setFare(distance * 10000);
            
            System.out.println("##### /payments/pay  call #####");
            // mappings goes here
            CallApplication.applicationContext
                .getBean(taxisvc.external.PaymentService.class)
                .pay(payment);

            repository().findById(callId).ifPresent(call->{
            
                call.setCallStatus("driveRequest");
                repository().save(call);
                
             });


        }catch(Exception e){
            System.out.println("##### /payments/pay  call  failed #####");

            repository().findById(callId).ifPresent(call->{
            
                call.setCallStatus("payFail");
                repository().saveAndFlush(call);
                //repository().save(call);
    
            });
        }

    }

    @PostUpdate
    @Transactional
    public void onPostUpdate() {
        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        System.out.println("##### onPostUpdate() #####");

        repository().findById(callId).ifPresent(call->{

            if("requestCancel".equals(call.getCallStatus())) {

                CallCancelled callCancelled = new CallCancelled(call);
                callCancelled.publishAfterCommit();

            }else if("driveRequest".equals(call.getCallStatus())) {

                CallPlaced callPlaced = new CallPlaced(call);
                callPlaced.publishAfterCommit();
            }

        });

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
