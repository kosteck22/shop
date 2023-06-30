package pl.zielona_baza.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan({"pl.zielona_baza.common.entity", "pl.zielona_baza.admin"})
public class ShopBackendApplication {
    public static void main(String[] args) {

        SpringApplication.run(ShopBackendApplication.class, args);
    }
}
