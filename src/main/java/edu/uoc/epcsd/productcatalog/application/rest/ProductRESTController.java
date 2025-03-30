package edu.uoc.epcsd.productcatalog.application.rest;


import edu.uoc.epcsd.productcatalog.application.rest.request.CreateProductRequest;
import edu.uoc.epcsd.productcatalog.application.rest.request.FindProductsByCriteria;
import edu.uoc.epcsd.productcatalog.application.rest.response.GetProductResponse;
import edu.uoc.epcsd.productcatalog.domain.Product;
import edu.uoc.epcsd.productcatalog.domain.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

@Log4j2
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("/products")
public class ProductRESTController {

    private final ProductService productService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Product> getAllProducts() {
        log.trace("getAllProducts");

        return productService.findAllProducts();
    }

    @GetMapping("/{productId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<GetProductResponse> getProductById(@PathVariable @NotNull Long productId) {
        log.trace("getProductById");

        return productService.findProductById(productId).map(product -> ResponseEntity.ok().body(GetProductResponse.fromDomain(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    // TODO: add the code for the missing system operations here: 
    // use the corresponding mapping HTTP request annotation with the parameter: "/search"
    // and call the method findProductsByCriteria(FindProductsByCriteria findProductsCriteria)
    // which call the corresponding findProductsByExample method 

    @PostMapping
    public ResponseEntity<Long> createProduct(@RequestBody @NotNull @Valid CreateProductRequest createProductRequest) {
        log.trace("createProduct");

        log.trace("Creating product " + createProductRequest);

        try {
            Long productId = productService.createProduct(Product.builder()
                    .name(createProductRequest.getName())
                    .description(createProductRequest.getDescription())
                    .dailyPrice(createProductRequest.getDailyPrice())
                    .model(createProductRequest.getModel())
                    .brand(createProductRequest.getBrand())
                    .categoryId(createProductRequest.getCategoryId())
                    .build());

            URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(productId)
                    .toUri();

            return ResponseEntity.created(uri).body(productId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The specified product category " + createProductRequest.getCategoryId() + " does not exist.", e);
        }
    }

    // TODO: add the code for the missing system operations here: 
    // use the corresponding mapping HTTP request annotation with the parameter: "/{productId}"
    // and call the method removeProduct(@PathVariable @NotNull Long productId)
    // which call the corresponding deleteProduct method 

}
