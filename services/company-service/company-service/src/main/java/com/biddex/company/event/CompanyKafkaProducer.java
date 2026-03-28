package com.biddex.company.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyKafkaProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper objectMapper;

	public void publishCompanyVerified(CompanyVerifiedEvent event) {
		send(CompanyKafkaTopics.COMPANY_VERIFIED, event.companyId().toString(), event);
	}

	public void publishRatingSubmitted(RatingSubmittedEvent event) {
		send(CompanyKafkaTopics.COMPANY_RATING_SUBMITTED, event.toCompanyId().toString(), event);
	}

	private void send(String topic, String key, Object payload) {
		try {
			String json = objectMapper.writeValueAsString(payload);
			kafkaTemplate.send(topic, key, json);
		} catch (JsonProcessingException e) {
			log.error("Failed to serialize Kafka payload for topic {}", topic, e);
			throw new IllegalStateException("Kafka serialization failed", e);
		}
	}
}
