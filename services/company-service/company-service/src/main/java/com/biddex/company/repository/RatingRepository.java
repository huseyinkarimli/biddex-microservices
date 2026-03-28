package com.biddex.company.repository;

import com.biddex.company.entity.Rating;
import com.biddex.company.entity.RatingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RatingRepository extends JpaRepository<Rating, UUID> {

	boolean existsByTenderIdAndFromCompany_IdAndToCompany_IdAndRatingType(
			UUID tenderId,
			UUID fromCompanyId,
			UUID toCompanyId,
			RatingType ratingType
	);

	List<Rating> findByToCompany_IdOrderByCreatedAtDesc(UUID toCompanyId);

	@Query("""
			select coalesce(avg(r.score), 0.0)
			from Rating r
			where r.toCompany.id = :companyId and r.ratingType = :ratingType
			""")
	Number averageScoreByToCompanyAndType(@Param("companyId") UUID companyId, @Param("ratingType") RatingType ratingType);

	@Query("""
			select count(r)
			from Rating r
			where r.toCompany.id = :companyId
			""")
	long countByToCompanyId(@Param("companyId") UUID companyId);

}
