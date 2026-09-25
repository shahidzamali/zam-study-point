const registerForm = document.querySelector("#registerForm");

registerForm.addEventListener("submit", async function (e) {
  e.preventDefault();

  const name = document.querySelector("#name").value.trim();
  const email = document.querySelector("#email").value.trim();
  const phone = document.querySelector("#phone").value.trim();
  const password = document.querySelector("#password").value;

  try {
    const response = await fetch("/api/user/register", {
      method: "POST",

      headers: {
        "Content-Type": "application/json",
      },

      body: JSON.stringify({
        name: name,
        email: email,
        phone: phone,
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
      alert(data || "Registration failed ❌");
      return;
    }

    alert("Account created successfully! ✅");

    // Login page par jao
    window.location.href = "login.html";
  } catch (error) {
    console.error(error);
    alert("Server se connection nahi ho raha ❌");
  }
});
