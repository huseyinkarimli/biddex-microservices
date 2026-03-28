package com.biddex.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
		properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
)
@ActiveProfiles("test")
class AuthServiceApplicationTests {

	@MockBean
	private RedisTemplate<String, String> redisStringTemplate;

	@Test
	void contextLoads() {
	}
}
