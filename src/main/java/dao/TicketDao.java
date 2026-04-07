package dao;

import dao.generic.AbstractJpaDao;
import entity.Ticket;

public class TicketDao  extends AbstractJpaDao<Long, Ticket> {
    public TicketDao(){
        super();
    }
}
