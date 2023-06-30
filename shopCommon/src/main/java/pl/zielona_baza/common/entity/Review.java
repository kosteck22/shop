package pl.zielona_baza.common.entity;

import lombok.*;
import pl.zielona_baza.common.entity.Customer;
import pl.zielona_baza.common.entity.product.Product;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor @AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 64, nullable = false)
    private String headline;

    @Column(length = 256, nullable = false)
    private String comment;

    private int rating;

    @Column(name = "review_time", nullable = false)
    private Date reviewTime;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private int votes;

    @Transient
    public boolean upVotedByCurrentCustomer;

    @Transient
    public boolean downVotedByCurrentCustomer;

    public Review(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Review{" +
                "headline='" + headline + '\'' +
                ", rating=" + rating +
                ", reviewTime=" + reviewTime +
                ", product=" + product.getShortName() +
                ", customer=" + customer.getFullName() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return id.equals(review.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
