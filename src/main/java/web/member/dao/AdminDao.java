package web.member.dao;


import web.member.vo.Admin;

public interface AdminDao {

	
	Admin selectByAccount(String account);

}
