package taxisvc.infra;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import taxisvc.config.kafka.KafkaProcessor;
import taxisvc.domain.*;

@Service
public class CallViewViewHandler {

    //<<< DDD / CQRS
    @Autowired
    private CallViewRepository callViewRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenCallPlaced_then_CREATE_1(@Payload CallPlaced callPlaced) {
        try {
            if (!callPlaced.validate()) return;

            // view 객체 생성
            CallView callView = new CallView();
            // view 객체에 이벤트의 Value 를 set 함
            callView.setCallId(callPlaced.getCallId());
            callView.setCallStatus(callPlaced.getCallStatus());
            callView.setUserName(callPlaced.getUserName());
            // view 레파지 토리에 save
            callViewRepository.save(callView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenCallCancelled_then_UPDATE_1(
        @Payload CallCancelled callCancelled
    ) {
        try {
            if (!callCancelled.validate()) return;
            // view 객체 조회

            List<CallView> callViewList = callViewRepository.findByCallId(
                callCancelled.getCallId()
            );
            for (CallView callView : callViewList) {
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                callView.setCallStatus(callCancelled.getCallStatus());
                // view 레파지 토리에 save
                callViewRepository.save(callView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenDriveStarted_then_UPDATE_2(
        @Payload DriveStarted driveStarted
    ) {
        try {
            if (!driveStarted.validate()) return;
            // view 객체 조회

            List<CallView> callViewList = callViewRepository.findByCallId(
                driveStarted.getCallId()
            );
            for (CallView callView : callViewList) {
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                callView.setDriverName(driveStarted.getDriverName());
                callView.setTaxiNum(driveStarted.getTaxiNum());
                callView.setDriveStatus(driveStarted.getDriveStatus());
                // view 레파지 토리에 save
                callViewRepository.save(callView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenDrivieEnded_then_UPDATE_3(
        @Payload DrivieEnded drivieEnded
    ) {
        try {
            if (!drivieEnded.validate()) return;
            // view 객체 조회

            List<CallView> callViewList = callViewRepository.findByCallId(
                drivieEnded.getCallId()
            );
            for (CallView callView : callViewList) {
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                callView.setDriveStatus(drivieEnded.getDriveStatus());
                // view 레파지 토리에 save
                callViewRepository.save(callView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenDriveNotAvailavled_then_UPDATE_4(
        @Payload DriveNotAvailavled driveNotAvailavled
    ) {
        try {
            if (!driveNotAvailavled.validate()) return;
            // view 객체 조회

            List<CallView> callViewList = callViewRepository.findByCallId(
                driveNotAvailavled.getCallId()
            );
            for (CallView callView : callViewList) {
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                callView.setDriveStatus(driveNotAvailavled.getDriveStatus());
                // view 레파지 토리에 save
                callViewRepository.save(callView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenDriveNotAvailavled_then_DELETE_1(
        @Payload DriveNotAvailavled driveNotAvailavled
    ) {
        try {
            if (!driveNotAvailavled.validate()) return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //>>> DDD / CQRS
}
