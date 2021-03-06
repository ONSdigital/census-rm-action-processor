package uk.gov.ons.census.action.model.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.ons.census.action.model.entity.FulfilmentToProcess;

public interface FulfilmentToProcessRepository extends JpaRepository<FulfilmentToProcess, Long> {

  @Query("SELECT DISTINCT f.fulfilmentCode FROM FulfilmentToProcess f")
  List<String> findDistinctFulfilmentCode();
}
