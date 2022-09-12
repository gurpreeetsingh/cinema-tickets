package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;

import static uk.gov.dwp.uc.pairtest.constant.TicketServiceConstants.*;

public class TicketServiceImpl implements TicketService {

    private TicketPaymentService ticketPaymentService;

    private SeatReservationService seatReservationService;

    TicketServiceImpl(TicketPaymentService ticketPaymentService, SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;
    }

    /**
     * Should only have private methods other than the one below.
     */
    @Override
    public void purchaseTickets(long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        if (accountId < 0) {
            throw new InvalidPurchaseException(INVALID_ACCOUNT_ID);
        }

        if (ticketTypeRequests.length > 0) {
            try {
                verifyChildAndInfantWithoutAdult(ticketTypeRequests);
                verifyMoreThanMaximumTicketsAllocation(ticketTypeRequests);
                seatReservationService.reserveSeat(accountId, getTotalSeatsToAllocate(ticketTypeRequests));
                ticketPaymentService.makePayment(accountId, getTotalAmountToPay(ticketTypeRequests));
            } catch (RuntimeException e) {
                handleRuntimeException(e);
            }
        }
    }

    private void verifyChildAndInfantWithoutAdult(TicketTypeRequest[] ticketTypeRequests) {
        int totalInfantAndChildSeats = getTotalInfantAndChildSeats(ticketTypeRequests);
        int totalAdultSeats = getTotalAdultSeats(ticketTypeRequests);
        if (totalInfantAndChildSeats > totalAdultSeats) {
            throw new RuntimeException(CHILD_AND_INFANT_WITHOUT_ADULT);
        }
    }

    private void verifyMoreThanMaximumTicketsAllocation(TicketTypeRequest[] ticketTypeRequests) {
        int totalSeatsToAllocate = getTotalSeatsToAllocate(ticketTypeRequests);
        if (totalSeatsToAllocate > 20) {
            throw new RuntimeException(MORE_THAN_MAXIMUM_TICKETS_ALLOCATION);
        }
    }

    private int getTotalInfantAndChildSeats(TicketTypeRequest[] ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .filter(ticketTypeRequest -> ticketTypeRequest.getTicketType() != Type.ADULT)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
    }

    private int getTotalAdultSeats(TicketTypeRequest[] ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .filter(ticketTypeRequest -> ticketTypeRequest.getTicketType() == Type.ADULT)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
    }

    private int getTotalSeatsToAllocate(TicketTypeRequest[] ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .filter(ticketTypeRequest -> ticketTypeRequest.getTicketType() != Type.INFANT)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
    }

    private int getTotalAmountToPay(TicketTypeRequest[] ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .filter(ticketTypeRequest -> ticketTypeRequest.getTicketType() != Type.INFANT)
                .mapToInt(ticketTypeRequest ->
                        ticketTypeRequest.getNoOfTickets() * getPriceForTicketType(ticketTypeRequest.getTicketType()))
                .sum();
    }

    private int getPriceForTicketType(Type ticketType) {
        // Price is in Pounds
        switch (ticketType) {
            case ADULT:
                return 20;
            case CHILD:
                return 10;
            default:
                return 0;
        }
    }

    private static void handleRuntimeException(RuntimeException e) {
        switch (e.getMessage()) {
            case NOT_ENOUGH_FUNDS:
                throw new InvalidPurchaseException(INSUFFICIENT_FUNDS);
            case MORE_THAN_MAXIMUM_TICKETS_ALLOCATION:
                throw new InvalidPurchaseException(MORE_THAN_MAXIMUM_TICKETS_ALLOCATION);
            case CHILD_AND_INFANT_WITHOUT_ADULT:
                throw new InvalidPurchaseException(CHILD_AND_INFANT_WITHOUT_ADULT);
            default:
                throw new InvalidPurchaseException(UNKNOWN_EXCEPTION);
        }
    }
}
