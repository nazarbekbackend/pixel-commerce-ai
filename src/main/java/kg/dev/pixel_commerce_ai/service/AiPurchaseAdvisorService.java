package kg.dev.pixel_commerce_ai.service;

import kg.dev.pixel_commerce_ai.dto.AiPurchaseAdviceRequest;
import kg.dev.pixel_commerce_ai.dto.AiPurchaseAdviceResponse;

public interface AiPurchaseAdvisorService {

    AiPurchaseAdviceResponse getAdvice(
            AiPurchaseAdviceRequest request
    );
}