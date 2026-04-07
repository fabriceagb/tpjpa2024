package dao;

import dao.generic.AbstractJpaDao;
import entity.Artist;

public class ArtistDao extends AbstractJpaDao<Long, Artist> {

    public ArtistDao(){
        super();
    }
}
