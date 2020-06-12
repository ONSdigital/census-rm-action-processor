package uk.gov.ons.census.action.model.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uk.gov.ons.census.action.model.entity.Case;

public interface CaseRepository extends JpaRepository<Case, Long>, JpaSpecificationExecutor<Case> {
  Optional<Case> findByCaseId(UUID caseId);
}
