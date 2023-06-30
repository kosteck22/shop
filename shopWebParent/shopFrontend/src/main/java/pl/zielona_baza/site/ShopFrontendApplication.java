package pl.zielona_baza.site;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan({"pl.zielona_baza.common.entity", "pl.zielona_baza.site"})
public class ShopFrontendApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShopFrontendApplication.class, args);
    }

}
