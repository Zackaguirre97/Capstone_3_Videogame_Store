package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrderDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.Profile;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("order")
@CrossOrigin
public class OrdersController
{
    private OrderDao orderDao;
    private ShoppingCartDao shoppingCartDao;
    private ProfileDao profileDao;
    private UserDao userDao;

    @Autowired
    public OrdersController(OrderDao orderDao,
                            ShoppingCartDao shoppingCartDao,
                            ProfileDao profileDao,
                            UserDao userDao)
    {
        this.orderDao = orderDao;
        this.shoppingCartDao = shoppingCartDao;
        this.profileDao = profileDao;
        this.userDao = userDao;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public void checkout(Principal principal)
    {
        try
        {
            // 1. Get user
            String username = principal.getName();
            User user = userDao.getByUserName(username);

            if (user == null)
            {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }

            int userId = user.getId();

            // 2. Get profile (shipping info)
            Profile profile = profileDao.getByUserId(userId);

            if (profile == null)
            {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Profile information is required before checkout."
                );
            }

            // 3. Get shopping cart
            ShoppingCart cart = shoppingCartDao.getByUserId(userId);

            if (cart == null || cart.getItems().isEmpty())
            {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Shopping cart is empty."
                );
            }

            // 4. Create order
            int orderId = orderDao.createOrder(
                    userId,
                    LocalDateTime.now(),
                    profile.getAddress(),
                    profile.getCity(),
                    profile.getState(),
                    profile.getZip(),
                    BigDecimal.ZERO // shipping for now
            );

            if (orderId <= 0)
            {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Failed to create order."
                );
            }

            // 5. Create order line items
            for (ShoppingCartItem item : cart.getItems().values())
            {
                orderDao.addLineItem(
                        orderId,
                        item.getProductId(),
                        item.getProduct().getPrice(),
                        item.getQuantity(),
                        item.getDiscountPercent()
                );
            }

            // 6. Clear shopping cart
            shoppingCartDao.clearCart(userId);
        }
        catch (ResponseStatusException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Oops... our bad."
            );
        }
    }
}
