package uk.gov.ons.census.action.model.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Data;
import uk.gov.ons.census.action.model.entity.CaseMetadata;

@Data
public class CollectionCase {
  private UUID id;
  private String caseRef;
  private String caseType;
  private String survey;
  private UUID collectionExerciseId;
  private Address address;
  private String actionableFrom;
  private OffsetDateTime createdDateTime;
  private OffsetDateTime lastUpdated;

  // Below this line is extra data potentially needed by Action Processor - can be ignored by RH
  private UUID actionPlanId;
  private String treatmentCode;
  private String oa;
  private String lsoa;
  private String msoa;
  private String lad;
  private String htcWillingness;
  private String htcDigital;
  private String fieldCoordinatorId;
  private String fieldOfficerId;
  private Integer ceExpectedCapacity;
  private int ceActualResponses;
  private Boolean receiptReceived;
  private RefusalType refusalReceived;
  private Boolean addressInvalid;
  private boolean handDelivery;
  private boolean skeleton;
  private CaseMetadata metadata;
  private String printBatch;
  private boolean surveyLaunched;
}
