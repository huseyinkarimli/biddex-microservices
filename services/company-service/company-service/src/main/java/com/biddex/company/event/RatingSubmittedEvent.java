package com.biddex.company.event;

import com.biddex.company.entity.RatingType;

import java.util.UUID;

public record RatingSubmittedEvent(
		UUID fromCompanyId,
		UUID toCompanyId,
		UUID tenderId,
		int score,
		RatingType ratingType
) {
}
