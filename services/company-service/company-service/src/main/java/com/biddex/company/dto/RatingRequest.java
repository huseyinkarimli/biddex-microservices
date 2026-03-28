package com.biddex.company.dto;

import com.biddex.company.entity.RatingType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class RatingRequest {

	@NotNull
	private UUID toCompanyId;

	@NotNull
	private UUID tenderId;

	@NotNull
	private RatingType ratingType;

	@NotNull
	@Min(1)
	@Max(5)
	private Integer score;

	@Size(max = 2000)
	private String comment;
}
