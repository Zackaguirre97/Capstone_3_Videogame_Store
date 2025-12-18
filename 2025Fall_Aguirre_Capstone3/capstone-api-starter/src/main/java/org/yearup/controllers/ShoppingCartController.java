package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.security.Principal;

// convert this class to a REST controller
// only logged in users should have access to these actions
@RestController
@RequestMapping("cart")
@CrossOrigin
public class ShoppingCartController
{
    // a shopping cart requires
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;
    private ProductDao productDao;

    /*
    * Constructor
    * */
    @Autowired
    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    // each method in this controller requires a Principal object as a parameter
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShoppingCart> getCart(Principal principal)
    {
        try
        {
            int userId = userDao.getIdByUsername(principal.getName());
            ShoppingCart cart = shoppingCartDao.getByUserId(userId);

            if (cart == null)
            {
                cart = new ShoppingCart();
            }

            return ResponseEntity.ok(cart);
        }
        catch (Exception e)
        {
            e.printStackTrace(); // keep this while polishing
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Load cart failed"
            );
        }
    }

    // add a POST method to add a product to the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be added
    @PostMapping("products/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShoppingCart> addProduct(Principal principal,
                                                   @PathVariable int productId)
    {
        try
        {
            int userId = userDao.getIdByUsername(principal.getName());
            shoppingCartDao.addProductToCart(userId, productId);

            ShoppingCart cart = shoppingCartDao.getByUserId(userId);
            return ResponseEntity.ok(cart);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Add to cart failed"
            );
        }
    }

    // add a PUT method to update an existing product in the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be updated)
    // the BODY should be a ShoppingCartItem - quantity is the only value that will be updated
    @PutMapping("products/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShoppingCart> updateProductQuantity(
            Principal principal,
            @PathVariable int productId,
            @RequestBody ShoppingCartItem body)
    {
        try
        {
            int userId = userDao.getIdByUsername(principal.getName());

            shoppingCartDao.updateProductQuantity(
                    userId,
                    productId,
                    body.getQuantity()
            );

            ShoppingCart updatedCart = shoppingCartDao.getByUserId(userId);
            return ResponseEntity.ok(updatedCart);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Update cart failed"
            );
        }
    }

    // add a DELETE method to clear all products from the current users cart
    // https://localhost:8080/cart
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ShoppingCart> clearCart(Principal principal)
    {
        try
        {
            int userId = userDao.getIdByUsername(principal.getName());

            shoppingCartDao.clearCart(userId);

            return ResponseEntity.ok(new ShoppingCart());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Clear cart failed"
            );
        }
    }
}
