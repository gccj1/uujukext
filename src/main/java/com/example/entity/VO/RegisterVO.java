package com.example.entity.VO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

@Data
public class RegisterVO {
    @Pattern(regexp = "^[一-龥a-zA-Z0-9]{3,10}$")
    @Length(min = 4,max = 10)
    String username;
    @Length(min = 6,max = 15)
    String password;
    @Email
    String email;
    @Length(max = 6,min = 6)
    String code;
}
