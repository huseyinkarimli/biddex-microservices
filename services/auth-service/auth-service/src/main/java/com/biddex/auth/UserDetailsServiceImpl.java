package com.biddex.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;
	private final UserCompanyRepository userCompanyRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) {
		User user = userRepository.findByEmailIgnoreCase(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		if (!user.isActive()) {
			throw new UsernameNotFoundException("User is inactive");
		}
		List<UserCompany> memberships = userCompanyRepository.findActiveMembershipsWithCompany(user.getId());
		UserCompany primary = AuthUserPrincipal.selectPrimaryMembership(memberships);
		return AuthUserPrincipal.from(user, primary);
	}
}
