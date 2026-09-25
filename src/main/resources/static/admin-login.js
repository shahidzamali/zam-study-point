const adminLoginForm = document.querySelector("#adminLoginForm");

adminLoginForm.addEventListener("submit", async function (e) {
  e.preventDefault();

  const username = document.querySelector("#username").value.trim();
  const password = document.querySelector("#password").value;

  try {
    const params = new URLSearchParams();

    params.append("username", username);
    params.append("password", password);

    const response = await fetch("/api/admin/login", {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: params,
    });

    const result = await response.text();

    if (result === "SUCCESS") {
      localStorage.setItem("adminLoggedIn", "true");

      alert("Admin Login Successful ✅");

      window.location.href = "admin.html";
    } else {
      alert("Invalid username or password ❌");
    }
  } catch (error) {
    console.error("Admin Login Error:", error);

    alert("Server se connection nahi ho raha ❌");
  }
});
