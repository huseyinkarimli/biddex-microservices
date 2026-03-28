package com.biddex.company.mapper;

import com.biddex.company.dto.CompanyDto;
import com.biddex.company.dto.CompanyShortDto;
import com.biddex.company.dto.CompanyUpdateRequest;
import com.biddex.company.entity.Company;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

	CompanyDto toDto(Company company);

	CompanyShortDto toShortDto(Company company);

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void applyUpdate(CompanyUpdateRequest request, @MappingTarget Company company);
}
