package kr.board.controller;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

import kr.board.entity.AuthVO;
import kr.board.entity.Member;
import kr.board.entity.MemberUser;
import kr.board.mapper.MemberMapper;
import kr.board.security.MemberUserDetailsService;

@Controller
public class MemberController {
	
	@Autowired
	MemberMapper memberMapper;
	
	@Autowired
	MemberUserDetailsService memberUserDetailsService;
	
	@Autowired
	PasswordEncoder pwEncoder;
	
	@RequestMapping("/memJoin.do")
	public String memJoin() {
		return "member/join";
	}
	
	@RequestMapping("/memRegisterCheck.do")
	public @ResponseBody int memRegisterCheck(@RequestParam("memID") String memID) {
		
		Member m=memberMapper.registerCheck(memID);
		if(m!=null || memID.equals("")) {
			return 0;
		}
		
		
		return 1;
	}
	
	@RequestMapping("/memRegister.do")
	public String memRegister(Member m, String memPassword1, String memPassword2, RedirectAttributes rttr, HttpSession session) {
		if(m.getMemID()==null || m.getMemID().equals("") ||
		   memPassword1==null || memPassword1.equals("") ||
		   memPassword2==null || memPassword2.equals("") ||
		   m.getMemName()==null || m.getMemName().equals("") ||		
		   m.getMemAge()==0 || m.getAuthList().size()==0 ||		
		   m.getMemGender()==null || m.getMemGender().equals("") ||
		   m.getMemEmail()==null || m.getMemEmail().equals("")) {
		   // 누락메시지를 가지고 가기? => 객체바인딩(Model, HttpServletRequest, HttpSession
			rttr.addFlashAttribute("msgType", "실패 메시지");
			rttr.addFlashAttribute("msg", "모든 내용을 입력하시오.");
			return "redirect:/memJoin.do";
		}
		if(!memPassword1.equals(memPassword2)) {
			rttr.addFlashAttribute("msgType", "실패 메시지");
			rttr.addFlashAttribute("msg", "비밀번호가 일치하지 않음.");
			return "redirect:/memJoin.do";
		}
		m.setMemProfile("");
		
		String encyptPw=pwEncoder.encode(m.getMemPassword());
		m.setMemPassword(encyptPw);
		int result=memberMapper.register(m);	
		if(result==1) {
			//권한테이블에 회원 권한 저장하기
			List<AuthVO> list=m.getAuthList();
			for(AuthVO authVO: list) {
				if(authVO.getAuth()!=null) {
					AuthVO saveVO=new AuthVO();
					saveVO.setMemID(m.getMemID());
					saveVO.setAuth(authVO.getAuth());
					memberMapper.authInsert(saveVO);
				}
			}
			rttr.addFlashAttribute("msgType", "성공 메시지");
			rttr.addFlashAttribute("msg", "회원가입 성공!");
			
			return "redirect:/memLoginForm.do";
		}else {
			rttr.addFlashAttribute("msgType", "실패 메시지");
			rttr.addFlashAttribute("msg", "이미 존재하는 회원입니다.");
			return "redirect:/memJoin.do";
		}
	}
	

	@RequestMapping("/memLoginForm.do")
	public String memLoginFrom() {
		return "member/memLoginForm";
	}
	
	@RequestMapping("/memUpdateForm.do")
	public String memUpdateForm() {
		return "member/memUpdateForm";
	}
	@RequestMapping("/memUpdate.do")
	public String memUpdate(Member m, RedirectAttributes rttr, String memPassword1, String memPassword2, HttpSession session) {
		        if(m.getMemID()==null || m.getMemID().equals("") ||
				   memPassword1==null || memPassword1.equals("") ||
				   memPassword2==null || memPassword2.equals("") ||
				   m.getMemName()==null || m.getMemName().equals("") ||		
				   m.getMemAge()==0 || m.getAuthList().size()==0 ||	
				   m.getMemGender()==null || m.getMemGender().equals("") ||
				   m.getMemEmail()==null || m.getMemEmail().equals("")) {
				   // 누락메시지를 가지고 가기? => 객체바인딩(Model, HttpServletRequest, HttpSession
					rttr.addFlashAttribute("msgType", "실패 메시지");
					rttr.addFlashAttribute("msg", "모든 내용을 입력하시오.");
					return "redirect:/memUpdateForm.do";
				}
				if(!memPassword1.equals(memPassword2)) {
					rttr.addFlashAttribute("msgType", "실패 메시지");
					rttr.addFlashAttribute("msg", "비밀번호가 일치하지 않음.");
					return "redirect:/memUpdateForm.do";
				}
				
				String encyptPw=pwEncoder.encode(m.getMemPassword());
				m.setMemPassword(encyptPw);
				int result=memberMapper.memUpdate(m);	
				if(result==1) { //수정성공
					//기존 권한 삭제
					memberMapper.authDelete(m.getMemID());
					
					// 새로운 권한 추가
					List<AuthVO> list=m.getAuthList();
					for(AuthVO authVO: list) {
						if(authVO.getAuth()!=null) {
							AuthVO saveVO=new AuthVO();
							saveVO.setMemID(m.getMemID());
							saveVO.setAuth(authVO.getAuth());
							memberMapper.authInsert(saveVO);
						}
					}
					
					rttr.addFlashAttribute("msgType", "성공 메시지");
					rttr.addFlashAttribute("msg", "회원정보 수정 성공!");
					Member mvo=memberMapper.getMember(m.getMemID());
					session.setAttribute("mvo", mvo);
					return "redirect:/";
				}else {
					rttr.addFlashAttribute("msgType", "실패 메시지");
					rttr.addFlashAttribute("msg", "회원정보 수정 실패.");
					return "redirect:/memUpdateForm.do";
				}
				
	}
	@RequestMapping("/memImageForm.do")
	public String memImageForm() {
		return "member/memImageForm";
	}
	
	@RequestMapping("/memImageUpdate.do")
	public String memImageUpdate(HttpServletRequest request ,HttpSession session,RedirectAttributes rttr) {
		// 파일 업로드 API(cos.jar, 3가지)
		MultipartRequest multi=null;
		int fileMaxSize=10*1024*1024; //10mb
		String savePath=request.getRealPath("resources/upload"); // 실제경로 가져옴
		try {
			//이미지 업로드
			multi=new MultipartRequest(request, savePath, fileMaxSize, "UTF-8", new DefaultFileRenamePolicy()); // 1.png 또 있을경우 1_1.png로
		} catch (Exception e) {
			e.printStackTrace();
			rttr.addFlashAttribute("msgType", "실패 메시지");
			rttr.addFlashAttribute("msg", "파일의 크기는 10MB를 넘길 수 없습니다.");
			return "redirect:/memImageForm.do";
		}
		String memID=multi.getParameter("memID");
		String newProfile="";
		File file=multi.getFile("memProfile");
		if(file != null) {
			String ext=file.getName().substring(file.getName().lastIndexOf(".")+1);
			ext=ext.toUpperCase();
			if(ext.equals("PNG") || ext.equals("GIF") || ext.equals("JPG")){
				String oldProfile=memberMapper.getMember(memID).getMemProfile();
				File oldFile=new File(savePath+"/"+oldProfile);
				if(oldFile.exists()) {
					oldFile.delete();
				}
				newProfile=file.getName();
			}else {
				if(file.exists()) {
					file.delete();
				}
				rttr.addFlashAttribute("msgType", "실패 메시지");
				rttr.addFlashAttribute("msg", "이미지 파일이 아닙니다.");
				return "redirect:/memImageForm.do";
			}
		}
		Member mvo=new Member();
		mvo.setMemID(memID);
		mvo.setMemProfile(newProfile);
		memberMapper.memProfileUpdate(mvo);
		Member m=memberMapper.getMember(memID);
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		MemberUser userAccount = (MemberUser) authentication.getPrincipal();
		SecurityContextHolder.getContext().setAuthentication(createNewAuthentication(authentication,userAccount.getMember().getMemID()));
		
		session.setAttribute("mvo", m);
		rttr.addFlashAttribute("msgType", "성공 메시지");
		rttr.addFlashAttribute("msg", "이미지 변경 성공.");
		
		return "redirect:/";
	}
	
	protected Authentication createNewAuthentication(Authentication currentAuth, String username) {
	    UserDetails newPrincipal = memberUserDetailsService.loadUserByUsername(username);
	    UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(newPrincipal, currentAuth.getCredentials(), newPrincipal.getAuthorities());
	    newAuth.setDetails(currentAuth.getDetails());	    
	    return newAuth;
	}
	@GetMapping("/access-denied")
	public String showAccessDenied() {
		return "access-denied";
	}
}
