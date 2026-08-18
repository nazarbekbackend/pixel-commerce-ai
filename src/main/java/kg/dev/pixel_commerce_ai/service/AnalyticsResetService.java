package kg.dev.pixel_commerce_ai.service;

import kg.dev.pixel_commerce_ai.repository.SaleItemRepository;
import kg.dev.pixel_commerce_ai.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalyticsResetService {

    private final SaleItemRepository saleItemRepository;
    private final SaleRepository saleRepository;

    @Transactional
    public void reset() {

        /*
         * Сначала удаляем SaleItem,
         * потому что они ссылаются на Sale.
         */
        saleItemRepository.deleteAllItems();

        /*
         * Затем удаляем сами продажи.
         */
        saleRepository.deleteAllSales();
    }
}