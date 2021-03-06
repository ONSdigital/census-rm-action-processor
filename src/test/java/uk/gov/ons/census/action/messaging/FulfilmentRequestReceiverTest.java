package uk.gov.ons.census.action.messaging;

import static org.mockito.Mockito.*;

import java.util.Optional;
import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.census.action.model.dto.ResponseManagementEvent;
import uk.gov.ons.census.action.model.entity.Case;
import uk.gov.ons.census.action.model.entity.FulfilmentType;
import uk.gov.ons.census.action.model.repository.CaseRepository;
import uk.gov.ons.census.action.service.FulfilmentRequestService;

@RunWith(MockitoJUnitRunner.class)
public class FulfilmentRequestReceiverTest {
  private static final String PRINT_INDIVIDUAL_QUESTIONNAIRE_REQUEST_ENGLAND = "P_OR_I1";
  private static final String PRINT_INDIVIDUAL_QUESTIONNAIRE_REQUEST_WALES_ENGLISH = "P_OR_I2";
  private static final String PRINT_INDIVIDUAL_QUESTIONNAIRE_REQUEST_WALES_WELSH = "P_OR_I2W";
  private static final String PRINT_INDIVIDUAL_QUESTIONNAIRE_REQUEST_NORTHERN_IRELAND = "P_OR_I4";

  @Mock private CaseRepository caseRepository;
  @Mock private FulfilmentRequestService fulfilmentRequestService;

  @InjectMocks FulfilmentRequestReceiver underTest;

  private EasyRandom easyRandom = new EasyRandom();

  @Test
  public void testReceiveEventIgnoresUnexpectedFulfilmentCode() {
    // Given
    when(fulfilmentRequestService.determineFulfilmentType(anyString())).thenReturn(null);
    caseRepositoryReturnsRandomCase();
    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);

    // When
    underTest.receiveEvent(event);

    verifyNoInteractions(caseRepository);
  }

  @Test
  public void testReceiveEventIgnoresUnwantedFulfilmentCodes() {
    // Given
    caseRepositoryReturnsRandomCase();
    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);
    event.getPayload().getFulfilmentRequest().setFulfilmentCode("UACHHT1");

    // When
    underTest.receiveEvent(event);

    verifyNoInteractions(caseRepository);
  }

  @Test
  public void testOnRequestQuestionnaireFulfilment() {
    // Given
    Case fulfilmentCase = caseRepositoryReturnsRandomCase();
    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);
    event.getPayload().getFulfilmentRequest().setFulfilmentCode("P_OR_H1");
    event.getPayload().getFulfilmentRequest().setCaseId(fulfilmentCase.getCaseId());

    when(fulfilmentRequestService.determineFulfilmentType("P_OR_H1"))
        .thenReturn(FulfilmentType.P_OR_HX);

    // When
    underTest.receiveEvent(event);

    // Then
    verify(fulfilmentRequestService, times(1))
        .processEvent(
            event.getPayload().getFulfilmentRequest(), fulfilmentCase, FulfilmentType.P_OR_HX);
  }

  @Test
  public void testOnRequestContinuationQuestionnaireFulfilment() {
    // Given
    Case fulfilmentCase = caseRepositoryReturnsRandomCase();
    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);
    event.getPayload().getFulfilmentRequest().setFulfilmentCode("P_OR_HC1");
    event.getPayload().getFulfilmentRequest().setCaseId(fulfilmentCase.getCaseId());

    when(fulfilmentRequestService.determineFulfilmentType("P_OR_HC1"))
        .thenReturn(FulfilmentType.P_OR_HX);

    // When
    underTest.receiveEvent(event);

    // Then
    verify(fulfilmentRequestService, times(1))
        .processEvent(
            event.getPayload().getFulfilmentRequest(), fulfilmentCase, FulfilmentType.P_OR_HX);
  }

  @Test
  public void testIndividualRequestIsNotIgnoredForCaseTypeSPG() {
    // Given
    Case fulfilmentCase = caseRepositoryReturnsRandomCase();
    fulfilmentCase.setCaseType("SPG");
    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);
    event.getPayload().getFulfilmentRequest().setFulfilmentCode("P_OR_I1");
    event.getPayload().getFulfilmentRequest().setCaseId(fulfilmentCase.getCaseId());

    when(fulfilmentRequestService.determineFulfilmentType("P_OR_I1"))
        .thenReturn(FulfilmentType.P_OR_HX);

    // When
    underTest.receiveEvent(event);

    // Then
    verify(fulfilmentRequestService, times(1))
        .processEvent(
            event.getPayload().getFulfilmentRequest(), fulfilmentCase, FulfilmentType.P_OR_HX);
  }

  @Test
  public void testOnRequestIndividualQuestionnaireFulfilmentEnglandCaseTypeHH() {
    testIndividualResponseRequestIsIgnoredOnHHCase(PRINT_INDIVIDUAL_QUESTIONNAIRE_REQUEST_ENGLAND);
  }

  @Test
  public void testOnRequestIndividualQuestionnaireFulfilmentWalesEnglishCaseTypeHH() {
    testIndividualResponseRequestIsIgnoredOnHHCase(
        PRINT_INDIVIDUAL_QUESTIONNAIRE_REQUEST_WALES_ENGLISH);
  }

  @Test
  public void testOnRequestIndividualQuestionnaireFulfilmentWalesWelshCaseTypeHH() {
    testIndividualResponseRequestIsIgnoredOnHHCase(
        PRINT_INDIVIDUAL_QUESTIONNAIRE_REQUEST_WALES_WELSH);
  }

  @Test
  public void testOnRequestIndividualQuestionnaireFulfilmentNorthernIrelandCaseTypeHH() {
    testIndividualResponseRequestIsIgnoredOnHHCase(
        PRINT_INDIVIDUAL_QUESTIONNAIRE_REQUEST_NORTHERN_IRELAND);
  }

  private void testIndividualResponseRequestIsIgnoredOnHHCase(String fulfilmentCode) {
    ResponseManagementEvent event = easyRandom.nextObject(ResponseManagementEvent.class);
    event.getPayload().getFulfilmentRequest().setFulfilmentCode(fulfilmentCode);
    Case caze = new Case();
    caze.setCaseType("HH");
    when(caseRepository.findByCaseId(any())).thenReturn(Optional.of(caze));
    when(fulfilmentRequestService.determineFulfilmentType(any()))
        .thenReturn(FulfilmentType.P_OR_HX);

    underTest.receiveEvent(event);
    verify(fulfilmentRequestService).determineFulfilmentType(fulfilmentCode);

    verifyNoMoreInteractions(fulfilmentRequestService);
  }

  private Case caseRepositoryReturnsRandomCase() {
    Case caze = easyRandom.nextObject(Case.class);
    when(caseRepository.findByCaseId(caze.getCaseId())).thenReturn(Optional.of(caze));
    return caze;
  }
}
