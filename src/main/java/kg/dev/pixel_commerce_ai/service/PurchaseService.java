package kg.dev.pixel_commerce_ai.service;

import kg.dev.pixel_commerce_ai.dto.PurchaseRequest;
import kg.dev.pixel_commerce_ai.dto.PurchaseResponse;
import kg.dev.pixel_commerce_ai.entity.Purchase;

import java.util.List;

public interface PurchaseService {
    void create(PurchaseRequest request);
    List<PurchaseResponse> findAll();

}
