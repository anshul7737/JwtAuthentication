package com.springboot.jwtaunthetication.repository;

import com.springboot.jwtaunthetication.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product,Integer> {
}
