package com.example.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.DTO.Account;
import com.example.entity.VO.RegisterVO;
import com.example.entity.VO.forgetVO;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AccountService extends IService<Account>, UserDetailsService {
    Account findByUsernameOrEmail(String username);
    String emailVerification(String type,String email,String ip);
    String registerVerification(RegisterVO registerVo);
    String forgetVerification(forgetVO forgetVO) ;
}
