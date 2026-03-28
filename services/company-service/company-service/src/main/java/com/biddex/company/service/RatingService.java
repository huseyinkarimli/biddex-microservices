package com.biddex.company.service;

import com.biddex.company.dto.RatingDto;
import com.biddex.company.dto.RatingRequest;
import com.biddex.company.entity.Company;
import com.biddex.company.entity.Rating;
import com.biddex.company.entity.RatingType;
import com.biddex.company.event.CompanyKafkaProducer;
import com.biddex.company.event.RatingSubmittedEvent;
import com.biddex.company.exception.CompanyNotFoundException;
import com.biddex.company.exception.DuplicateRatingException;
import com.biddex.company.exception.UnauthorizedOperationException;
import com.biddex.company.mapper.RatingMapper;
import com.biddex.company.repository.CompanyRepository;
import com.biddex.company.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RatingService {

	private final RatingRepository ratingRepository;
	private final CompanyRepository companyRepository;
	private final RatingMapper ratingMapper;
	private final CompanyKafkaProducer companyKafkaProducer;

	@Transactional
	public RatingDto submitRating(RatingRequest request, UUID fromCompanyId) {
		if (fromCompanyId == null) {
			throw new UnauthorizedOperationException("Company context is required to submit a rating");
		}
		if (fromCompanyId.equals(request.getToCompanyId())) {
			throw new UnauthorizedOperationException("Cannot rate your own company");
		}

		Company from = companyRepository.findById(fromCompanyId)
				.orElseThrow(() -> new CompanyNotFoundException("Source company not found"));
		Company to = companyRepository.findById(request.getToCompanyId())
				.orElseThrow(() -> new CompanyNotFoundException("Target company not found"));

		if (!from.isActive() || !to.isActive()) {
			throw new UnauthorizedOperationException("Inactive companies cannot participate in ratings");
		}

		if (ratingRepository.existsByTenderIdAndFromCompany_IdAndToCompany_IdAndRatingType(
				request.getTenderId(), fromCompanyId, request.getToCompanyId(), request.getRatingType())) {
			throw new DuplicateRatingException("Rating already submitted for this tender and direction");
		}

		Rating rating = Rating.builder()
				.fromCompany(from)
				.toCompany(to)
				.tenderId(request.getTenderId())
				.ratingType(request.getRatingType())
				.score(request.getScore())
				.comment(request.getComment())
				.build();

		Rating saved = ratingRepository.save(rating);
		recalculateRatingsForCompany(to.getId());

		companyKafkaProducer.publishRatingSubmitted(
				new RatingSubmittedEvent(fromCompanyId, request.getToCompanyId(), request.getTenderId(), request.getScore(), request.getRatingType())
		);

		return ratingMapper.toDto(saved);
	}

	@Transactional(readOnly = true)
	public List<RatingDto> getCompanyRatings(UUID companyId) {
		if (!companyRepository.existsById(companyId)) {
			throw new CompanyNotFoundException("Company not found");
		}
		return ratingRepository.findByToCompany_IdOrderByCreatedAtDesc(companyId)
				.stream()
				.map(ratingMapper::toDto)
				.toList();
	}

	private void recalculateRatingsForCompany(UUID toCompanyId) {
		Company company = companyRepository.findById(toCompanyId)
				.orElseThrow(() -> new CompanyNotFoundException("Company not found"));

		Number sellerAvgNum = ratingRepository.averageScoreByToCompanyAndType(toCompanyId, RatingType.BUYER_TO_SELLER);
		Number buyerAvgNum = ratingRepository.averageScoreByToCompanyAndType(toCompanyId, RatingType.SELLER_TO_BUYER);
		double sellerAvg = sellerAvgNum != null ? sellerAvgNum.doubleValue() : 0.0;
		double buyerAvg = buyerAvgNum != null ? buyerAvgNum.doubleValue() : 0.0;
		long total = ratingRepository.countByToCompanyId(toCompanyId);

		company.setSellerRating(sellerAvg);
		company.setBuyerRating(buyerAvg);
		company.setRatingCount((int) Math.min(total, Integer.MAX_VALUE));
		companyRepository.save(company);
	}
}
