package taxisvc.infra;

import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import taxisvc.domain.*;

@RepositoryRestResource(collectionResourceRel = "callViews", path = "callViews")
public interface CallViewRepository
    extends PagingAndSortingRepository<CallView, Long> {
    List<CallView> findByCallId(Long callId);
}
