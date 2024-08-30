import { sendPost, isLoggedIn, getSession } from "./api.js";
import { Session } from "./types.js";

const sendLogin = async (sid: Number, password: String) => {
    return sendPost('login', { "student_number": sid, "password": password }, {});
};

type LoginResponse = {
    JWTToken: string
};

document.getElementById("loginButton").addEventListener("click", async (e) => {
    e.preventDefault();
    // Set button to loading animation
    document.getElementById("loginButton").innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Logging in...`;
    (document.getElementById("loginButton") as HTMLInputElement).disabled = true;
    const sid: Number = Number((document.getElementById("sid") as HTMLInputElement).value);
    const password: String = (document.getElementById("pass") as HTMLInputElement).value;

    const login: Response = await sendLogin(sid, password);
    document.getElementById("loginButton").innerHTML = `Login`;
    (document.getElementById("loginButton") as HTMLInputElement).disabled = false;
    if (login.status === 200) {
        const response: LoginResponse = await login.json();
        const session: Session = await getSession(response.JWTToken);
        localStorage.setItem("session", JSON.stringify(session));
        // If we are admin we should go to the commitee dashboard
        if (session.user.admin) {
            window.location.href = "/commitee-pages/commitee-dash.html";
            return;
        }
        // Otherwise we should go to the user dashboard
        window.location.href = "/user-pages/user-dashboard.html";
        return;
    } else if (login.status === 401) {
        document.getElementById("sid").classList.add("is-invalid");
        document.getElementById("pass").classList.add("is-invalid");
        document.getElementById("loginErrorMessage").hidden = false;
        return;
    } else if (login.status === 400) {
        alert("Unknown error");
        return;
    } else if (login.status === 500) {
        alert("Server error");
    } else {
        alert("Login failed");
    }
});

(async () => {
    // If we are logged in we can just go to the dashboard page
    if (await isLoggedIn()) {
        // Are we admin?
        const session: Session = JSON.parse(localStorage.getItem("session"));
        if (session.user.admin) {
            window.location.href = "/commitee-pages/commitee-dash.html";
            return;
        }
        window.location.href = "/user-pages/user-dashboard.html";
        return;
    }


})();