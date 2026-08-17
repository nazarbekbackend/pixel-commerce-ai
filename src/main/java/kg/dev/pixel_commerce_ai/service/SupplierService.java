package kg.dev.pixel_commerce_ai.service;

import kg.dev.pixel_commerce_ai.entity.Supplier;

import java.util.List;

public interface SupplierService {

    Supplier create(Supplier supplier);

    List<Supplier> findAll();
}