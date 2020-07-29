package uk.gov.ons.census.action.service;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.action.messaging.FulfilmentRequestReceiver;
import uk.gov.ons.census.action.model.dto.FulfilmentRequestDTO;
import uk.gov.ons.census.action.model.entity.ActionType;
import uk.gov.ons.census.action.model.entity.Case;
import uk.gov.ons.census.action.model.entity.FulfilmentToProcess;
import uk.gov.ons.census.action.model.repository.FulfilmentToProcessRepository;

@Service
public class FulfilmentRequestService {
  private static final Logger log = LoggerFactory.getLogger(FulfilmentRequestReceiver.class);
  private final FulfilmentToProcessRepository fulfilmentToProcessRepository;
  private static final Set<String> paperQuestionnaireFulfilmentCodes =
      Set.of(
          "P_OR_H1",
          "P_OR_H2",
          "P_OR_H2W",
          "P_OR_H4",
          "P_OR_HC1",
          "P_OR_HC2",
          "P_OR_HC2W",
          "P_OR_HC4",
          "P_OR_I1",
          "P_OR_I2",
          "P_OR_I2W",
          "P_OR_I4");

  public FulfilmentRequestService(FulfilmentToProcessRepository fulfilmentToProcessRepository) {
    this.fulfilmentToProcessRepository = fulfilmentToProcessRepository;
  }

  public void processEvent(
      FulfilmentRequestDTO fulfilmentRequest, Case caze, ActionType actionType) {
    checkMandatoryFields(fulfilmentRequest, caze);
    saveFulfilmentToSend(caze, actionType, fulfilmentRequest);
  }

  private static void checkMandatoryFields(FulfilmentRequestDTO fulfilmentRequest, Case caze) {
    /*
    Throws a RuntimeException if the case does not have the minimum data according to the mandatory fields listed here
    https://collaborate2.ons.gov.uk/confluence/display/SDC/Handle+New+Address+Reported+Events
     */
    Map<String, Object> mandatoryValues = new HashMap<>();
    mandatoryValues.put("addressLine1", caze.getAddressLine1());
    mandatoryValues.put("postcode", caze.getPostcode());
    mandatoryValues.put("townName", caze.getTownName());

    if (!isPaperQuestionnaireFulfilment(fulfilmentRequest) && caze.isHandDelivery()) {
      // Non PQ fulfilments which are hand delivered need a field officer and coordinator
      mandatoryValues.put("fieldCoordinatorId", caze.getFieldCoordinatorId());
      mandatoryValues.put("fieldOfficerId", caze.getFieldOfficerId());
    }

    List<String> missingFields = new ArrayList<>();
    for (Map.Entry<String, Object> entry : mandatoryValues.entrySet()) {
      if (entry.getValue() == null) {
        missingFields.add(entry.getKey());
      }
    }
    if (!missingFields.isEmpty()) {
      throw new RuntimeException(
          String.format(
              "Received fulfilment request for case which is missing mandatory values: %s, fulfilmentCode: %s, caseId: %s",
              missingFields.toString(), fulfilmentRequest.getFulfilmentCode(), caze.getCaseId()));
    }
  }

  private static boolean isPaperQuestionnaireFulfilment(FulfilmentRequestDTO fulfilmentRequest) {
    return paperQuestionnaireFulfilmentCodes.contains(fulfilmentRequest.getFulfilmentCode());
  }

  public ActionType determineActionType(String fulfilmentCode) {

    // These are currently not added as Enums, as not known.
    switch (fulfilmentCode) {
      case "P_OR_H1":
      case "P_OR_H2":
      case "P_OR_H2W":
      case "P_OR_H4":
      case "P_OR_HC1":
      case "P_OR_HC2":
      case "P_OR_HC2W":
      case "P_OR_HC4":
        return ActionType.P_OR_HX;
      case "P_UAC_UACHHP1":
      case "P_UAC_UACHHP2B":
      case "P_UAC_UACHHP4":
        return ActionType.P_UAC_HX;
      case "P_LP_HL1":
      case "P_LP_HL2":
      case "P_LP_HL2W":
      case "P_LP_HL4":
        return ActionType.P_LP_HLX;
      case "P_LP_ILP1":
      case "P_LP_ILP2":
      case "P_LP_ILP2W":
      case "P_LP_IL4":
        return ActionType.P_LP_ILX;
      case "P_ER_ILER1":
      case "P_ER_ILER2B":
        return ActionType.P_ER_IL;
      case "P_TB_TBALB1":
      case "P_TB_TBAMH1":
      case "P_TB_TBARA1":
      case "P_TB_TBARA2":
      case "P_TB_TBARA4":
      case "P_TB_TBARM1":
      case "P_TB_TBBEN1":
      case "P_TB_TBBEN2":
      case "P_TB_TBBOS1":
      case "P_TB_TBBUL1":
      case "P_TB_TBBUL2":
      case "P_TB_TBBUL4":
      case "P_TB_TBBUR1":
      case "P_TB_TBCAN1":
      case "P_TB_TBCAN2":
      case "P_TB_TBCAN4":
      case "P_TB_TBCZE1":
      case "P_TB_TBCZE4":
      case "P_TB_TBFAR1":
      case "P_TB_TBFAR2":
      case "P_TB_TBFRE1":
      case "P_TB_TBGER1":
      case "P_TB_TBGRE1":
      case "P_TB_TBGRE2":
      case "P_TB_TBGUJ1":
      case "P_TB_TBPAN1":
      case "P_TB_TBPAN2":
      case "P_TB_TBHEB1":
      case "P_TB_TBHIN1":
      case "P_TB_TBHUN1":
      case "P_TB_TBHUN4":
      case "P_TB_TBIRI4":
      case "P_TB_TBITA1":
      case "P_TB_TBITA2":
      case "P_TB_TBJAP1":
      case "P_TB_TBKOR1":
      case "P_TB_TBKUR1":
      case "P_TB_TBKUR2":
      case "P_TB_TBLAT1":
      case "P_TB_TBLAT2":
      case "P_TB_TBLAT4":
      case "P_TB_TBLIN1":
      case "P_TB_TBLIT1":
      case "P_TB_TBLIT4":
      case "P_TB_TBMAL1":
      case "P_TB_TBMAL2":
      case "P_TB_TBMAN1":
      case "P_TB_TBMAN2":
      case "P_TB_TBMAN4":
      case "P_TB_TBNEP1":
      case "P_TB_TBPAS1":
      case "P_TB_TBPAS2":
      case "P_TB_TBPOL1":
      case "P_TB_TBPOL2":
      case "P_TB_TBPOL4":
      case "P_TB_TBPOR1":
      case "P_TB_TBPOR2":
      case "P_TB_TBPOR4":
      case "P_TB_TBPOT1":
      case "P_TB_TBROM1":
      case "P_TB_TBROM4":
      case "P_TB_TBRUS1":
      case "P_TB_TBRUS2":
      case "P_TB_TBRUS4":
      case "P_TB_TBSLE1":
      case "P_TB_TBSLO1":
      case "P_TB_TBSLO4":
      case "P_TB_TBSOM1":
      case "P_TB_TBSOM4":
      case "P_TB_TBSPA1":
      case "P_TB_TBSPA2":
      case "P_TB_TBSWA1":
      case "P_TB_TBSWA2":
      case "P_TB_TBTAG1":
      case "P_TB_TBTAM1":
      case "P_TB_TBTHA1":
      case "P_TB_TBTHA2":
      case "P_TB_TBTET4":
      case "P_TB_TBTIG1":
      case "P_TB_TBTUR1":
      case "P_TB_TBUKR1":
      case "P_TB_TBULS4":
      case "P_TB_TBURD1":
      case "P_TB_TBVIE1":
      case "P_TB_TBYSH1":
        return ActionType.P_TB_TBX;
      case "UACHHT1":
      case "UACHHT2":
      case "UACHHT2W":
      case "UACHHT4":
      case "UACIT1":
      case "UACIT2":
      case "UACIT2W":
      case "UACIT4":
      case "UACCET1":
      case "UACCET2":
      case "UACCET2W":
      case "RM_TC_HI":
      case "RM_TC":
        return null; // Ignore SMS and RM internal fulfilments
      case "P_OR_I1":
      case "P_OR_I2":
      case "P_OR_I2W":
      case "P_OR_I4":
        return ActionType.P_OR_IX;
      case "P_UAC_UACIP1":
      case "P_UAC_UACIP2B":
      case "P_UAC_UACIP4":
      case "P_UAC_UACIPA1":
      case "P_UAC_UACIPA2B":
      case "P_UAC_UACIPA4":
        return ActionType.P_UAC_IX;
      case "P_UAC_UACCEP1":
      case "P_UAC_UACCEP2B":
        return ActionType.P_UAC_CX;
      default:
        log.with("fulfilmentCode", fulfilmentCode).warn("Unexpected fulfilment code received");
        return null;
    }
  }

  private void saveFulfilmentToSend(
      Case fulfilmentCase, ActionType actionType, FulfilmentRequestDTO fulfilmentRequest) {
    FulfilmentToProcess fulfilmentToProcess = new FulfilmentToProcess();
    fulfilmentToProcess.setCaze(fulfilmentCase);
    fulfilmentToProcess.setAddressLine1(fulfilmentCase.getAddressLine1());
    fulfilmentToProcess.setAddressLine2(fulfilmentCase.getAddressLine2());
    fulfilmentToProcess.setAddressLine3(fulfilmentCase.getAddressLine3());
    fulfilmentToProcess.setTownName(fulfilmentCase.getTownName());
    fulfilmentToProcess.setPostcode(fulfilmentCase.getPostcode());
    fulfilmentToProcess.setTitle(fulfilmentRequest.getContact().getTitle());
    fulfilmentToProcess.setForename(fulfilmentRequest.getContact().getForename());
    fulfilmentToProcess.setSurname(fulfilmentRequest.getContact().getSurname());
    fulfilmentToProcess.setFieldCoordinatorId(fulfilmentCase.getFieldCoordinatorId());
    fulfilmentToProcess.setFieldOfficerId(fulfilmentCase.getFieldOfficerId());
    fulfilmentToProcess.setOrganisationName(fulfilmentCase.getOrganisationName());
    fulfilmentToProcess.setFulfilmentCode(fulfilmentRequest.getFulfilmentCode());
    fulfilmentToProcess.setActionType(actionType);
    fulfilmentToProcessRepository.saveAndFlush(fulfilmentToProcess);
  }
}
