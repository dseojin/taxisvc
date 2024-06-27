package taxisvc.domain;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.persistence.*;

import org.springframework.transaction.annotation.Transactional;

import lombok.Data;
import taxisvc.DriveApplication;
import taxisvc.domain.DriveNotAvailavled;
import taxisvc.domain.DriveStarted;
import taxisvc.domain.DrivieEnded;

@Entity
@Table(name = "Drive_table")
@Data
@Transactional
//<<< DDD / Aggregate Root
public class Drive {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long driveId;

    private String driverName;

    private Long callId;

    private String driveStatus;

    private String taxiNum;

    public Long getDriveId(){
        return driveId;
    }

    public void setDriveId(Long driveId){
        this.driveId = driveId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public Long getCallId() {
        return callId;
    }

    public void setCallId(Long callId) {
        this.callId = callId;
    }

    public String getDriveStatus() {
        return driveStatus;
    }

    public void setDriveStatus(String driveStatus) {
        this.driveStatus = driveStatus;
    }

    public String getTaxiNum() {
        return taxiNum;
    }

    public void seTaxiNum(String taxiNum) {
        this.taxiNum = taxiNum;
    }

    @PostPersist
    public void onPostPersist() {

        Random random = new Random();
        int r = random.nextInt(100);

        if(r >= 0 && r <= 90) {
            DriveStarted driveStarted = new DriveStarted(this);
            driveStarted.publishAfterCommit();
        }else {
            DriveNotAvailavled driveNotAvailavled = new DriveNotAvailavled(this);
            driveNotAvailavled.publishAfterCommit();
        }

    }

    @PostUpdate
    public void onPostUpdate() {

        repository().findById(driveId).ifPresent(drive->{
            
            if("end".equals(drive.getDriveStatus())) {
                DrivieEnded drivieEnded = new DrivieEnded(this);
                drivieEnded.publishAfterCommit();
           }

        });

    }

    public static DriveRepository repository() {
        DriveRepository driveRepository = DriveApplication.applicationContext.getBean(
            DriveRepository.class
        );
        return driveRepository;
    }

    //<<< Clean Arch / Port Method
    public static void requestDriver(FarePaid farePaid) {

        Drive drive = new Drive();
        drive.setCallId(farePaid.getCallId());
        drive.setDriveStatus("start");
        repository().save(drive);

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
