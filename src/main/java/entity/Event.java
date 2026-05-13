package entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@NamedQueries({
    @NamedQuery(name = "Event.findAll", query = "SELECT e FROM Event e"),
    @NamedQuery(name = "Event.findUpcoming", query = "SELECT e FROM Event e WHERE e.date >= CURRENT_DATE"),
    @NamedQuery(name = "Event.findByLocation", query = "SELECT e FROM Event e WHERE e.location = :location"),
    @NamedQuery(name = "Event.findByCategory", query = "SELECT e FROM Event e WHERE e.categoryEvent.id = :categoryId"),
    @NamedQuery(name = "Event.findByManager", query = "SELECT e FROM Event e WHERE e.manager.id = :managerId")
})
public class Event  implements Serializable {
    private Long id;
    private String label;
    private String description;
    private String location;
    private double price;
    private Date date;
    private int popularity;
    private int numberOfTickets;
    private boolean cancelled = false;
    private Manager manager;
    private List<Ticket> tickets = new ArrayList<Ticket>();
    private CategoryEvent categoryEvent;

    public Event() {

    }
    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    @Column(name = "DESCRIPTION", nullable = false, length = 1000000)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public int getNumberOfTickets() {
        return numberOfTickets;
    }

    public void setNumberOfTickets(int numberOfTickets) {
        this.numberOfTickets = numberOfTickets;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @ManyToOne
    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    @OneToMany(mappedBy = "event",  cascade = CascadeType.PERSIST)
    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    @ManyToOne
    public CategoryEvent getCategoryEvent() {
        return this.categoryEvent;
    }

    public void setCategoryEvent(CategoryEvent categoryEvent) {
        this.categoryEvent = categoryEvent;
    }
}
