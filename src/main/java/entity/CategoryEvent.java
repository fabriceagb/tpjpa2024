package entity;

import jakarta.persistence.*;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Entity
public class CategoryEvent implements Serializable {
    private Long id;
    private String libelle;
    private List<Event> events = new ArrayList<Event> ();

    public CategoryEvent(){

    }

    @GeneratedValue
    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    @OneToMany(mappedBy ="categoryEvent",  cascade = CascadeType.PERSIST, fetch =  FetchType.LAZY)
    @JsonIgnore
    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
}
