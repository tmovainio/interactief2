(function () {
    var logoutButton = document.getElementById("logout-button");
    if (logoutButton == null)
        return;
    logoutButton.addEventListener("click", function () {
        console.log("Logging out!");
        localStorage.removeItem("session");
        window.location.href = "/public-pages/login.html";
        return;
    });
})();
//# sourceMappingURL=main.js.map