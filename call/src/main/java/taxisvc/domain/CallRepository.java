package taxisvc.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import taxisvc.domain.*;

//<<< PoEAA / Repository
@RepositoryRestResource(collectionResourceRel = "calls", path = "calls")
public interface CallRepository
    extends JpaRepository<Call, Long> {}
