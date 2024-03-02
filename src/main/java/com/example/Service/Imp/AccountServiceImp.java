package com.example.Service.Imp;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Mapper.AccountMapper;
import com.example.Service.AccountService;
import com.example.entity.DTO.Account;
import com.example.entity.VO.RegisterVO;
import com.example.entity.VO.ForgetVO;
import com.example.utils.Const;
import com.example.utils.LimitUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImp extends ServiceImpl<AccountMapper, Account> implements AccountService {
    @Resource
    AmqpTemplate amqpTemplate;
    @Resource
    StringRedisTemplate Template;
    @Resource
    LimitUtils limitUtils;
    @Resource
    PasswordEncoder encoder;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(username == null)
            throw new UsernameNotFoundException("用户名不能为空");
        Account account = findByUsernameOrEmail(username);
        if(account == null)
            throw new UsernameNotFoundException("用户不存在");
        return User.withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    @Override
    public Account findByUsernameOrEmail(String username) {
        return query().eq("username", username)
                .or().eq("email", username)
                .one();
    }

    @Override
    public Account findById(int id) {
        return query().eq("id", id).one();
    }

    @Override
    public  String emailVerification(String type, String email, String ip) {
        synchronized(ip.intern()){
            if(isEmailUsed(email) && Objects.equals(type, "register")) return "该邮箱已被注册";
            if(!isEmailUsed(email) && Objects.equals(type, "forget")) return "该邮箱未注册";
            if(isEmailLimited(ip)) return "发送过于频繁,请稍后再试";//同时封禁了同ip,执行了isEmailLimited
            Random random=new Random();
            int code=random.nextInt(899999)+100000;
            Map<String,Object> map=Map.of("type",type,"email",email,"code",code);
            amqpTemplate.convertAndSend("email",map);
            Template.opsForValue().set("EMAIL_CODE_"+type.toUpperCase()+email,code+"",2, TimeUnit.MINUTES);
            return null;
        }
    }

    @Override
    public String registerVerification(RegisterVO registerVo) {
        String email=registerVo.getEmail();
        String code=Template.opsForValue().get(Const.EMAIL_CODE_REGISTER+email);
        if(code==null) return "请先获取验证码";
        if(!code.equals(registerVo.getCode())) return "验证码错误";
        if(isEmailUsed(email)) return "该邮箱已被注册";
        if(isUsernameUsed(registerVo.getUsername())) return  "该用户名已被注册";
        String password=encoder.encode(registerVo.getPassword());
        Account account = new Account(null,registerVo.getUsername(),password,email,"user",new Date());
        if (save(account)) {
            Template.delete(Const.EMAIL_CODE_REGISTER+email);
            return null;
        }
            return "发生一些错误,请稍后再试";
    }

    @Override
    public String forgetVerification(ForgetVO forgetVO) {
        String email=forgetVO.getEmail();
        String code=Template.opsForValue().get(Const.EMAIL_CODE_FORGET+email);
        if(code==null) return "请先获取验证码";
        if(!code.equals(forgetVO.getCode())) return "验证码错误";
        if(!isEmailUsed(email)) return "该邮箱未被注册";
        String password=encoder.encode(forgetVO.getPassword());
        boolean update=update(new UpdateWrapper<Account>().set("password",password).eq("email",email));
        if(update){Template.delete(Const.EMAIL_CODE_FORGET+email);return null;}
        return "发生一些错误,请稍后再试";
    }



    private Boolean isEmailUsed(String email){
        return baseMapper.exists(new QueryWrapper<Account>().eq("email",email));
//             query().eq("email",email).count()>0;
    }
    private Boolean isUsernameUsed(String username){
         return baseMapper.exists(new QueryWrapper<Account>().eq("username",username));
     }
    private Boolean isEmailLimited(String ip){
        String key=Const.EMAIL_LIMIT+ip;
        return limitUtils.emailLimited(key,60);
    }
}
