package kr.board.entity;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import lombok.Data;

@Data
public class MemberUser extends User{

	private Member member;
	
	public MemberUser(Member mvo) {
		super(mvo.getMemID(), mvo.getMemPassword(), 
				mvo.getAuthList().stream()
				.map(auth->new SimpleGrantedAuthority(auth.getAuth()))
				.collect(Collectors.toList()));
		this.member=mvo;
		// List<AuthVO> -> Collection<SimpleGrantedAuthority>
	}
}
