package org.yearup.data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface OrderDao {
    int createOrder(int userId,
                           LocalDateTime date,
                           String address,
                           String city,
                           String state,
                           String zip,
                           BigDecimal shippingAmount);

    void addLineItem(int orderId, int productId, BigDecimal salesPrice, int quantity, BigDecimal discount);
}

