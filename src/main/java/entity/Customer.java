package entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Customer extends User {

    private List<Account> accounts = new ArrayList<Account>();
    private List<Ticket> tickets = new ArrayList<Ticket>();

    public Customer() {
        super();
    }



    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    @OneToMany(mappedBy =  "customer", cascade = CascadeType.PERSIST)
    public List<Account> getAccounts() {
        return accounts;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    @OneToMany(mappedBy =  "customer", cascade = CascadeType.PERSIST)
    public List<Ticket> getTickets() {
        return tickets;
    }
}
