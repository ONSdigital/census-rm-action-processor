package uk.gov.ons.census.action.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.jeasy.random.EasyRandom;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.ons.census.action.model.dto.FulfilmentRequestDTO;
import uk.gov.ons.census.action.model.entity.ActionType;
import uk.gov.ons.census.action.model.entity.Case;
import uk.gov.ons.census.action.model.entity.FulfilmentToProcess;
import uk.gov.ons.census.action.model.repository.FulfilmentToSendRepository;

@RunWith(MockitoJUnitRunner.class)
public class FulfillmentRequestServiceTest {
  @Mock RabbitTemplate rabbitTemplate;
  @Mock FulfilmentToSendRepository fulfilmentToSendRepository;

  @InjectMocks FulfilmentRequestService underTest;

  @Value("${queueconfig.outbound-exchange}")
  private String outboundExchange;

  @Value("${queueconfig.action-case-exchange}")
  private String actionCaseExchange;

  private final EasyRandom easyRandom = new EasyRandom();

  @Test
  public void testLargePrintQuestionnaireFulfilmentMappings() {
    assertThat(underTest.determineActionType("P_LP_HL1")).isEqualTo(ActionType.P_LP_HLX);
  }

  @Test
  public void testTranslationBookletFulfilmentMappings() {
    assertThat(underTest.determineActionType("P_TB_TBARA1")).isEqualTo(ActionType.P_TB_TBX);
  }

  @Test
  public void testInformationLeafletFulfilmentMappings() {
    assertThat(underTest.determineActionType("P_ER_ILER1")).isEqualTo(ActionType.P_ER_IL);
  }

  @Test
  public void testHouseholdUniqueAccessCodesViaPaper() {
    assertThat(underTest.determineActionType("P_UAC_UACHHP1")).isEqualTo(ActionType.P_UAC_HX);
  }

  @Test
  public void testIndividualPrintFulfilment() {
    FulfilmentRequestDTO fulfilmentRequestDTO = easyRandom.nextObject(FulfilmentRequestDTO.class);
    fulfilmentRequestDTO.setFulfilmentCode("P_OR_I1");
    Case caze = easyRandom.nextObject(Case.class);

    underTest.processEvent(fulfilmentRequestDTO, caze, ActionType.P_OR_IX);

    ArgumentCaptor<FulfilmentToProcess> fulfilmentToSendArgumentCaptor =
        ArgumentCaptor.forClass(FulfilmentToProcess.class);
    verify(fulfilmentToSendRepository).saveAndFlush(fulfilmentToSendArgumentCaptor.capture());

    FulfilmentToProcess actualFulfilmentToProcess = fulfilmentToSendArgumentCaptor.getValue();
    assertThat(actualFulfilmentToProcess)
        .isEqualToComparingOnlyGivenFields(
            caze, "addressLine1", "addressLine2", "addressLine3", "postcode", "townName");
    assertThat(actualFulfilmentToProcess)
        .isEqualToComparingOnlyGivenFields(
            fulfilmentRequestDTO.getContact(), "title", "forename", "surname");
    assertEquals(
        fulfilmentRequestDTO.getFulfilmentCode(), actualFulfilmentToProcess.getFulfilmentCode());
    assertEquals(ActionType.P_OR_IX, actualFulfilmentToProcess.getActionType());
  }

  @Test(expected = RuntimeException.class)
  public void testPpoFulfilmentForHandDeliverCaseMissingFieldIdsIsRejected() {
    FulfilmentRequestDTO fulfilmentRequestDTO = easyRandom.nextObject(FulfilmentRequestDTO.class);
    fulfilmentRequestDTO.setFulfilmentCode("P_TB_TBCAN1");
    Case caze = easyRandom.nextObject(Case.class);
    caze.setHandDelivery(true);
    caze.setFieldCoordinatorId(null);
    caze.setFieldOfficerId(null);

    try {
      underTest.processEvent(fulfilmentRequestDTO, caze, ActionType.P_TB_TBX);
    } catch (RuntimeException runtimeException) {
      assertThat(runtimeException.getMessage()).contains("fieldOfficerId");
      assertThat(runtimeException.getMessage()).contains("fieldCoordinatorId");
      assertThat(runtimeException.getMessage()).contains(caze.getCaseId().toString());
      assertThat(runtimeException.getMessage()).contains(fulfilmentRequestDTO.getFulfilmentCode());
      throw runtimeException;
    }
  }

  @Test(expected = RuntimeException.class)
  public void testCaseMissingMandatoryAddressFieldsIsRejected() {
    FulfilmentRequestDTO fulfilmentRequestDTO = easyRandom.nextObject(FulfilmentRequestDTO.class);
    fulfilmentRequestDTO.setFulfilmentCode("P_OR_H1");
    Case caze = easyRandom.nextObject(Case.class);
    caze.setAddressLine1(null);
    caze.setPostcode(null);

    try {
      underTest.processEvent(fulfilmentRequestDTO, caze, ActionType.P_OR_HX);
    } catch (RuntimeException runtimeException) {
      assertThat(runtimeException.getMessage()).contains("addressLine1");
      assertThat(runtimeException.getMessage()).contains("postcode");
      assertThat(runtimeException.getMessage()).contains(caze.getCaseId().toString());
      assertThat(runtimeException.getMessage()).contains(fulfilmentRequestDTO.getFulfilmentCode());
      throw runtimeException;
    }
  }
}
