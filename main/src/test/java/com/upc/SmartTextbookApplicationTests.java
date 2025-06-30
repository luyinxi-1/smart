package com.upc;

import com.upc.modular.student.entity.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@SpringBootTest
class SmartTextbookApplicationTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void test1() {
        String key = "student:101";
        Student student = new Student();
        student.setName("zhangsan");
        student.setEmail("13211563@qq.com");
        student.setPhone("13245687511");

        // 2. 将 Student 对象存入 Redis
        // Jackson 会自动将其转换为 JSON 字符串
        System.out.println("正在存入 Student 对象: " + student);
        redisTemplate.opsForValue().set(key, student);
        System.out.println("存入成功！");

        // 3. 从 Redis 中取出数据
        System.out.println("正在读取 Student 对象...");
        Object retrievedObject = redisTemplate.opsForValue().get(key);
        System.out.println("读取到的原始对象: " + retrievedObject);
        System.out.println("原始对象的类型: " + retrievedObject.getClass().getName());

        // 4. 将取出的对象转换回 Student 类型
        // 注意：因为 RedisTemplate<Object, Object> 的泛型是 Object，
        // Jackson 默认会把 JSON 对象反序列化成一个 LinkedHashMap。
        // 我们需要手动将这个 Map 转换成我们期望的 Student 对象。
        if (retrievedObject instanceof Student) {
            // 如果序列化器配置得非常好（比如使用了 GenericJackson2JsonRedisSerializer 并处理了类型信息），可能直接就是 Student 类型
            System.out.println("直接是 Student 类型！" + retrievedObject);
        } else if (retrievedObject instanceof java.util.Map) {
            System.out.println("需要手动序列化");
        }

        System.out.println("====== 测试结束 ======");

    }

}
