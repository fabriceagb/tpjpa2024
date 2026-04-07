package dao;

import dao.generic.AbstractJpaDao;
import entity.Manager;

public class ManagerDao extends AbstractJpaDao<Long, Manager> {
    public ManagerDao(){
        super();
    }
}
