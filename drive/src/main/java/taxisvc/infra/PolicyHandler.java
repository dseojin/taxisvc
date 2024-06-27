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
    DriveRepository driveRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='FarePaid'"
    )
    public void wheneverFarePaid_RequestDriver(@Payload FarePaid farePaid) {
        FarePaid event = farePaid;
        System.out.println(
            "\n\n##### listener RequestDriver : " + farePaid + "\n\n"
        );

        // Sample Logic //
        Drive.requestDriver(event);
    }
}
//>>> Clean Arch / Inbound Adaptor
