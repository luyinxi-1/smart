package com.upc;

import com.upc.service.TestUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SmartTextbookApplicationTests {
    @Autowired
    private TestUserService testUserService;

    @Test
    void test1() {
        System.out.println(testUserService.list());
    }

}
