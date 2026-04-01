package com.example.EShopProject.Service;

import com.example.EShopProject.Repository.CartItemRepository;
import com.example.EShopProject.Repository.OrderItemRepository;
import com.example.EShopProject.Repository.OrderRepository;
import com.example.EShopProject.entity.CartItem;
import com.example.EShopProject.entity.Order;
import com.example.EShopProject.entity.OrderForm;
import com.example.EShopProject.entity.OrderItem;
import com.example.EShopProject.entity.Product;
import com.example.EShopProject.entity.User;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CartService {

    @Autowired private CartItemRepository cartItemRepo;
    @Autowired private ProductService productService;
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;


    public void addToCart(User user, int productId, int quantity) {
        Product product = productService.getSingleProduct(productId);
        CartItem item = cartItemRepo.findByUserAndProduct(user, product).orElse(null);

        if (item != null) {
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            item = new CartItem();
            item.setUser(user);
            item.setProduct(product);
            item.setQuantity(quantity);
        }

        cartItemRepo.save(item);
    }

    public List<CartItem> getCartItems(User user) {
        return cartItemRepo.findByUser(user);
    }

    public double getTotalPrice(User user) {
        return getCartItems(user).stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }

    public void updateQuantity(User user, int productId, int quantity) {
        Product product = productService.getSingleProduct(productId);
        CartItem item = cartItemRepo.findByUserAndProduct(user, product).orElse(null);
        if (item != null) {
            item.setQuantity(quantity);
            cartItemRepo.save(item);
        }
    }

    @Transactional
    public void removeItem(User user, int productId) {
        Product product = productService.getSingleProduct(productId);
        cartItemRepo.deleteByUserAndProduct(user, product);
    }

    @Transactional
    public void clearCart(User user) {
        cartItemRepo.deleteByUser(user);
    }

    @Transactional
    public void checkout(User user, OrderForm orderForm) {
        List<CartItem> cartItems = getCartItems(user);

        if (cartItems.isEmpty()) return;

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setFullName(orderForm.getFullName());
        order.setAddressLine(orderForm.getAddressLine());
        order.setArea(orderForm.getArea());
        order.setCity(orderForm.getCity());
        order.setState(orderForm.getState());
        order.setPinCode(orderForm.getPinCode());

        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItem.setTotalPrice(cartItem.getTotalPrice());
            orderItems.add(orderItem);
            total += cartItem.getTotalPrice();
        }

        order.setItems(orderItems);
        order.setTotalAmount(total);

        orderRepository.save(order);
        orderItemRepository.saveAll(orderItems);
        cartItemRepository.deleteAll(cartItems);
    }

}
