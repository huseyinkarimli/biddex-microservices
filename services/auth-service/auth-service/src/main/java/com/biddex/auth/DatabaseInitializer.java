package com.biddex.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;

@Component
@Order(Integer.MAX_VALUE)
@RequiredArgsConstructor
public class DatabaseInitializer implements ApplicationRunner {

	private static final String ADMIN_EMAIL = "admin@biddex.az";
	private static final String ADMIN_PASSWORD = "Admin123!";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (userRepository.existsByEmailIgnoreCase(ADMIN_EMAIL)) {
			return;
		}
		User admin = User.builder()
				.email(ADMIN_EMAIL)
				.password(passwordEncoder.encode(ADMIN_PASSWORD))
				.firstName("Platform")
				.lastName("Administrator")
				.isActive(true)
				.isVerified(true)
				.systemRoles(EnumSet.of(SystemRole.ADMIN, SystemRole.USER))
				.build();
		userRepository.save(admin);
	}
}
