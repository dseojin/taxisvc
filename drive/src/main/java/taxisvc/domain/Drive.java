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
@SequenceGenerator(
  name = "DRIVE_SEQ_GENERATOR", 
  sequenceName = "DRIVE_SEQ", // 매핑할 데이터베이스 시퀀스 이름 
  initialValue = 1,
  allocationSize = 1)
//<<< DDD / Aggregate Root
public class Drive {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long driveId;

    private String driverName;

    private Long callId;

    private String driveStatus;

    private String taxiNum;

    private BigDecimal fare;

    @PostPersist
    @Transactional
    public void onPostPersist() {
        System.out.println("3333333333##### Driver.onPostPersist  #####");
        if("start".equals(this.getDriveStatus())) {
            System.out.println("4444444444##### Driver.onPostPersist  ##### data :::::" + this);
            DriveStarted driveStarted = new DriveStarted(this);
            driveStarted.publishAfterCommit();

        }else if("requestCancel".equals(this.getDriveStatus())) {
            System.out.println("5555555555##### Driver.onPostPersist  ##### data ::::: " + this);
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

        BigDecimal a = new BigDecimal(1000000);
        System.out.println("3333333333##### Driver.onPostPersist  #####");
        if((farePaid.getFare().compareTo(a)) != 1) {
            drive.setDriveStatus("start");
        }else {
            drive.setDriveStatus("requestCancel");;
        }

        repository().save(drive);

        System.out.println("2222222222##### Driver.requestDriver  #####" + drive);

    }
    //>>> Clean Arch / Port Method

}
//>>> DDD / Aggregate Root
