package dao;

import dao.generic.AbstractJpaDao;
import entity.Account;

public class AccountDao extends AbstractJpaDao<Long, Account> {
    public AccountDao(){
        super();
    }
}
