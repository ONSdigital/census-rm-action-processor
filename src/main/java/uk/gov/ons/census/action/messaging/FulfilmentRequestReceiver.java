package uk.gov.ons.census.action.messaging;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.ons.census.action.model.dto.ResponseManagementEvent;
import uk.gov.ons.census.action.model.entity.Case;
import uk.gov.ons.census.action.model.entity.FulfilmentType;
import uk.gov.ons.census.action.model.repository.CaseRepository;
import uk.gov.ons.census.action.service.FulfilmentRequestService;

@MessageEndpoint
public class FulfilmentRequestReceiver {
  private static final Set<String> individualResponseRequestCodes =
      new HashSet<>(
          Arrays.asList(
              "P_OR_I1",
              "P_OR_I2",
              "P_OR_I2W",
              "P_OR_I4",
              "P_UAC_UACIP1",
              "P_UAC_UACIP2B",
              "P_UAC_UACIP4",
              "P_UAC_UACIPA1",
              "P_UAC_UACIPA2B",
              "P_UAC_UACIPA4"));
  private final CaseRepository caseRepository;
  private final FulfilmentRequestService fulfilmentRequestService;

  public FulfilmentRequestReceiver(
      CaseRepository caseRepository, FulfilmentRequestService fulfilmentRequestService) {
    this.caseRepository = caseRepository;
    this.fulfilmentRequestService = fulfilmentRequestService;
  }

  @Transactional
  @ServiceActivator(inputChannel = "actionFulfilmentInputChannel")
  public void receiveEvent(ResponseManagementEvent event) {
    String fulfilmentCode = event.getPayload().getFulfilmentRequest().getFulfilmentCode();

    FulfilmentType fulfilmentType =
        fulfilmentRequestService.determineFulfilmentType(fulfilmentCode);
    if (fulfilmentType == null) {
      return; // This is not a fulfilment that we need to process
    }

    Case fulfilmentCase =
        fetchFulfilmentCase(event.getPayload().getFulfilmentRequest().getCaseId());

    if (fulfilmentCase.getCaseType().equals("HH")
        && individualResponseRequestCodes.contains(fulfilmentCode)) {
      // We can't process this message until the case has been cloned from its parent case.
      // We will receive an 'enriched' case creation message including the fulfilment details
      // from Case Processor.
      return;
    }

    fulfilmentRequestService.processEvent(
        event.getPayload().getFulfilmentRequest(), fulfilmentCase, fulfilmentType);
  }

  private Case fetchFulfilmentCase(UUID caseId) {
    Optional<Case> fulfilmentCase = caseRepository.findByCaseId(caseId);
    if (fulfilmentCase.isEmpty()) {
      throw new RuntimeException(
          String.format("Cannot find case %s for fulfilment request.", caseId));
    }
    return fulfilmentCase.get();
  }
}
