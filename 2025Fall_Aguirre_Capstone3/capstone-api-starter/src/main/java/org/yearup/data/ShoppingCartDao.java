package org.yearup.data;

import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao
{
    ShoppingCart getByUserId(int userId);

    /*
    * add additional method signatures here
    */

    // Add a product to the cart (increment quantity if already exists)
    void addProductToCart(int userId, int productId);

    // Update the quantity of a product in the cart
    void updateProductQuantity(int userId, int productId, int quantity);

    // Clear the entire cart for a user
    void clearCart(int userId);
}
