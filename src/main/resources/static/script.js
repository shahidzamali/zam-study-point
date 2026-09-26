const API_BASE = "";

let allProducts = [];

/* =========================================================
   HELPER - FILE URL
   ========================================================= */

function getFileUrl(value, folder) {
  if (!value) {
    return "";
  }

  const url = String(value).trim();

  if (url.startsWith("http://") || url.startsWith("https://")) {
    return url;
  }

  if (url.startsWith("/")) {
    return `${API_BASE}${url}`;
  }

  return `${API_BASE}/${folder}/${url}`;
}

/* =========================================================
   LOGIN / AUTH STATUS
   ========================================================= */

function updateAuthButtons() {
  const authButtons = document.querySelector(".auth-buttons");

  if (!authButtons) {
    return;
  }

  const storedUser = localStorage.getItem("loggedInUser");

  /* LOGGED OUT */

  if (!storedUser) {
    authButtons.innerHTML = `
      <a href="login.html" class="login-btn">
        Login
      </a>

      <a href="register.html" class="register-btn">
        Register
      </a>
    `;

    return;
  }

  /* LOGGED IN */

  let user;

  try {
    user = JSON.parse(storedUser);
  } catch (error) {
    console.error("Login data error:", error);

    localStorage.removeItem("loggedInUser");

    updateAuthButtons();

    return;
  }

  authButtons.innerHTML = `
    <span class="welcome-user">
      Welcome, ${user.name || "User"}
    </span>

    <button
      type="button"
      class="history-btn"
      onclick="loadPurchaseHistory()">
      Purchase History
    </button>

    <button
      type="button"
      class="logout-btn"
      onclick="logoutUser()">
      Logout
    </button>
  `;
}

/* =========================================================
   SIDE USER MENU
   ========================================================= */

function updateSideUserMenu() {
  const sideUserMenu = document.getElementById("sideUserMenu");

  if (!sideUserMenu) {
    return;
  }

  const storedUser = localStorage.getItem("loggedInUser");

  /* NOT LOGGED IN */

  if (!storedUser) {
    sideUserMenu.innerHTML = `
      <a
        href="login.html"
        class="side-user-btn side-login-btn">
        Login
      </a>

      <a
        href="register.html"
        class="side-user-btn side-register-btn">
        Register
      </a>
    `;

    return;
  }

  /* LOGGED IN */

  let user;

  try {
    user = JSON.parse(storedUser);
  } catch (error) {
    console.error("Invalid user data:", error);

    localStorage.removeItem("loggedInUser");

    updateSideUserMenu();

    return;
  }

  sideUserMenu.innerHTML = `
    <div class="side-welcome-user">
      Welcome, ${user.name || "User"}
    </div>

    <button
      type="button"
      class="side-user-btn side-history-btn"
      onclick="loadPurchaseHistory(); closeMobileMenu();">
      Purchase History
    </button>

    <button
      type="button"
      class="side-user-btn side-logout-btn"
      onclick="logoutUser();">
      Logout
    </button>
  `;
}

/* =========================================================
   LOGOUT
   ========================================================= */

function logoutUser() {
  localStorage.removeItem("loggedInUser");

  alert("Logout Successful ✅");

  window.location.href = "index.html";
}

/* =========================================================
   PRODUCTS CONTAINER
   ========================================================= */

const productsContainer = document.getElementById("products-container");

/* =========================================================
   LOAD PRODUCTS
   ========================================================= */

async function loadProducts() {
  if (!productsContainer) {
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/api/products`);

    if (!response.ok) {
      throw new Error(`Products API failed: ${response.status}`);
    }

    const products = await response.json();

    allProducts = Array.isArray(products) ? products : [];

    displayProducts(allProducts);
  } catch (error) {
    console.error("Load Products Error:", error);

    productsContainer.innerHTML = `
      <p>
        Products could not be loaded.
      </p>
    `;
  }
}

/* =========================================================
   DISPLAY PRODUCTS
   ========================================================= */

function displayProducts(products) {
  if (!productsContainer) {
    return;
  }

  productsContainer.innerHTML = "";

  if (!products || products.length === 0) {
    productsContainer.innerHTML = `
      <p>
        No products available.
      </p>
    `;

    return;
  }

  products.forEach((product) => {
    const card = document.createElement("div");

    card.className = "product-card";

    const thumbnailUrl = getFileUrl(product.imageUrl, "thumbnails");

    card.innerHTML = `
      ${
        thumbnailUrl
          ? `
            <img
              src="${thumbnailUrl}"
              alt="${product.name || "Product"}"
              class="product-image"
              onerror="
                this.style.display='none';
                this.nextElementSibling.style.display='flex';
              "
            >

            <div
              class="product-image-placeholder"
              style="display:none;">
              No Image
            </div>
          `
          : `
            <div class="product-image-placeholder">
              No Image
            </div>
          `
      }

      <span class="product-category">
        ${product.category || "Digital Product"}
      </span>

      <h3>
        ${product.name || "Untitled Product"}
      </h3>

      <p>
        ${product.description || ""}
      </p>

      <div class="product-price">
        ₹${product.price ?? 0}
      </div>

      <button
        type="button"
        class="buy-btn"
        onclick="buyProduct(${product.id})">
        View Product
      </button>
    `;

    productsContainer.appendChild(card);
  });
}

/* =========================================================
   VIEW PRODUCT
   ========================================================= */

async function buyProduct(productId) {
  try {
    let product = allProducts.find((p) => Number(p.id) === Number(productId));

    try {
      const response = await fetch(`${API_BASE}/api/products/${productId}`);

      if (response.ok) {
        const backendProduct = await response.json();

        if (backendProduct) {
          product = backendProduct;
        }
      }
    } catch (apiError) {
      console.warn("Product details API unavailable.", apiError);
    }

    if (!product) {
      alert("Product details could not be loaded.");

      return;
    }

    showProductDetails(product);
  } catch (error) {
    console.error("Product Details Error:", error);

    alert("Product details could not be loaded.");
  }
}

/* =========================================================
   DIRECT BUY PRODUCT
   ========================================================= */

async function buyProductDirect(productId) {
  try {
    // Product find karo
    let product = allProducts.find((p) => Number(p.id) === Number(productId));

    // Agar product allProducts me nahi mila
    if (!product) {
      const response = await fetch(`${API_BASE}/api/products/${productId}`);

      if (!response.ok) {
        throw new Error("Product not found");
      }

      product = await response.json();
    }

    if (!product) {
      alert("Product details could not be loaded.");
      return;
    }

    // Customer details
    const customerName = prompt("Enter your name:");

    if (!customerName) {
      return;
    }

    const customerEmail = prompt("Enter your email:");

    if (!customerEmail) {
      return;
    }

    const customerPhone = prompt("Enter your phone number:");

    if (!customerPhone) {
      return;
    }

    // ==============================
    // CREATE RAZORPAY ORDER
    // ==============================

    const orderResponse = await fetch(
      `${API_BASE}/api/payment/create-order?productId=${productId}`,
      {
        method: "POST",
      },
    );

    if (!orderResponse.ok) {
      throw new Error("Unable to create Razorpay order");
    }

    const orderData = await orderResponse.json();

    // ==============================
    // RAZORPAY CHECKOUT
    // ==============================

    const options = {
      key: orderData.keyId,

      amount: orderData.amount,

      currency: orderData.currency,

      name: "Zam Digital Store",

      description: product.name,

      order_id: orderData.orderId,

      handler: async function (response) {
        try {
          // ==============================
          // VERIFY PAYMENT
          // ==============================

          const params = new URLSearchParams({
            paymentId: response.razorpay_payment_id,

            orderId: response.razorpay_order_id,

            signature: response.razorpay_signature,

            productId: productId,

            customerName: customerName,

            customerEmail: customerEmail,

            customerPhone: customerPhone,
          });

          const verifyResponse = await fetch(
            `${API_BASE}/api/payment/verify?${params.toString()}`,
            {
              method: "POST",
            },
          );

          const result = await verifyResponse.text();

          if (!verifyResponse.ok) {
            throw new Error(result);
          }

          alert(
            "Payment successful! 🎉\n\n" + "Your payment has been verified.",
          );

          // Optional:
          // product file open/download karwana ho
          if (product.fileUrl) {
            window.open(product.fileUrl, "_blank");
          }
        } catch (error) {
          console.error("Payment verification error:", error);

          alert(
            "Payment successful but verification failed.\nPlease contact support.",
          );
        }
      },

      prefill: {
        name: customerName,

        email: customerEmail,

        contact: customerPhone,
      },

      theme: {
        color: "#4f46e5",
      },
    };

    const razorpay = new Razorpay(options);

    razorpay.on("payment.failed", function (response) {
      console.error("Payment failed:", response.error);

      alert("Payment failed. Please try again.");
    });

    razorpay.open();
  } catch (error) {
    console.error("Direct Buy Error:", error);

    alert("Unable to start payment. Please try again.");
  }
}

/* =========================================================
   PRODUCT DETAILS
   ========================================================= */

function showProductDetails(product) {
  closeProductDetails();

  const details = document.createElement("div");

  details.className = "product-details-overlay";

  let images = [];

  if (Array.isArray(product.previewImages)) {
    images = product.previewImages;
  } else if (
    product.previewImages &&
    typeof product.previewImages === "string"
  ) {
    try {
      images = JSON.parse(product.previewImages);

      if (!Array.isArray(images)) {
        images = [];
      }
    } catch (error) {
      images = [];
    }
  }

  let currentImage = 0;

  const firstImage = images.length > 0 ? getFileUrl(images[0], "previews") : "";

  details.innerHTML = `
    <div class="product-details">

      <button
        type="button"
        class="close-btn"
        onclick="closeProductDetails()">
        ×
      </button>

      <span class="product-category">
        ${product.category || "Digital Product"}
      </span>

      <h2>
        ${product.name || "Product"}
      </h2>

      ${
        images.length > 0
          ? `
            <div class="demo-gallery">

              <img
                id="demo-preview-image"
                src="${firstImage}"
                alt="${product.name || "Product"}"
                class="demo-preview-image"
              >

              <div
                id="demo-image-error"
                style="
                  display:none;
                  padding:40px;
                  text-align:center;
                  color:#94a3b8;
                ">
                Demo image could not be loaded.
              </div>

              ${
                images.length > 1
                  ? `
                    <div class="demo-gallery-buttons">

                      <button
                        type="button"
                        id="demo-prev-btn">
                        ❮
                      </button>

                      <span id="demo-image-count">
                        1 / ${images.length}
                      </span>

                      <button
                        type="button"
                        id="demo-next-btn">
                        ❯
                      </button>

                    </div>
                  `
                  : ""
              }

            </div>
          `
          : `
            <div class="no-demo-image">
              No demo images available.
            </div>
          `
      }

      <p>
        ${product.description || ""}
      </p>

      <div class="details-price">
        ₹${product.price ?? 0}
      </div>

      <p>
        <strong>Product Type:</strong>
        ${product.productType || "PDF"}
      </p>

      <button
        type="button"
        class="checkout-btn"
        onclick="checkoutProduct(${product.id})">
        Continue to Checkout
      </button>

    </div>
  `;

  document.body.appendChild(details);

  /* IMAGE ERROR */

  const demoImage = document.getElementById("demo-preview-image");

  if (demoImage) {
    demoImage.onerror = function () {
      this.style.display = "none";

      const errorBox = document.getElementById("demo-image-error");

      if (errorBox) {
        errorBox.style.display = "block";
      }
    };
  }

  /* IMAGE SLIDER */

  if (images.length > 1) {
    const imageElement = document.getElementById("demo-preview-image");

    const countElement = document.getElementById("demo-image-count");

    const prevButton = document.getElementById("demo-prev-btn");

    const nextButton = document.getElementById("demo-next-btn");

    function showImage(index) {
      currentImage = index;

      imageElement.style.display = "block";

      imageElement.src = getFileUrl(images[currentImage], "previews");

      countElement.textContent = `${currentImage + 1} / ${images.length}`;
    }

    prevButton.onclick = function () {
      currentImage--;

      if (currentImage < 0) {
        currentImage = images.length - 1;
      }

      showImage(currentImage);
    };

    nextButton.onclick = function () {
      currentImage++;

      if (currentImage >= images.length) {
        currentImage = 0;
      }

      showImage(currentImage);
    };
  }
}

/* =========================================================
   CLOSE PRODUCT DETAILS
   ========================================================= */

function closeProductDetails() {
  const details = document.querySelector(".product-details-overlay");

  if (details) {
    details.remove();
  }
}

/* =========================================================
   CHECKOUT
   ========================================================= */

async function checkoutProduct(productId) {
  const storedUser = localStorage.getItem("loggedInUser");

  if (!storedUser) {
    alert("Please login first.");

    localStorage.setItem("pendingProductId", productId);

    window.location.href = "login.html";

    return;
  }

  let user;

  try {
    user = JSON.parse(storedUser);
  } catch (error) {
    console.error("Invalid login data:", error);

    localStorage.removeItem("loggedInUser");

    alert("Please login again.");

    window.location.href = "login.html";

    return;
  }

  if (!user.email) {
    alert("User email not found.");

    return;
  }

  const email = user.email;

  try {
    /* CHECK ALREADY PURCHASED */

    const response = await fetch(
      `${API_BASE}/api/payment/purchased` +
        `?email=${encodeURIComponent(email)}` +
        `&productId=${productId}`,
    );

    if (!response.ok) {
      throw new Error(`Purchase check failed: ${response.status}`);
    }

    const alreadyPurchased = await response.json();

    /* GET PRODUCT */

    const product = allProducts.find((p) => Number(p.id) === Number(productId));

    if (!product) {
      alert("Product not found.");

      return;
    }

    /* ALREADY PURCHASED */

    if (alreadyPurchased) {
      const downloadAgain = confirm(
        "You have already purchased this product ✅\n\n" +
          "Do you want to download it again?",
      );

      if (downloadAgain) {
        downloadPurchasedProduct(productId);
      }

      return;
    }

    /* NEW PURCHASE */

    closeProductDetails();

    const oldCheckout = document.querySelector(".checkout-overlay");

    if (oldCheckout) {
      oldCheckout.remove();
    }

    const checkoutBox = document.createElement("div");

    checkoutBox.className = "checkout-overlay";

    checkoutBox.innerHTML = `
      <div class="checkout-box">

        <button
          type="button"
          class="close-checkout"
          onclick="
            this.parentElement.parentElement.remove()
          ">
          ×
        </button>

        <h2>
          Checkout
        </h2>

        <div class="checkout-product">

          <h3>
            ${product.name || "Product"}
          </h3>

          <p>
            ${product.description || ""}
          </p>

          <h3>
            Price: ₹${product.price}
          </h3>

        </div>

        <h3>
          Customer Details
        </h3>

        <input
          type="text"
          id="customerName"
          value="${user.name || ""}"
          placeholder="Enter your name"
        >

        <input
          type="email"
          id="customerEmail"
          value="${user.email || ""}"
          placeholder="Enter your email"
        >

        <input
          type="text"
          id="customerPhone"
          value="${user.phone || ""}"
          placeholder="Enter mobile number"
          maxlength="10"
        >

        <button
          type="button"
          class="pay-button"
          onclick="
            startPayment(
              ${product.id},
              ${product.price}
            )
          ">
          Pay ₹${product.price}
        </button>

      </div>
    `;

    document.body.appendChild(checkoutBox);
  } catch (error) {
    console.error("Checkout Error:", error);

    alert("Unable to check previous purchase.");
  }
}

/* =========================================================
   START RAZORPAY PAYMENT
   ========================================================= */

async function startPayment(productId, price) {
  const nameElement = document.getElementById("customerName");

  const emailElement = document.getElementById("customerEmail");

  const phoneElement = document.getElementById("customerPhone");

  if (!nameElement || !emailElement || !phoneElement) {
    alert("Checkout form not found.");

    return;
  }

  const name = nameElement.value.trim();

  const email = emailElement.value.trim();

  const phone = phoneElement.value.trim();

  /* VALIDATION */

  if (!name) {
    alert("Please enter your name");

    return;
  }

  if (!email) {
    alert("Please enter your email");

    return;
  }

  if (!phone) {
    alert("Please enter your mobile number");

    return;
  }

  if (!/^[0-9]{10}$/.test(phone)) {
    alert("Please enter a valid 10-digit mobile number");

    return;
  }

  try {
    /* CREATE ORDER */

    const response = await fetch(
      `${API_BASE}/api/payment/create-order` +
        `?productId=${productId}` +
        `&amount=${price}`,
      {
        method: "POST",
      },
    );

    const responseText = await response.text();

    if (!response.ok) {
      alert("Payment order could not be created.\n\n" + responseText);

      return;
    }

    let order;

    try {
      order = JSON.parse(responseText);
    } catch (error) {
      console.error("Invalid order response:", responseText);

      alert("Invalid response received from payment server.");

      return;
    }

    /* RAZORPAY CHECK */

    if (typeof Razorpay === "undefined") {
      alert("Razorpay library is not loaded.");

      return;
    }

    /* RAZORPAY OPTIONS */

    const options = {
      key: order.keyId,

      amount: order.amount,

      currency: order.currency || "INR",

      name: "Zam Digital Store",

      description: "Digital Product Purchase",

      order_id: order.orderId,

      prefill: {
        name: name,
        email: email,
        contact: "+91" + phone,
      },

      theme: {
        color: "#312e81",
      },

      handler: async function (paymentResponse) {
        try {
          const params = new URLSearchParams();

          params.append("paymentId", paymentResponse.razorpay_payment_id);

          params.append("orderId", paymentResponse.razorpay_order_id);

          params.append("signature", paymentResponse.razorpay_signature);

          params.append("productId", productId);

          params.append("customerName", name);

          params.append("customerEmail", email);

          params.append("customerPhone", phone);

          params.append("amount", price);

          /* VERIFY */

          const verifyResponse = await fetch(`${API_BASE}/api/payment/verify`, {
            method: "POST",

            headers: {
              "Content-Type": "application/x-www-form-urlencoded",
            },

            body: params.toString(),
          });

          const verifyResult = await verifyResponse.text();

          if (!verifyResponse.ok) {
            alert("Payment verification failed ❌\n\n" + verifyResult);

            return;
          }

          alert(
            "Payment Verified Successfully! ✅\n\n" +
              "Payment ID: " +
              paymentResponse.razorpay_payment_id +
              "\n\n" +
              "Your payment is successful. " +
              "Your PDF download will start now.",
          );

          /* DOWNLOAD */

          const storedUser = localStorage.getItem("loggedInUser");

          if (!storedUser) {
            alert("Payment successful, but please login again to download.");

            return;
          }

          const currentUser = JSON.parse(storedUser);

          if (!currentUser.email) {
            alert("User email not found.");

            return;
          }

          const downloadUrl =
            `${API_BASE}/api/secure/download/` +
            `${productId}?email=` +
            encodeURIComponent(currentUser.email);

          const downloadLink = document.createElement("a");

          downloadLink.href = downloadUrl;

          downloadLink.target = "_blank";

          downloadLink.rel = "noopener";

          document.body.appendChild(downloadLink);

          downloadLink.click();

          downloadLink.remove();
        } catch (error) {
          console.error("Verification Error:", error);

          alert("Payment successful, but verification failed ❌");
        }
      },

      modal: {
        ondismiss: function () {
          console.log("Razorpay Checkout closed.");
        },
      },
    };

    const razorpay = new Razorpay(options);

    razorpay.on("payment.failed", function (response) {
      console.error("Payment Failed:", response.error);

      alert("Payment Failed ❌\n\n" + "Reason: " + response.error.description);
    });

    razorpay.open();
  } catch (error) {
    console.error("Payment Error:", error);

    alert("Unable to connect to payment server.");
  }
}

/* =========================================================
   PURCHASE HISTORY
   ========================================================= */

async function loadPurchaseHistory() {
  const storedUser = localStorage.getItem("loggedInUser");

  if (!storedUser) {
    alert("Please login first.");

    return;
  }

  let user;

  try {
    user = JSON.parse(storedUser);
  } catch (error) {
    alert("Please login again.");

    return;
  }

  try {
    const response = await fetch(
      `${API_BASE}/api/payment/history` +
        `?email=${encodeURIComponent(user.email)}`,
    );

    if (!response.ok) {
      throw new Error("History could not be loaded");
    }

    const history = await response.json();

    showPurchaseHistory(history);
  } catch (error) {
    console.error("Purchase History Error:", error);

    alert("Purchase history could not be loaded.");
  }
}

/* =========================================================
   SHOW PURCHASE HISTORY
   ========================================================= */

async function showPurchaseHistory(history) {
  const oldHistory = document.querySelector(".purchase-history-overlay");

  if (oldHistory) {
    oldHistory.remove();
  }

  let products = [];

  try {
    const response = await fetch(`${API_BASE}/api/products`);

    if (response.ok) {
      products = await response.json();
    }
  } catch (error) {
    console.error("Products load error:", error);
  }

  const overlay = document.createElement("div");

  overlay.className = "purchase-history-overlay";

  overlay.innerHTML = `
    <div class="purchase-history-box">

      <button
        type="button"
        class="close-checkout"
        onclick="
          this.parentElement.parentElement.remove()
        ">
        ×
      </button>

      <h2>
        Purchase History
      </h2>

      ${
        !history || history.length === 0
          ? `
            <div class="history-item">
              You have not purchased anything yet.
            </div>
          `
          : history
              .map((payment) => {
                const product = products.find(
                  (p) => Number(p.id) === Number(payment.productId),
                );

                const productName = product
                  ? product.name
                  : `Product #${payment.productId}`;

                return `
                  <div class="history-item">

                    <h3>
                      📘 ${productName}
                    </h3>

                    <p>
                      <strong>Amount:</strong>
                      ₹${payment.amount}
                    </p>

                    <p>
                      <strong>Product ID:</strong>
                      ${payment.productId}
                    </p>

                    <p>
                      <strong>Status:</strong>
                      ${payment.status}
                    </p>

                    <p>
                      <strong>Date:</strong>
                      ${
                        payment.createdAt
                          ? new Date(payment.createdAt).toLocaleString()
                          : "N/A"
                      }
                    </p>

                    <button
                      type="button"
                      class="pay-button"
                      onclick="
                        downloadPurchasedProduct(
                          ${payment.productId}
                        )
                      ">
                      📥 Download PDF
                    </button>

                  </div>
                `;
              })
              .join("")
      }

    </div>
  `;

  document.body.appendChild(overlay);
}

/* =========================================================
   DOWNLOAD PURCHASED PRODUCT
   ========================================================= */

function downloadPurchasedProduct(productId) {
  const storedUser = localStorage.getItem("loggedInUser");

  if (!storedUser) {
    alert("Please login first.");

    return;
  }

  let user;

  try {
    user = JSON.parse(storedUser);
  } catch (error) {
    alert("Please login again.");

    return;
  }

  if (!user.email) {
    alert("User email not found.");

    return;
  }

  const downloadUrl =
    `${API_BASE}/api/secure/download/` +
    `${productId}?email=` +
    encodeURIComponent(user.email);

  window.open(downloadUrl, "_blank");
}

/* =========================================================
   HOME
   ========================================================= */

function goHome() {
  displayProducts(allProducts);

  window.scrollTo({
    top: 0,
    behavior: "smooth",
  });

  closeMobileMenu();
}

/* =========================================================
   SHOW ALL PRODUCTS
   ========================================================= */

function showAllProducts() {
  displayProducts(allProducts);

  const productsSection = document.querySelector(".products-section");

  if (productsSection) {
    productsSection.scrollIntoView({
      behavior: "smooth",
      block: "start",
    });
  }

  closeMobileMenu();
}

/* =========================================================
   CATEGORY FILTER
   ========================================================= */

function showCategory(category) {
  const filteredProducts = allProducts.filter((product) => {
    const productCategory = String(product.category || "")
      .trim()
      .toLowerCase();

    return productCategory === String(category).trim().toLowerCase();
  });

  displayProducts(filteredProducts);

  const productsSection = document.querySelector(".products-section");

  if (productsSection) {
    productsSection.scrollIntoView({
      behavior: "smooth",
      block: "start",
    });
  }

  closeMobileMenu();
}

/* =========================================================
   ABOUT
   ========================================================= */

function scrollToAbout() {
  const section = document.getElementById("about-section");

  closeMobileMenu();

  if (section) {
    setTimeout(() => {
      section.scrollIntoView({
        behavior: "smooth",
        block: "start",
      });
    }, 100);
  }
}

/* =========================================================
   CONTACT
   ========================================================= */

function scrollToContact() {
  const section = document.getElementById("contact-section");

  closeMobileMenu();

  if (section) {
    setTimeout(() => {
      section.scrollIntoView({
        behavior: "smooth",
        block: "start",
      });
    }, 100);
  }
}

/* =========================================================
   COURSES
   ========================================================= */

function goCourses() {
  closeMobileMenu();

  const coursesSection = document.getElementById("courses-section");

  if (coursesSection) {
    setTimeout(() => {
      coursesSection.scrollIntoView({
        behavior: "smooth",
        block: "start",
      });
    }, 100);

    return;
  }

  window.location.href = "courses.html";
}

/* =========================================================
   HAMBURGER MENU
   ========================================================= */

function openMobileMenu() {
  const menu = document.getElementById("sideMenu");

  const overlay = document.getElementById("sideMenuOverlay");

  if (menu) {
    menu.classList.add("active");
  }

  if (overlay) {
    overlay.classList.add("active");
  }

  document.body.style.overflow = "hidden";
}

/* =========================================================
   CLOSE HAMBURGER MENU
   ========================================================= */
function closeMobileMenu() {
  const menu = document.getElementById("sideMenu");
  const overlay = document.getElementById("sideMenuOverlay");

  if (menu) {
    menu.classList.remove("active");
  }

  if (overlay) {
    overlay.classList.remove("active");
  }
}

/* =========================================================
   DOM READY
   ========================================================= */

document.addEventListener("DOMContentLoaded", function () {
  /* AUTH */

  updateAuthButtons();

  updateSideUserMenu();

  /* ==========================================
       HAMBURGER
       ========================================== */

  const menuToggle = document.getElementById("menuToggle");

  const closeMenuButton = document.getElementById("closeMenu");

  const sideMenuOverlay = document.getElementById("sideMenuOverlay");

  if (menuToggle) {
    menuToggle.addEventListener("click", function () {
      openMobileMenu();
    });
  }

  /* X BUTTON */

  if (closeMenuButton) {
    closeMenuButton.addEventListener("click", function () {
      closeMobileMenu();
    });
  }

  /* OUTSIDE CLICK */

  if (sideMenuOverlay) {
    sideMenuOverlay.addEventListener("click", function () {
      closeMobileMenu();
    });
  }

  /* ESC KEY */

  /* =========================================================
   SIDE MENU
   ========================================================= */

  document.addEventListener("DOMContentLoaded", function () {
    const menuToggle = document.getElementById("menuToggle");
    const sideMenu = document.getElementById("sideMenu");
    const closeMenu = document.getElementById("closeMenu");
    const sideMenuOverlay = document.getElementById("sideMenuOverlay");

    function openSideMenu() {
      if (sideMenu) {
        sideMenu.classList.add("active");
      }

      if (sideMenuOverlay) {
        sideMenuOverlay.classList.add("active");
      }

      // Page scrolling lock
      document.body.classList.add("menu-open");
    }

    function closeSideMenu() {
      if (sideMenu) {
        sideMenu.classList.remove("active");
      }

      if (sideMenuOverlay) {
        sideMenuOverlay.classList.remove("active");
      }

      // IMPORTANT:
      // Page scrolling unlock
      document.body.classList.remove("menu-open");

      // Extra safety
      document.body.style.overflow = "";
      document.documentElement.style.overflow = "";
    }

    /* OPEN */
    if (menuToggle) {
      menuToggle.addEventListener("click", function () {
        openSideMenu();
      });
    }

    /* CLOSE USING X */
    if (closeMenu) {
      closeMenu.addEventListener("click", function () {
        closeSideMenu();
      });
    }

    /* CLOSE USING DARK OVERLAY */
    if (sideMenuOverlay) {
      sideMenuOverlay.addEventListener("click", function () {
        closeSideMenu();
      });
    }

    /* ESC KEY */
    document.addEventListener("keydown", function (event) {
      if (event.key === "Escape") {
        closeSideMenu();
      }
    });

    /* CLOSE MENU AFTER CLICKING MENU LINK */
    const sideMenuLinks = document.querySelectorAll(".side-menu-link");

    sideMenuLinks.forEach(function (link) {
      link.addEventListener("click", function () {
        closeSideMenu();
      });
    });
  });

  /* ==========================================
       DESKTOP NAVIGATION
       ========================================== */

  const mainNavLinks = document.querySelectorAll(".main-nav-link");

  mainNavLinks.forEach(function (link) {
    link.addEventListener("click", function (event) {
      const href = link.getAttribute("href");

      if (href === "#about-section") {
        event.preventDefault();

        scrollToAbout();

        return;
      }

      if (href === "#contact-section") {
        event.preventDefault();

        scrollToContact();

        return;
      }

      if (href === "#courses-section") {
        event.preventDefault();

        goCourses();

        return;
      }
    });
  });

  /* ==========================================
       COURSE CATEGORY BUTTONS
       ========================================== */

  const categoryButtons = document.querySelectorAll(".course-category");

  categoryButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      const category = button.dataset.category;

      if (!category) {
        return;
      }

      showCategory(category);

      categoryButtons.forEach(function (btn) {
        btn.classList.remove("active");
      });

      button.classList.add("active");
    });
  });

  /* ==========================================
       CONTACT FORM
       ========================================== */

  const contactForm = document.getElementById("contactForm");

  if (contactForm) {
    contactForm.addEventListener("submit", function (event) {
      event.preventDefault();

      const name = document.getElementById("contactName")?.value.trim();

      const email = document.getElementById("contactEmail")?.value.trim();

      const subject = document.getElementById("contactSubject")?.value.trim();

      const message = document.getElementById("contactMessage")?.value.trim();

      if (!name || !email || !subject || !message) {
        alert("Please fill all fields.");

        return;
      }

      alert("Thank you, " + name + "! Your message has been received. ✅");

      contactForm.reset();
    });
  }
});

/* =========================================================
   START APPLICATION
   ========================================================= */

loadProducts().then(function () {
  const pendingProductId = localStorage.getItem("pendingProductId");

  if (pendingProductId) {
    localStorage.removeItem("pendingProductId");

    setTimeout(function () {
      checkoutProduct(Number(pendingProductId));
    }, 500);
  }
});
function buyProductDirect(productId) {
  alert("Buy Now clicked! Product ID: " + productId);
}

/* =========================================================
   DIRECT BUY PRODUCT
   ========================================================= */

async function buyProductDirect(productId) {
  try {
    // Product find karo
    let product = allProducts.find((p) => Number(p.id) === Number(productId));

    // Agar product allProducts me nahi mila
    if (!product) {
      const response = await fetch(`${API_BASE}/api/products/${productId}`);

      if (!response.ok) {
        throw new Error("Product not found");
      }

      product = await response.json();
    }

    // Customer details
    const customerName = prompt("Enter your name:");

    if (!customerName) {
      return;
    }

    const customerEmail = prompt("Enter your email:");

    if (!customerEmail) {
      return;
    }

    const customerPhone = prompt("Enter your phone number:");

    if (!customerPhone) {
      return;
    }

    // Create Razorpay Order
    const orderResponse = await fetch(
      `${API_BASE}/api/payment/create-order?productId=${productId}`,
      {
        method: "POST",
      },
    );

    if (!orderResponse.ok) {
      const errorText = await orderResponse.text();

      throw new Error(errorText || "Order creation failed");
    }

    const order = await orderResponse.json();

    console.log("Razorpay Order:", order);

    // Razorpay Checkout
    const options = {
      key: order.keyId,

      amount: order.amount,

      currency: order.currency,

      name: "Zam Digital Store",

      description: product.name || "Digital Product",

      order_id: order.orderId,

      prefill: {
        name: customerName,

        email: customerEmail,

        contact: customerPhone,
      },

      theme: {
        color: "#4f46e5",
      },

      handler: async function (paymentResponse) {
        try {
          console.log("Payment Response:", paymentResponse);

          // Verify Payment
          const verifyUrl =
            `${API_BASE}/api/payment/verify?` +
            `paymentId=${encodeURIComponent(paymentResponse.razorpay_payment_id)}` +
            `&orderId=${encodeURIComponent(paymentResponse.razorpay_order_id)}` +
            `&signature=${encodeURIComponent(paymentResponse.razorpay_signature)}` +
            `&productId=${encodeURIComponent(productId)}` +
            `&customerName=${encodeURIComponent(customerName)}` +
            `&customerEmail=${encodeURIComponent(customerEmail)}` +
            `&customerPhone=${encodeURIComponent(customerPhone)}`;

          const verifyResponse = await fetch(verifyUrl, {
            method: "POST",
          });

          const result = await verifyResponse.text();

          if (!verifyResponse.ok) {
            throw new Error(result || "Payment verification failed");
          }

          alert("Payment successful!\n\n" + "Your payment has been verified.");

          console.log("Payment verified:", result);
        } catch (error) {
          console.error("Payment verification error:", error);

          alert("Payment verification failed.\n" + error.message);
        }
      },

      modal: {
        ondismiss: function () {
          console.log("Payment window closed");
        },
      },
    };

    // Razorpay loaded hai ya nahi check karo
    if (typeof Razorpay === "undefined") {
      alert("Razorpay Checkout is not loaded.");

      return;
    }

    const razorpay = new Razorpay(options);

    razorpay.on("payment.failed", function (response) {
      console.error("Payment Failed:", response.error);

      alert("Payment failed.\n\n" + response.error.description);
    });

    razorpay.open();
  } catch (error) {
    console.error("Direct Buy Error:", error);

    alert("Unable to start payment.\n\n" + error.message);
  }
}
async function buyProductDirect(productId) {
  try {
    console.log("Buy Now clicked:", productId);

    let product = allProducts.find((p) => Number(p.id) === Number(productId));

    // Agar product allProducts me nahi mila
    if (!product) {
      const response = await fetch(`${API_BASE}/api/products/${productId}`);

      if (!response.ok) {
        throw new Error("Product not found");
      }

      product = await response.json();
    }

    if (!product) {
      alert("Product details not found.");
      return;
    }

    // Customer details
    const customerName = prompt("Enter your name:");

    if (!customerName) {
      return;
    }

    const customerEmail = prompt("Enter your email:");

    if (!customerEmail) {
      return;
    }

    const customerPhone = prompt("Enter your phone number:");

    if (!customerPhone) {
      return;
    }

    // Create Razorpay Order
    const response = await fetch(
      `${API_BASE}/api/payment/create-order?productId=${productId}`,
      {
        method: "POST",
      },
    );

    if (!response.ok) {
      const errorText = await response.text();

      console.error("Order creation error:", errorText);

      alert("Razorpay order are not create.");

      return;
    }

    const orderData = await response.json();

    console.log("Order created:", orderData);

    // Razorpay Checkout
    const options = {
      key: orderData.keyId,

      amount: orderData.amount,

      currency: orderData.currency,

      name: "Zam Digital Store",

      description: product.name,

      order_id: orderData.orderId,

      handler: async function (paymentResponse) {
        console.log("Payment successful:", paymentResponse);

        try {
          const verifyUrl =
            `${API_BASE}/api/payment/verify` +
            `?paymentId=${encodeURIComponent(paymentResponse.razorpay_payment_id)}` +
            `&orderId=${encodeURIComponent(paymentResponse.razorpay_order_id)}` +
            `&signature=${encodeURIComponent(paymentResponse.razorpay_signature)}` +
            `&productId=${encodeURIComponent(productId)}` +
            `&customerName=${encodeURIComponent(customerName)}` +
            `&customerEmail=${encodeURIComponent(customerEmail)}` +
            `&customerPhone=${encodeURIComponent(customerPhone)}`;

          const verifyResponse = await fetch(verifyUrl, {
            method: "POST",
          });

          const result = await verifyResponse.text();

          console.log("Verification result:", result);

          if (verifyResponse.ok) {
            alert(
              "Payment successful! 🎉\n\n" + "Your product has been purchased.",
            );
          } else {
            alert("Payment done, but verification failed.\n" + result);
          }
        } catch (error) {
          console.error("Payment verification error:", error);

          alert("problem in Payment verification.");
        }
      },

      prefill: {
        name: customerName,

        email: customerEmail,

        contact: customerPhone,
      },

      theme: {
        color: "#3399cc",
      },
    };

    const razorpay = new Razorpay(options);

    razorpay.open();
  } catch (error) {
    console.error("Buy Now Error:", error);

    alert("Payment are not start.\n" + error.message);
  }
}
// ==========================================
// CONTACT FORM
// ==========================================

const contactForm = document.getElementById("contactForm");

if (contactForm) {
  contactForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    const name = document.getElementById("contactName").value.trim();

    const email = document.getElementById("contactEmail").value.trim();

    const subject = document.getElementById("contactSubject").value.trim();

    const message = document.getElementById("contactMessage").value.trim();

    if (!name || !email || !subject || !message) {
      alert("Please fill all fields.");

      return;
    }

    const contactData = {
      name: name,

      email: email,

      subject: subject,

      message: message,
    };

    try {
      const response = await fetch("/api/contact", {
        method: "POST",

        headers: {
          "Content-Type": "application/json",
        },

        body: JSON.stringify(contactData),
      });

      const result = await response.text();

      if (response.ok) {
        alert("✅ Message sent successfully! We will contact you soon.");

        contactForm.reset();
      } else {
        alert("❌ Failed to send message.");
      }
    } catch (error) {
      console.error("Contact form error:", error);

      alert("❌ Server error. Please try again later.");
    }
  });
}
