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

    public Long getCallId() {
        return callId;
    }

    public void setCallId(Long callId) {
        this.callId = callId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

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
    
                CallPlaced callPlaced = new CallPlaced(call);
                callPlaced.publishAfterCommit();
             });


        }catch(Exception e){
            System.out.println("##### /payments/pay  call  failed #####");

            repository().findById(callId).ifPresent(call->{
            
                call.setCallStatus("payFail");
                repository().save(call);
    
                CallCancelled callCancelled = new CallCancelled(call);
                callCancelled.publishAfterCommit();
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

            if("callCancel".equals(call.getCallStatus())) {

                CallCancelled callCancelled = new CallCancelled(call);
                callCancelled.publishAfterCommit();

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
        
        // 드라이버가 없으면
        // callStatus를 callCancel로 바꾸고
        // callCancelled 이벤트 발행
        repository().findById(Long.valueOf(driveNotAvailavled.getCallId())).ifPresent(call->{
            
            call.setCallStatus("callCancel");
            repository().save(call);

            CallCancelled callCancelled = new CallCancelled(call);
            callCancelled.publishAfterCommit();

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
