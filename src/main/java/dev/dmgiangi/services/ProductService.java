package dev.dmgiangi.services;

import dev.dmgiangi.models.Product;
import dev.dmgiangi.providers.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Flux<Product> getProducts() {
        return productRepository
                .findAll()
                .delayElements(Duration.of(5, ChronoUnit.SECONDS));
    }
}
