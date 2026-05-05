package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.CartItem;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderStatus;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.OrderRepository;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;

    public PaymentService(OrderRepository orderRepository,
                          OrderItemRepository orderItemRepository,
                          CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
    }

    /**
     * 1️⃣ Create order in DB (before payment)
     */
    @Transactional
    public Order createOrderInDB(int userId, String orderId, BigDecimal totalAmount) {

        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }

    /**
     * 2️⃣ Payment success ke baad call karo
     */
    @Transactional
    public void markPaymentSuccess(String orderId, int userId) {

		System.out.println("🔥 PAYMENT SUCCESS CALLED");

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.SUCCESS);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // 🛒 Cart → OrderItems
        List<CartItem> cartItems = cartRepository.findCartItemsWithProductDetails(userId);

        for (CartItem cartItem : cartItems) {
            OrderItem item = new OrderItem();

            item.setOrder(order);
            item.setProductId(cartItem.getProduct().getProductId());
            item.setQuantity(cartItem.getQuantity());
            item.setPricePerUnit(cartItem.getProduct().getPrice());

            item.setTotalPrice(
                    cartItem.getProduct().getPrice()
                            .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );

            orderItemRepository.save(item);
        }

        // 🔥 FINAL FIX — CART CLEAR
        cartRepository.deleteByUserId(userId);

        
    }

    /**
     * 3️⃣ Payment fail
     */
    @Transactional
    public void markPaymentFailed(String orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.FAILED);
        order.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(order);
    }
}