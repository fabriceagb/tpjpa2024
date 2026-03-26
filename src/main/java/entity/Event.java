package entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Event {
    private Long id;
    private String label;
    private String description;
    private String location;
    private Date date;
    private double price;
    private String musicalType;
    private int popularity;
    private Manager manager;
    private List<Ticket> tickets = new ArrayList<Ticket>();

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

    public String getMusicalType() {
        return musicalType;
    }

    public void setMusicalType(String musicalType) {
        this.musicalType = musicalType;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
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
}
