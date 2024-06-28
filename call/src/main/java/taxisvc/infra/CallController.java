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
@RequestMapping(value="/calls")
@Transactional
public class CallController {

    @Autowired
    CallRepository callRepository;

    @PostMapping("/cancel")
    public void cancle(@RequestBody Call call){
        callRepository.findById(call.getCallId()).ifPresent(data->{
            data.setCallStatus("requestCancel");

            callRepository.save(data);
        });
    }
}
//>>> Clean Arch / Inbound Adaptor
