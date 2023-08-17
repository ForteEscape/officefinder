package com.dokkebi.officefinder.repository;

import com.dokkebi.officefinder.entity.Customer;
import com.dokkebi.officefinder.entity.office.Office;
import com.dokkebi.officefinder.entity.review.Review;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class ReviewRepositoryTest {

  @Autowired
  private ReviewRepository reviewRepository;
  @Autowired
  private CustomerRepository customerRepository;
  @Autowired
  private OfficeRepository officeRepository;

  @Test
  public void existsByCustomerAndOffice() throws Exception {
      //given
    Customer customer = customerRepository.save(Customer.builder().name("1").email("").password("").roles(
        Set.of("a")).point(0).build());
    Office office = officeRepository.save(Office.builder().name("1").build());

    Review review = Review.builder()
        .customer(customer)
        .office(office)
        .rate(5)
        .description("a").build();
    reviewRepository.save(review);
      //when
    boolean found = reviewRepository.existsByCustomerAndOffice(customer, office);
      //then
    Assertions.assertTrue(found);
  }

}