package edu.uoc.epcsd.productcatalog.application.rest;


import edu.uoc.epcsd.productcatalog.application.rest.request.CreateProductRequest;
import edu.uoc.epcsd.productcatalog.application.rest.request.FindProductsByCriteria;
import edu.uoc.epcsd.productcatalog.application.rest.response.GetProductResponse;
import edu.uoc.epcsd.productcatalog.domain.Product;
import edu.uoc.epcsd.productcatalog.domain.service.CategoryNotFoundException;
import edu.uoc.epcsd.productcatalog.domain.service.ProductNotFoundException;
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

    /**
     * GET requests to search for products by optional criteria such as name and category ID.
     *
     * @param findProductsCriteria an object containing optional search filters (e.g. name, categoryId)
     * @return a list of products matching the specified criteria
     * @throws CategoryNotFoundException with status 400 if the product ID does not exist
     */
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<Product> findProductsByCriteria(@ModelAttribute FindProductsByCriteria findProductsCriteria) {
        log.trace("findProductsByCriteria");

        try {
            Product product = new Product();
            product.setName(findProductsCriteria.getName());
            product.setCategoryId(findProductsCriteria.getCategoryId());

            return productService.findProductsByExample(product);

        } catch (IllegalArgumentException e) {
            CategoryNotFoundException ce = new CategoryNotFoundException(findProductsCriteria.getCategoryId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ce.getMessage());
        }
    }

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

    /**
     * DELETE request to remove a product by ID
     *
     * @param productId the productId
     * @return HTTP 204 No Content if successfully removed
     * @throws ProductNotFoundException with status 400 if the product ID does not exist
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<GetProductResponse> removeProduct(@PathVariable @NotNull Long productId) {
        log.trace("removeProduct");

        log.trace("Deleting product " + productId);

        productService.findProductById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        new ProductNotFoundException(productId).getMessage()));

        productService.deleteProduct(productId);

        return ResponseEntity.noContent().build();
    }
}