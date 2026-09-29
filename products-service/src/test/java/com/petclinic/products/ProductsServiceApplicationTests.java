package com.petclinic.products;

import com.petclinic.products.utils.PostgresTestContainerBase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ProductsServiceApplicationTests extends PostgresTestContainerBase {

    @Test
    void contextLoads() {
    }

}
