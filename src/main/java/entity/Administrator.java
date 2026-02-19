package entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
@Entity
public class Administrator extends User{
    @EmbeddedId
    private Long id;
    public Administrator() {
        super();
    }

}
