package com.biddex.company.repository;

import com.biddex.company.entity.CompanyDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompanyDocumentRepository extends JpaRepository<CompanyDocument, UUID> {

	List<CompanyDocument> findByCompany_Id(UUID companyId);
}
