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
@RequestMapping(value="/drives")
@Transactional
public class DriveController {

    @Autowired
    DriveRepository driveRepository;

    @PostMapping("/end")
    public void end(@RequestBody Drive drive){
        driveRepository.findById(drive.getDriveId()).ifPresent(data->{
            if("start".equals(data.getDriveStatus())) {
                data.setDriveStatus("end");

                driveRepository.save(data);
            }
        });
    }
}
//>>> Clean Arch / Inbound Adaptor
