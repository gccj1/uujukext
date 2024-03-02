package com.example.entity.VO;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
@Data
public class ForgetVO {
    @Length(min = 6,max = 15)
    String password;
    @Email
    String email;
    @Length(max = 6,min = 6)
    String code;
}
