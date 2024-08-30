(() => {
    // Logout button
    const logoutButton: HTMLButtonElement = document.getElementById("logout-button") as HTMLButtonElement;
    if (logoutButton == null) return;
    logoutButton.addEventListener("click", () => {
        console.log("Logging out!");
        localStorage.removeItem("session");
        window.location.href = "/public-pages/login.html";
        return;
    });
})();