package com.example.demo;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class UserPasswordBCrypt {

    @Test
    public void selectEquipment(){
        String rawPassword = "admin"; // 获取明文密码
        String salt = BCrypt.gensalt(); // 生成随机盐值
        String hashedPassword = BCrypt.hashpw(rawPassword, salt); // 使用哈希算法进行密码加密
        System.out.println(hashedPassword);
    }

}
