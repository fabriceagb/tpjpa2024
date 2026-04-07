package dao;

import dao.generic.AbstractJpaDao;
import entity.Administrator;

public class AdministratorDao extends AbstractJpaDao<Long, Administrator> {
    public AdministratorDao(){
        super();
    }
}
