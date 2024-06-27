package taxisvc.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.*;
import lombok.Data;
import taxisvc.DriveApplication;
import taxisvc.domain.DriveNotAvailavled;
import taxisvc.domain.DriveStarted;
import taxisvc.domain.DrivieEnded;

@Entity
@Table(name = "Drive_table")
@Data
//<<< DDD / Aggregate Root
public class Drive {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long driveId;

    private String driverName;

    private Long callId;

    private String driveStatus;

    private String taxiNum;

    @PostPersist
    public void onPostPersist() {
        DriveStarted driveStarted = new DriveStarted(this);
        driveStarted.publishAfterCommit();

        DriveNotAvailavled driveNotAvailavled = new DriveNotAvailavled(this);
        driveNotAvailavled.publishAfterCommit();

        DrivieEnded drivieEnded = new DrivieEnded(this);
        drivieEnded.publishAfterCommit();
    }

    public static DriveRepository repository() {
        DriveRepository driveRepository = DriveApplication.applicationContext.getBean(
            DriveRepository.class
        );
        return driveRepository;
    }

    //<<< Clean Arch / Port Method
    public static void requestDriver(FarePaid farePaid) {
        //implement business logic here:

        /** Example 1:  new item 
        Drive drive = new Drive();
        repository().save(drive);

        DriveStarted driveStarted = new DriveStarted(drive);
        driveStarted.publishAfterCommit();
        DriveNotAvailavled driveNotAvailavled = new DriveNotAvailavled(drive);
        driveNotAvailavled.publishAfterCommit();
        */

        /** Example 2:  finding and process
        
        repository().findById(farePaid.get???()).ifPresent(drive->{
            
            drive // do something
            repository().save(drive);

            DriveStarted driveStarted = new DriveStarted(drive);
            driveStarted.publishAfterCommit();
            DriveNotAvailavled driveNotAvailavled = new DriveNotAvailavled(drive);
            driveNotAvailavled.publishAfterCommit();

         });
        */

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
