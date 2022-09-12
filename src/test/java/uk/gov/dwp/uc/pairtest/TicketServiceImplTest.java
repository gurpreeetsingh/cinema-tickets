package uk.gov.dwp.uc.pairtest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.constant.TicketServiceConstants;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static uk.gov.dwp.uc.pairtest.constant.TicketServiceConstants.*;

public class TicketServiceImplTest {

    public static final long INVALID_ACCOUNT_ID = -1L;
    public static final long ACCOUNT_ID = 1L;
    public static final int NO_OF_TICKETS_1 = 1;
    public static final int NO_OF_TICKETS_2 = 2;
    public static final int NO_OF_TICKETS_21 = 21;
    public static final int TOTAL_AMOUNT_TO_PAY_20 = 20;
    public static final int TOTAL_AMOUNT_TO_PAY_30 = 30;
    public static final int TOTAL_AMOUNT_TO_PAY_40 = 40;
    public static final int TOTAL_AMOUNT_TO_PAY_60 = 60;
    public static final int TOTAL_SEATS_TO_ALLOCATE_1 = 1;
    public static final int TOTAL_SEATS_TO_ALLOCATE_2 = 2;
    public static final int TOTAL_SEATS_TO_ALLOCATE_4 = 4;

    private TicketService ticketService;

    @Mock
    private TicketPaymentService ticketPaymentService;

    @Mock
    private SeatReservationService seatReservationService;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public MockitoRule initRule = MockitoJUnit.rule();

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);

        ticketService = new TicketServiceImpl(ticketPaymentService, seatReservationService);
    }

    @Test
    public void purchaseTickets_AccountIdNotGreaterThan0_ThrowInvalidPurchaseException() {
        thrown.expect(InvalidPurchaseException.class);
        thrown.expectMessage(TicketServiceConstants.INVALID_ACCOUNT_ID);

        ticketService.purchaseTickets(INVALID_ACCOUNT_ID,
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, NO_OF_TICKETS_1));
    }

    @Test
    public void purchaseTickets_InsufficientFunds_ThrowInvalidPurchaseException() {
        thrown.expect(InvalidPurchaseException.class);
        thrown.expectMessage(INSUFFICIENT_FUNDS);

        doThrow(new RuntimeException(NOT_ENOUGH_FUNDS)).when(ticketPaymentService).makePayment(ACCOUNT_ID, TOTAL_AMOUNT_TO_PAY_20);

        ticketService.purchaseTickets(ACCOUNT_ID, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, NO_OF_TICKETS_1));
    }

    @Test
    public void purchaseTickets_NoTicket_NoTicketsPurchased() {
        ticketService.purchaseTickets(ACCOUNT_ID);

        verify(ticketPaymentService, never()).makePayment(anyLong(), anyInt());
        verify(seatReservationService, never()).reserveSeat(anyLong(), anyInt());
    }

    @Test
    public void purchaseTickets_OneAdultTicket_TicketIsPurchased() {
        ticketService.purchaseTickets(ACCOUNT_ID, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, NO_OF_TICKETS_1));

        verify(ticketPaymentService).makePayment(ACCOUNT_ID, TOTAL_AMOUNT_TO_PAY_20);
        verify(seatReservationService).reserveSeat(ACCOUNT_ID, TOTAL_SEATS_TO_ALLOCATE_1);
    }

    @Test
    public void purchaseTickets_OneAdultOneChildTickets_TicketsArePurchased() {
        ticketService.purchaseTickets(ACCOUNT_ID, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, NO_OF_TICKETS_1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, NO_OF_TICKETS_1));

        verify(ticketPaymentService).makePayment(ACCOUNT_ID, TOTAL_AMOUNT_TO_PAY_30);
        verify(seatReservationService).reserveSeat(ACCOUNT_ID, TOTAL_SEATS_TO_ALLOCATE_2);
    }

    @Test
    public void purchaseTickets_TwoAdultTwoChildTickets_TicketsArePurchased() {
        ticketService.purchaseTickets(ACCOUNT_ID, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, NO_OF_TICKETS_2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2));

        verify(ticketPaymentService).makePayment(ACCOUNT_ID, TOTAL_AMOUNT_TO_PAY_60);
        verify(seatReservationService).reserveSeat(ACCOUNT_ID, TOTAL_SEATS_TO_ALLOCATE_4);
    }

    @Test
    public void purchaseTickets_AdultTicketCountGraterThan20_ThrowInvalidPurchaseException() {
        thrown.expect(InvalidPurchaseException.class);
        thrown.expectMessage(MORE_THAN_MAXIMUM_TICKETS_ALLOCATION);

        ticketService.purchaseTickets(ACCOUNT_ID, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, NO_OF_TICKETS_21));
    }

    @Test
    public void purchaseTickets_OneAdultOneInfantTickets_OneTicketIsPurchased() {
        ticketService.purchaseTickets(ACCOUNT_ID, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, NO_OF_TICKETS_1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, NO_OF_TICKETS_1));

        verify(ticketPaymentService).makePayment(ACCOUNT_ID, TOTAL_AMOUNT_TO_PAY_20);
        verify(seatReservationService).reserveSeat(ACCOUNT_ID, TOTAL_SEATS_TO_ALLOCATE_1);
    }

    @Test
    public void purchaseTickets_TwoAdultsTwoInfantsTickets_TwoTicketsArePurchased() {
        ticketService.purchaseTickets(ACCOUNT_ID, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, NO_OF_TICKETS_2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, NO_OF_TICKETS_2));

        verify(ticketPaymentService).makePayment(ACCOUNT_ID, TOTAL_AMOUNT_TO_PAY_40);
        verify(seatReservationService).reserveSeat(ACCOUNT_ID, TOTAL_SEATS_TO_ALLOCATE_2);
    }

    @Test
    public void purchaseTickets_OneChild_ThrowInvalidPurchaseException() {
        thrown.expect(InvalidPurchaseException.class);
        thrown.expectMessage(CHILD_AND_INFANT_WITHOUT_ADULT);

        ticketService.purchaseTickets(ACCOUNT_ID, new TicketTypeRequest(TicketTypeRequest.Type.CHILD, NO_OF_TICKETS_1));
    }

    @Test
    public void purchaseTickets_OneAdultOneChildOneInfant_ThrowInvalidPurchaseException() {
        thrown.expect(InvalidPurchaseException.class);
        thrown.expectMessage(CHILD_AND_INFANT_WITHOUT_ADULT);

        ticketService.purchaseTickets(ACCOUNT_ID, new TicketTypeRequest(TicketTypeRequest.Type.ADULT, NO_OF_TICKETS_1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, NO_OF_TICKETS_1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, NO_OF_TICKETS_1));
    }
}