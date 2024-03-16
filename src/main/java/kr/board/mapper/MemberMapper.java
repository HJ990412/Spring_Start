package kr.board.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import kr.board.entity.AuthVO;
import kr.board.entity.Board;
import kr.board.entity.Member;

@Mapper //- Mybatis API
public interface MemberMapper {
	
	public Member registerCheck(String memID);
	public int register(Member m);
	public Member memLogin(String username);
	public int memUpdate(Member mvo);
	public Member getMember(String memID);
	public void memProfileUpdate(Member mvo);
	public void authInsert(AuthVO saveVO);
	public void authDelete(String memID);
	
}
