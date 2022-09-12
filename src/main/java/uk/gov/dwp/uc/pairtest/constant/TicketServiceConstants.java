package uk.gov.dwp.uc.pairtest.constant;

public final class TicketServiceConstants {

    private TicketServiceConstants() {
        // No need to instantiate the class, we can hide its constructor
    }

    public static final String INVALID_ACCOUNT_ID = "Only Account Id greater than zero are valid";
    public static final String MORE_THAN_MAXIMUM_TICKETS_ALLOCATION
            = "Only a maximum of 20 tickets that can be purchased at a time.";
    public static final String NOT_ENOUGH_FUNDS = "Not enough funds available";
    public static final String INSUFFICIENT_FUNDS = "Insufficient funds to pay for ticket(s).";
    public static final String CHILD_AND_INFANT_WITHOUT_ADULT
            = "Child and Infant tickets cannot be purchased without purchasing an Adult ticket.";
    public static final String UNKNOWN_EXCEPTION = "UNKNOWN EXCEPTION";
}
