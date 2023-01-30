package dev.dmgiangi.controllers;

import dev.dmgiangi.models.Product;
import dev.dmgiangi.services.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/")
@AllArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Product> getProducts(){
        return productService.getProducts();
    }
}
