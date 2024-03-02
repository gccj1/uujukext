package com.example.Controller;

import com.example.Service.AccountService;
import com.example.entity.RestBean;
import com.example.entity.VO.RegisterVO;
import com.example.entity.VO.ForgetVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.function.Supplier;

@Validated
@RestController
@RequestMapping("/api/auth")
public class WebController {
    @Resource
    AccountService accountService;
    @PostMapping("/email-verify")
    public RestBean<Void> emailVerify(@RequestParam("email") @Email String email,
                                      @RequestParam("type") @Pattern(regexp = "(register|forget)") String type,
                                      HttpServletRequest request) {
        return messageHandle(() -> accountService.emailVerification(type, email, request.getRemoteAddr()));
    }
    @PostMapping("/register")
        public RestBean<Void> register(@Valid @RequestBody RegisterVO form) {
        return  messageHandle(() -> accountService.registerVerification(form));
    }
    @PostMapping("/forget")
    public RestBean<Void> forget(@Valid @RequestBody ForgetVO form) {
        return  messageHandle(() -> accountService.forgetVerification(form));
    }
    private RestBean<Void> messageHandle(Supplier<String> function) {
        String meg=function.get();
        return meg==null? RestBean.success():RestBean.failure(400,meg);
    }
    @GetMapping("/test")
    public  String test() {
        return "hello";
    }
}
