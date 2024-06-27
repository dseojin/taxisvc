package taxisvc.infra;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.naming.NameParser;
import javax.naming.NameParser;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import taxisvc.config.kafka.KafkaProcessor;
import taxisvc.domain.*;

//<<< Clean Arch / Inbound Adaptor
@Service
@Transactional
public class PolicyHandler {

    @Autowired
    CallRepository callRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='DriveNotAvailavled'"
    )
    public void wheneverDriveNotAvailavled_CancelCall(
        @Payload DriveNotAvailavled driveNotAvailavled
    ) {
        DriveNotAvailavled event = driveNotAvailavled;
        System.out.println(
            "\n\n##### listener CancelCall : " + driveNotAvailavled + "\n\n"
        );

        // Sample Logic //
        Call.cancelCall(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='DriveStarted'"
    )
    public void wheneverDriveStarted_ChangeCallStatus(
        @Payload DriveStarted driveStarted
    ) {
        DriveStarted event = driveStarted;
        System.out.println(
            "\n\n##### listener ChangeCallStatus : " + driveStarted + "\n\n"
        );

        // Sample Logic //
        Call.changeCallStatus(event);
    }

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='DrivieEnded'"
    )
    public void wheneverDrivieEnded_ChangeCallStatus(
        @Payload DrivieEnded drivieEnded
    ) {
        DrivieEnded event = drivieEnded;
        System.out.println(
            "\n\n##### listener ChangeCallStatus : " + drivieEnded + "\n\n"
        );

        // Sample Logic //
        Call.changeCallStatus(event);
    }
}
//>>> Clean Arch / Inbound Adaptor
