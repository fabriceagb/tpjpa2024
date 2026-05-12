package dao;

import dao.generic.AbstractJpaDao;
import entity.Customer;
import entity.Event;
import entity.Ticket;
import jakarta.persistence.EntityTransaction;

public class TicketDao  extends AbstractJpaDao<Long, Ticket> {

    public TicketDao(){
        super();
    }

    /**
     * LOGIQUE MÉTIER : Un client (Customer) achète un ticket pour un événement
     */
    public Ticket buyTicket(Long eventId, Long customerId) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();

            Event event = entityManager.find(Event.class, eventId);
            Customer customer = entityManager.find(Customer.class, customerId);

            if (event == null || customer == null) {
                throw new IllegalArgumentException("Événement ou Client introuvable.");
            }

            if (event.isCancelled()) {
                throw new IllegalStateException("Impossible d'acheter un billet : l'événement est annulé.");
            }

            long soldTickets = entityManager
                    .createQuery("SELECT COUNT(t) FROM Ticket t WHERE t.event.id = :eventId", Long.class)
                    .setParameter("eventId", eventId)
                    .getSingleResult();
            if (event.getNumberOfTickets() > 0 && soldTickets >= event.getNumberOfTickets()) {
                throw new IllegalStateException("Plus de billets disponibles pour cet événement.");
            }

            Ticket ticket = new Ticket();
            ticket.setPrice(event.getPrice());
            ticket.setNumber("TICKET-" + System.currentTimeMillis());
            ticket.setEvent(event);
            ticket.setCustomer(customer);

            entityManager.persist(ticket);
            transaction.commit();
            return ticket;
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw new RuntimeException("Erreur lors de l'achat du billet.", e);
        }
    }
}
