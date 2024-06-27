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
    PaymentRepository paymentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString) {}

    @StreamListener(
        value = KafkaProcessor.INPUT,
        condition = "headers['type']=='CallCancelled'"
    )
    public void wheneverCallCancelled_CancelPay(
        @Payload CallCancelled callCancelled
    ) {
        CallCancelled event = callCancelled;
        System.out.println(
            "\n\n##### listener CancelPay : " + callCancelled + "\n\n"
        );

        // Sample Logic //
        Payment.cancelPay(event);
    }
}
//>>> Clean Arch / Inbound Adaptor
