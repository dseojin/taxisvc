package taxisvc.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
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
    public void onPostPersist() {
        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.

        taxisvc.external.Payment payment = new taxisvc.external.Payment();
        // mappings goes here
        CallApplication.applicationContext
            .getBean(taxisvc.external.PaymentService.class)
            .pay(payment);

        CallPlaced callPlaced = new CallPlaced(this);
        callPlaced.publishAfterCommit();

        CallCancelled callCancelled = new CallCancelled(this);
        callCancelled.publishAfterCommit();
    }

    public static CallRepository repository() {
        CallRepository callRepository = CallApplication.applicationContext.getBean(
            CallRepository.class
        );
        return callRepository;
    }

    //<<< Clean Arch / Port Method
    public static void cancelCall(DriveNotAvailavled driveNotAvailavled) {
        //implement business logic here:

        /** Example 1:  new item 
        Call call = new Call();
        repository().save(call);

        CallCancelled callCancelled = new CallCancelled(call);
        callCancelled.publishAfterCommit();
        */

        /** Example 2:  finding and process
        
        repository().findById(driveNotAvailavled.get???()).ifPresent(call->{
            
            call // do something
            repository().save(call);

            CallCancelled callCancelled = new CallCancelled(call);
            callCancelled.publishAfterCommit();

         });
        */

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public static void changeCallStatus(DriveStarted driveStarted) {
        //implement business logic here:

        /** Example 1:  new item 
        Call call = new Call();
        repository().save(call);

        */

        /** Example 2:  finding and process
        
        repository().findById(driveStarted.get???()).ifPresent(call->{
            
            call // do something
            repository().save(call);


         });
        */

    }

    //>>> Clean Arch / Port Method
    //<<< Clean Arch / Port Method
    public static void changeCallStatus(DrivieEnded drivieEnded) {
        //implement business logic here:

        /** Example 1:  new item 
        Call call = new Call();
        repository().save(call);

        */

        /** Example 2:  finding and process
        
        repository().findById(drivieEnded.get???()).ifPresent(call->{
            
            call // do something
            repository().save(call);


         });
        */

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
