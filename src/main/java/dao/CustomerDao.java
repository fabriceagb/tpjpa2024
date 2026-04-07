package dao;

import dao.generic.AbstractJpaDao;
import entity.Customer;

public class CustomerDao extends AbstractJpaDao<Long, Customer> {

    public CustomerDao(){
        super();
    }
}
