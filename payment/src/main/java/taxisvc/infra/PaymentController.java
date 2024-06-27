package taxisvc.infra;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import taxisvc.domain.*;

//<<< Clean Arch / Inbound Adaptor

@RestController
// @RequestMapping(value="/payments")
@Transactional
public class PaymentController {

    @Autowired
    PaymentRepository paymentRepository;

    @RequestMapping(value="/callpay", method=RequestMethod.POST) 
    public Payment pay (
        @RequestBody Payment payment,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception  {

            System.out.println("##### /payments/pay  called #####");
            paymentRepository.save(payment);

            return payment;
    }
}
//>>> Clean Arch / Inbound Adaptor
