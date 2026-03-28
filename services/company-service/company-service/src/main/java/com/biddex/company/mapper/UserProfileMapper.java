package com.biddex.company.mapper;

import com.biddex.company.dto.UserProfileDto;
import com.biddex.company.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

	@Mapping(source = "company.id", target = "companyId")
	UserProfileDto toDto(UserProfile profile);
}
