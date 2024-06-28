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

import java.math.BigDecimal;

@Entity
@Table(name = "Drive_table")
@Data
//<<< DDD / Aggregate Root
public class Drive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long driveId;

    private String driverName;

    private Long callId;

    private String driveStatus;

    private String taxiNum;

    private BigDecimal fare;

    @PostPersist
    @Transactional
    public void onPostPersist() {
        BigDecimal a = new BigDecimal(1000000);
        System.out.println("3333333333##### Driver.onPostPersist  #####");
        if((fare.compareTo(a)) != 1) {
            repository().findById(driveId).ifPresent(data->{
                data.setDriveStatus("start");
                repository().save(data);
            });

            System.out.println("4444444444##### Driver.onPostPersist  #####");
            DriveStarted driveStarted = new DriveStarted(this);
            driveStarted.publishAfterCommit();
        }else {
            repository().findById(driveId).ifPresent(data->{
                data.setDriveStatus("requestCancel");
                repository().save(data);
            });

            System.out.println("5555555555##### Driver.onPostPersist  #####");
            DriveNotAvailavled driveNotAvailavled = new DriveNotAvailavled(this);
            driveNotAvailavled.publishAfterCommit();
        }

    }

    @PostUpdate
    @Transactional
    public void onPostUpdate() {

        repository().findById(driveId).ifPresent(drive->{
            System.out.println("6666666666##### Driver.onPostUpdate  #####" + drive);
            if("end".equals(drive.getDriveStatus())) {
                DrivieEnded drivieEnded = new DrivieEnded(drive);
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
    @Transactional
    public static void requestDriver(FarePaid farePaid) {

        System.out.println("1111111111##### Driver.requestDriver  #####" + farePaid.getFare());
        Drive drive = new Drive();
        drive.setCallId(farePaid.getCallId());
        drive.setFare(farePaid.getFare());
        String taxiNum = String.valueOf(farePaid.getCallId() * 1111).substring(0, 4);
        drive.setTaxiNum(taxiNum);
        drive.setDriverName("driver"+taxiNum);
        repository().save(drive);

        System.out.println("2222222222##### Driver.requestDriver  #####" + drive);

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
