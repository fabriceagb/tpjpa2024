package dto;

import entity.Ticket;

public class TicketResponseDto {
    public Long id;
    public String number;
    public double price;
    public Long eventId;
    public String eventLabel;
    public Long customerId;
    public String customerEmail;

    public TicketResponseDto() {}

    public TicketResponseDto(Ticket ticket) {
        this.id = ticket.getId();
        this.number = ticket.getNumber();
        this.price = ticket.getPrice();
        if (ticket.getEvent() != null) {
            this.eventId = ticket.getEvent().getId();
            this.eventLabel = ticket.getEvent().getLabel();
        }
        if (ticket.getCustomer() != null) {
            this.customerId = ticket.getCustomer().getId();
            this.customerEmail = ticket.getCustomer().getEmail();
        }
    }

    public Long getId() { return id; }
    public String getNumber() { return number; }
    public double getPrice() { return price; }
    public Long getEventId() { return eventId; }
    public String getEventLabel() { return eventLabel; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerEmail() { return customerEmail; }
}
