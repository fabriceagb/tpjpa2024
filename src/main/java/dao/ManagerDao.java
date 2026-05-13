package dao;

import dao.generic.AbstractJpaDao;
import entity.Manager;
import jakarta.persistence.NoResultException;

public class ManagerDao extends AbstractJpaDao<Long, Manager> {

    public ManagerDao(){
        super();
    }

    public Manager findById(Long id) {
        return entityManager.find(Manager.class, id);
    }

    public Manager findByEmail(String email) {
        try {
            return entityManager.createQuery(
                "SELECT m FROM Manager m WHERE m.email = :email", Manager.class)
                .setParameter("email", email)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
