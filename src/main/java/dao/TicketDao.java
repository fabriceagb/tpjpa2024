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
    public void buyTicket(Long eventId, Long customerId) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();

            // 1. Récupérer l'événement et le client depuis la base
            Event event = entityManager.find(Event.class, eventId);
            Customer customer = entityManager.find(Customer.class, customerId);

            if (event == null || customer == null) {
                throw new IllegalArgumentException("Événement ou Client introuvable.");
            }

            // 2. Créer le ticket
            Ticket ticket = new Ticket();
            ticket.setPrice(event.getPrice()); // Le ticket prend le prix de l'événement
            ticket.setNumber("TICKET-" + System.currentTimeMillis());
            ticket.setEvent(event);
            ticket.setCustomer(customer);

            // 3. Sauvegarder
            entityManager.persist(ticket);

            transaction.commit();
            System.out.println("Ticket acheté avec succès par " + customer.getFirstName() + " pour l'événement " + event.getLabel());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}
