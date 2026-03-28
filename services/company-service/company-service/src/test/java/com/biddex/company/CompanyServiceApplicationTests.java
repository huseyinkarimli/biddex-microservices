package com.biddex.company;

import com.biddex.company.client.AuthServiceClient;
import com.biddex.company.event.CompanyKafkaProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CompanyServiceApplicationTests {

	@MockBean
	private AuthServiceClient authServiceClient;

	@MockBean
	private CompanyKafkaProducer companyKafkaProducer;

	@Test
	void contextLoads() {
	}
}
