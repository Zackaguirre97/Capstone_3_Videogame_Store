let cartService;

class ShoppingCartService {

    cart = {
        items:[],
        total:0
    };

    addToCart(productId)
    {
        const url = `${config.baseUrl}/cart/products/${productId}`;
        // const headers = userService.getHeaders();

        axios.post(url, {})// ,{headers})
            .then(response => {
                this.setCart(response.data)

                this.updateCartDisplay()

            })
            .catch(error => {

                const data = {
                    error: "Add to cart failed."
                };

                templateBuilder.append("error", data, "errors")
            })
    }

    setCart(data)
    {
        this.cart = {
            items: [],
            total: 0
        }

        this.cart.total = data.total;

        for (const [key, value] of Object.entries(data.items)) {
            this.cart.items.push(value);
        }
    }

    loadCart()
    {

        const url = `${config.baseUrl}/cart`;

        axios.get(url)
            .then(response => {
                this.setCart(response.data)

                this.updateCartDisplay()

            })
            .catch(error => {

                const data = {
                    error: "Load cart failed."
                };

                templateBuilder.append("error", data, "errors")
            })

    }

    loadCartPage() {
        // templateBuilder.build("cart", this.cart, "main");
        const main = document.getElementById("main");
        main.innerHTML = "";

        // Filter box (empty placeholder)
        let div = document.createElement("div");
        div.classList = "filter-box";
        main.appendChild(div);

        const contentDiv = document.createElement("div");
        contentDiv.id = "content";
        contentDiv.classList.add("content-form");

        // Cart header
        const cartHeader = document.createElement("div");
        cartHeader.classList.add("cart-header");

        const h1 = document.createElement("h1");
        h1.innerText = "Cart";
        cartHeader.appendChild(h1);

        // Clear cart button
        const clearButton = document.createElement("button");
        clearButton.classList.add("btn", "btn-danger");
        clearButton.innerText = "Clear";
        clearButton.addEventListener("click", () => this.clearCart());
        cartHeader.appendChild(clearButton);

        // NEW checkout button
        const checkoutButton = document.createElement("button");
        checkoutButton.classList.add("btn", "btn-success");
        checkoutButton.innerText = "Checkout";

        checkoutButton.addEventListener("click", async () => {
            try {
                await axios.post(`${config.baseUrl}/order`, {}, {
                    headers: {
                        "Authorization": `Bearer ${JSON.parse(localStorage.getItem("user")).token}`
                    }
                });
                alert("Order placed successfully!");

                // Clear cart in memory and refresh display
                this.cart.items = [];
                this.cart.total = 0;
                this.updateCartDisplay();
                this.loadCartPage();

            } catch (err) {
                console.error(err);
                alert("Checkout failed.");
            } finally {
                checkoutButton.disabled = false;
            }
        });

        cartHeader.appendChild(checkoutButton);

        contentDiv.appendChild(cartHeader);
        main.appendChild(contentDiv);

        // Render cart items
        this.cart.items.forEach(item => {
            this.buildItem(item, contentDiv);
        });
    }


    buildItem(item, parent)
    {
        let outerDiv = document.createElement("div");
        outerDiv.classList.add("cart-item");

        let div = document.createElement("div");
        outerDiv.appendChild(div);
        let h4 = document.createElement("h4")
        h4.innerText = item.product.name;
        div.appendChild(h4);

        let photoDiv = document.createElement("div");
        photoDiv.classList.add("photo")
        let img = document.createElement("img");
        img.src = `./images/products/${item.product.imageUrl}`
        img.addEventListener("click", () => {
            showImageDetailForm(item.product.name, img.src)
        })
        photoDiv.appendChild(img)
        let priceH4 = document.createElement("h4");
        priceH4.classList.add("price");
        priceH4.innerText = `$${item.product.price}`;
        photoDiv.appendChild(priceH4);
        outerDiv.appendChild(photoDiv);

        let descriptionDiv = document.createElement("div");
        descriptionDiv.innerText = item.product.description;
        outerDiv.appendChild(descriptionDiv);

        let quantityDiv = document.createElement("div")
        quantityDiv.innerText = `Quantity: ${item.quantity}`;
        outerDiv.appendChild(quantityDiv)


        parent.appendChild(outerDiv);
    }

    clearCart()
    {

        const url = `${config.baseUrl}/cart`;

        axios.delete(url)
             .then(response => {
                 this.cart = {
                     items: [],
                     total: 0
                 }

                 this.cart.total = response.data.total;

                 for (const [key, value] of Object.entries(response.data.items)) {
                     this.cart.items.push(value);
                 }

                 this.updateCartDisplay()
                 this.loadCartPage()

             })
             .catch(error => {

                 const data = {
                     error: "Empty cart failed."
                 };

                 templateBuilder.append("error", data, "errors")
             })
    }

    updateCartDisplay()
    {
        try {
            const itemCount = this.cart.items.reduce(
                (sum,item)=>sum+item.quantity,0
            );
            const cartControl = document.getElementById("cart-items")

            cartControl.innerText = itemCount;
        }
        catch (e) {

        }
    }
}





document.addEventListener('DOMContentLoaded', () => {
    cartService = new ShoppingCartService();

    if(userService.isLoggedIn())
    {
        cartService.loadCart();
    }

});
