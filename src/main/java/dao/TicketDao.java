package dao;

import dao.generic.AbstractJpaDao;
import entity.Customer;
import entity.Event;
import entity.Ticket;
import jakarta.persistence.EntityTransaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TicketDao extends AbstractJpaDao<Long, Ticket> {

    public TicketDao(){
        super();
    }


    public List<Ticket> buyTicket(int numberOfTicket, Event event){
        EntityTransaction transaction = entityManager.getTransaction();
        List<Ticket> tickets = new ArrayList<Ticket>();
        try {
            transaction.begin();
            CustomerDao customerDao = new CustomerDao();
            Customer customer = customerDao.findById(1L);

            for( int i = 0; i < numberOfTicket ; i++)
            {
                Ticket  ticket =  new Ticket();
                ticket.setEvent(event);
                ticket.setNumber("TICK-" + UUID.randomUUID().toString());;
                ticket.setCustomer(customer);
                ticket.setPrice(event.getPrice());
                entityManager.persist(ticket);
                tickets.add(ticket);
                event.setNumberOfTickets(event.getNumberOfTickets() - 1);
                entityManager.persist(ticket);
                entityManager.merge(event);   
            }

            transaction.commit();
            System.out.println(numberOfTicket + " ticket(s) acheté(s) avec succès.");
            return tickets;

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            return  null;
        }
    }
}
