import { sendPost, isLoggedIn } from "./api.js";

const sendRegister = async (sid: String, password: String, phoneNumber: String, name: String): Promise<Boolean> => {
    const response = await sendPost('register', { "s_numb": sid, "password": password, "phone_numb": phoneNumber, "name": name }, {});
    return new Promise((resolve, _) => {
        resolve(response.status === 200);
    });
};

const registerButton: HTMLButtonElement = document.getElementById("registerButton") as HTMLButtonElement;

registerButton.addEventListener("click", async (e) => {
    e.preventDefault();
    const nameElement: HTMLInputElement = document.getElementById("name") as HTMLInputElement;
    const sidElement: HTMLInputElement = document.getElementById("sid") as HTMLInputElement;
    const passwordElement: HTMLInputElement = document.getElementById("pass") as HTMLInputElement;
    const phoneNumberElement: HTMLInputElement = document.getElementById("phonenumber") as HTMLInputElement;
    // Set button to loading animation
    registerButton.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Registering...`;
    (registerButton as HTMLInputElement).disabled = true;
    const name = nameElement.value;
    const sid = sidElement.value;
    const password = passwordElement.value;
    const phoneNumber = phoneNumberElement.value;
    const register: Boolean = await sendRegister(sid, password, phoneNumber, name);
    if (register) {
        window.location.href = "/public-pages/login.html";
        return;
    } else {
        registerButton.innerHTML = `Register`;
        (registerButton as HTMLInputElement).disabled = false;
        nameElement.classList.add("is-invalid");
        sidElement.classList.add("is-invalid");
        passwordElement.classList.add("is-invalid");
        phoneNumberElement.classList.add("is-invalid");
        document.getElementById("registerErrorMessage").hidden = false;
    }
});