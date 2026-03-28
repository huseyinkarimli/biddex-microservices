package com.biddex.auth;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Builder
public class AuthUserPrincipal implements UserDetails {

	private final UUID userId;
	private final String email;
	private final String password;
	private final boolean active;
	private final Collection<? extends GrantedAuthority> authorities;
	private final UUID companyId;
	private final CompanyType companyType;
	private final CompanyRole companyRole;

	public static AuthUserPrincipal from(User user, UserCompany primaryMembership) {
		Set<GrantedAuthority> auths = user.getSystemRoles().stream()
				.map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
				.collect(Collectors.toSet());
		if (auths.isEmpty()) {
			auths.add(new SimpleGrantedAuthority("ROLE_USER"));
		}

		UUID companyId = null;
		CompanyType companyType = null;
		CompanyRole companyRole = null;
		if (primaryMembership != null) {
			companyId = primaryMembership.getCompany().getId();
			companyType = primaryMembership.getCompany().getCompanyType();
			companyRole = primaryMembership.getCompanyRole();
		}

		return AuthUserPrincipal.builder()
				.userId(user.getId())
				.email(user.getEmail())
				.password(user.getPassword())
				.active(user.isActive())
				.authorities(auths)
				.companyId(companyId)
				.companyType(companyType)
				.companyRole(companyRole)
				.build();
	}

	public static UserCompany selectPrimaryMembership(List<UserCompany> memberships) {
		if (memberships == null || memberships.isEmpty()) {
			return null;
		}
		return memberships.stream()
				.filter(Objects::nonNull)
				.min(Comparator
						.comparing((UserCompany uc) -> uc.getCompanyRole() != CompanyRole.COMPANY_ADMIN)
						.thenComparing(UserCompany::getCreatedAt))
				.orElse(null);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return active;
	}
}
