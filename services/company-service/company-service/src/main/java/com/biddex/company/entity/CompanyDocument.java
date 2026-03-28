package com.biddex.company.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "company_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDocument {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private DocumentType documentType;

	@Column(nullable = false, length = 512)
	private String fileName;

	@Column(nullable = false, length = 1024)
	private String minioPath;

	@Column(nullable = false)
	private Instant uploadedAt;

	@Column(name = "is_verified", nullable = false)
	@Builder.Default
	private boolean isVerified = false;

	@PrePersist
	void onCreate() {
		if (uploadedAt == null) {
			uploadedAt = Instant.now();
		}
	}
}
