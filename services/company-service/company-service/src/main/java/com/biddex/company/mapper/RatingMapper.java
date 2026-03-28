package com.biddex.company.mapper;

import com.biddex.company.dto.RatingDto;
import com.biddex.company.entity.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RatingMapper {

	@Mapping(source = "fromCompany.id", target = "fromCompanyId")
	@Mapping(source = "toCompany.id", target = "toCompanyId")
	RatingDto toDto(Rating rating);
}
