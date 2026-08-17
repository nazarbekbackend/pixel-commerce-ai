package kg.dev.pixel_commerce_ai.service;

import kg.dev.pixel_commerce_ai.dto.AiPurchaseAdviceRequest;
import kg.dev.pixel_commerce_ai.dto.AiPurchaseAdviceResponse;
import kg.dev.pixel_commerce_ai.dto.BudgetPurchasePlanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiPurchaseAdvisorServiceImpl
        implements AiPurchaseAdvisorService {

    private final AnalyticsService analyticsService;

    private final ChatClient.Builder chatClientBuilder;

    @Override
    public AiPurchaseAdviceResponse getAdvice(
            AiPurchaseAdviceRequest request
    ) {

        BudgetPurchasePlanResponse plan =
                analyticsService.getBudgetPurchasePlan(
                        request.getExchangeRate(),
                        request.getBudget()
                );

        ChatClient chatClient =
                chatClientBuilder.build();

        String prompt = """
                You are an AI purchasing advisor
                for a smartphone commerce business.

                Analyze the following purchase plan.

                IMPORTANT RULES:

                1. Do not invent numbers.
                2. Use only the provided business data.
                3. Do not change calculated values.
                4. Explain why the recommended products
                   are attractive.
                5. Mention budget usage.
                6. Mention expected revenue.
                7. Mention expected profit.
                8. Give a concise business recommendation.

                PURCHASE PLAN:

                Available budget:
                %s KGS

                Used budget:
                %s KGS

                Remaining budget:
                %s KGS

                Expected revenue:
                %s KGS

                Expected profit:
                %s KGS

                Recommended products:
                %s

                Return the answer in Russian.
                Keep it concise but useful for a business owner.
                """.formatted(
                plan.getAvailableBudget(),
                plan.getUsedBudget(),
                plan.getRemainingBudget(),
                plan.getExpectedRevenue(),
                plan.getExpectedProfit(),
                plan.getItems()
        );

        String advice =
                chatClient
                        .prompt()
                        .user(prompt)
                        .call()
                        .content();

        AiPurchaseAdviceResponse response =
                new AiPurchaseAdviceResponse();

        response.setBudget(
                plan.getAvailableBudget()
        );

        response.setUsedBudget(
                plan.getUsedBudget()
        );

        response.setRemainingBudget(
                plan.getRemainingBudget()
        );

        response.setExpectedRevenue(
                plan.getExpectedRevenue()
        );

        response.setExpectedProfit(
                plan.getExpectedProfit()
        );

        response.setRecommendedItems(
                plan.getItems()
        );

        response.setAdvice(
                advice
        );

        return response;
    }
}