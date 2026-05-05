package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.CartItem;

import jakarta.transaction.Transactional;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Integer> {

    @Query("SELECT c FROM CartItem c WHERE c.user.userId = :userId AND c.product.productId = :productId")
    Optional<CartItem> findByUserAndProduct(int userId, int productId);

    @Query("SELECT c FROM CartItem c JOIN FETCH c.product p LEFT JOIN FETCH ProductImage pi ON p.productId = pi.product.productId WHERE c.user.userId = :userId")
    List<CartItem> findCartItemsWithProductDetails(int userId);

    // 🔥 FIXED (IMPORTANT)
    @Modifying
    @Transactional
    @Query("UPDATE CartItem c SET c.quantity = :quantity WHERE c.id = :cartItemId")
    void updateCartItemQuantity(@Param("cartItemId") int cartItemId, @Param("quantity") int quantity);

    // DELETE SINGLE ITEM
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.user.userId = :userId AND c.product.productId = :productId")
    void deleteCartItem(@Param("userId") int userId, @Param("productId") int productId);

    // COUNT
    @Query("SELECT COALESCE(SUM(c.quantity), 0) FROM CartItem c WHERE c.user.userId = :userId")
    int countTotalItems(int userId);

    // 🔥 KEEP ONLY ONE METHOD
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.user.userId = :userId")
    void deleteByUserId(@Param("userId") Integer userId);
}