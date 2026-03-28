package com.biddex.company.dto;

import com.biddex.company.entity.RatingType;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class RatingDto {
	UUID id;
	UUID fromCompanyId;
	UUID toCompanyId;
	UUID tenderId;
	RatingType ratingType;
	int score;
	String comment;
	Instant createdAt;
}
