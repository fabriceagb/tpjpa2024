package dao;

import dao.generic.AbstractJpaDao;
import entity.Customer;

public class CustomerDao extends AbstractJpaDao<Long, Customer> {

    public CustomerDao(){
        super();
    }

    public Customer findById(Long id) {
        return entityManager.find(Customer.class, id);
    }
}
