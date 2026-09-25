const loginForm = document.querySelector("#loginForm");

loginForm.addEventListener("submit", async function (e) {
  e.preventDefault();

  const email = document.querySelector("#email").value.trim();
  const password = document.querySelector("#password").value;

  try {
    const response = await fetch("/api/user/login", {
      method: "POST",

      headers: {
        "Content-Type": "application/json",
      },

      body: JSON.stringify({
        email: email,
        password: password,
      }),
    });

    const text = await response.text();

    let data;

    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }

    if (!response.ok) {
      alert(data || "Login failed ❌");
      return;
    }

    // Login user ko browser me save karo
    localStorage.setItem("loggedInUser", JSON.stringify(data));

    alert("Login Successful ✅");

    // Homepage par jao
    window.location.href = "index.html";
  } catch (error) {
    console.error(error);
    alert("Server se connection nahi ho raha ❌");
  }
});
