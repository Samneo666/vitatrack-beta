package web.member.dao.impl;

import java.util.List;

import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.springframework.stereotype.Repository;


import web.checkout.vo.Orders;
import web.member.dao.MemberDao;
import web.member.vo.Member;
import web.member_admin.dto.MemberListResponse;


@Repository
public class MemberDaoImpl implements MemberDao {
	@PersistenceContext
	private Session session;
	

	@Override
	public int insert(Member member) {
		session.persist(member);
		return 1;
	}

	@Override
	public int deleteByEmail(String email) {
		final String hql = "DELETE FROM Member m WHERE m.email = :email";
		return session.createQuery(hql).setParameter("email", email).executeUpdate();
	}

	// 更新單筆會員
	@Override
	public int update(Member member) {
		session.merge(member);
		return 1;
	}

	@Override
	public Member selectByEmail(String email) {
	    final String hql = "FROM Member m WHERE m.email = :email"; 
	    return session.createQuery(hql, Member.class)
	                  .setParameter("email", email)
	                  .uniqueResult();
	}

	@Override
	public Member selectByMemberId(Integer memberId) {
		final String hql = "FROM Member m WHERE m.memberId = :memberId ";
		return session.createQuery(hql, Member.class)
				.setParameter("memberId", memberId)
				.uniqueResult();
	}

	@Override
	public List<MemberListResponse> selectAllWithPagination(int offset, int size) {
		return session.createQuery(

				"SELECT new web.member_admin.dto.MemberListResponse("
						+ "m.memberId, m.name, m.email, m.phone, m.address, m.memberStatus, m.registrationTime) "
						+ "FROM Member m "
						+ "ORDER BY m.memberId ASC",
				MemberListResponse.class)
				.setFirstResult(offset)
				.setMaxResults(size)
				.getResultList();

	}

	@Override
	public long countAllMembers() {
		
		 return session.createQuery(
		        "SELECT COUNT(m) FROM Member m", 
		        Long.class)
		        .getSingleResult();
	}

	@Override
	public List<MemberListResponse> searchMemberWithPagination(String keyword, int offset, int size) {
		
		 return session.createQuery(
				"SELECT new web.member_admin.dto.MemberListResponse("
						+ "m.memberId, m.name, m.email, m.phone, m.address, m.memberStatus, m.registrationTime) "
						+ "FROM Member m "
						+ "WHERE m.name LIKE :keyword " 
						+ "OR m.phone LIKE :keyword "
						+ "OR m.address LIKE :keyword "
						+ "ORDER BY m.memberId ",
				MemberListResponse.class)
				.setParameter("keyword", "%" + keyword + "%")
				.setFirstResult(offset)
				.setMaxResults(size)
				.getResultList();

	}

	@Override
	public long countMemberByKeyword(String keyword) {
		
		 return session.createQuery(
		        "SELECT COUNT(m) FROM Member m "
				+ "WHERE m.name LIKE :keyword " 
				+ "OR m.phone LIKE :keyword "
				+ "OR m.address LIKE :keyword " ,
		        Long.class)
				 .setParameter("keyword", "%" + keyword + "%")
				 .getSingleResult();
	}

	public List<Orders> selectAllOrdersWithPagination(Integer memberId, int offset, int size) {

	    final String hql = "FROM Orders o WHERE o.memberId = :memberId ORDER BY o.orderId ASC";

	    return session.createQuery(hql, Orders.class)
	            .setParameter("memberId", memberId)
	            .setFirstResult(offset)
	            .setMaxResults(size)
	            .getResultList();
	}

	@Override
	public long countAllOrdersById(Integer memberId){

	    return session.createQuery(
	            "SELECT COUNT(o) FROM Orders o WHERE o.memberId = :memberId",
	            Long.class)
	            .setParameter("memberId", memberId)
	            .getSingleResult();
	}

	@Override
	public List<Object[]> selectAllCartItems(Integer memberId) {
	    final String hql = "SELECT c, p FROM CartItem c "
	    		+ "JOIN Product p ON c.sku = p.sku "
	    		+ "WHERE c.memberId = :memberId ORDER BY c.cartItemId ASC";
	    return session.createQuery(hql,Object[].class)
	    		.setParameter("memberId", memberId)
	    		.getResultList();
	}

	@Override
	public int updateStatusByEmail(Integer memberStatus, String email) {
		return session.createQuery("UPDATE Member m "
				+ "SET m.memberStatus = :memberStatus " 
				+ "where m.email = :email ") 
				.setParameter("memberStatus", memberStatus) 
				.setParameter("email", email)
				.executeUpdate();
	}

	@Override
	public Member selectByPhone(String phone) {
		return session.createQuery("FROM Member m WHERE m.phone = :phone",Member.class)
		.setParameter("phone", phone) 
		.uniqueResult();
	}

}
