package entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Manager extends User {
    @EmbeddedId
    private Long id;
    List<Artist> artists  = new ArrayList<Artist>();
    public Manager() {
        super();
    }
    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @OneToMany(mappedBy = "manager", cascade = CascadeType.PERSIST)
    public List<Artist> getArtists() {
        return artists;
    }

    public void setArtists(List<Artist> artists) {
        this.artists = artists;
    }

}
