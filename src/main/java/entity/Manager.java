package entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Manager extends User{


    List<Artist> artists  = new ArrayList<Artist>();
    public Manager() {
        super();
    }


    @OneToMany(mappedBy = "manager", cascade = CascadeType.PERSIST)
    public List<Artist> getArtists() {
        return artists;
    }

    public void setArtists(List<Artist> artists) {
        this.artists = artists;
    }

}
