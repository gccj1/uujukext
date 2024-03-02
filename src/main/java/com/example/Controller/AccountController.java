package com.example.Controller;

import com.example.Service.AccountService;
import com.example.entity.DTO.Account;
import com.example.entity.RestBean;
import com.example.entity.VO.AccountVO;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
public class AccountController {
    @Resource
    AccountService service;
    @GetMapping("/info")
    public RestBean<AccountVO> getAccountInfo(@RequestAttribute("id") int id) {
        Account account = service.findById(id);
        AccountVO accountVO = new AccountVO();
        BeanUtils.copyProperties(account, accountVO);
        return RestBean.success(accountVO);
    }
}
