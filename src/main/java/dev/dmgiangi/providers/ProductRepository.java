package dev.dmgiangi.providers;

import dev.dmgiangi.models.Product;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ProductRepository extends ReactiveCrudRepository<Product, Integer> {
}
