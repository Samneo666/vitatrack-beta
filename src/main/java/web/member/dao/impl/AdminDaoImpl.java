package web.member.dao.impl;



import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import web.member.dao.AdminDao;

import web.member.vo.Admin;



@Repository
public class AdminDaoImpl implements AdminDao {
	
	@PersistenceContext
	private Session session;
	
	
	
	@Override
	public Admin selectByAccount(String account) {
	    final String hql = "FROM Admin a WHERE a.account = :account"; 
	    return session.createQuery(hql, Admin.class)
	                  .setParameter("account", account)
	                  .uniqueResult();
	}


}
