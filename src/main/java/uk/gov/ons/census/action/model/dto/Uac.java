package uk.gov.ons.census.action.model.dto;

import java.util.UUID;
import lombok.Data;

@Data
public class Uac {
  private String uacHash;
  private String uac;
  private boolean active;
  private String questionnaireId;
  private String caseType;
  private String region;
  private UUID caseId;
  private UUID collectionExerciseId;
}
