import { isLoggedIn } from "./api.js";
import { Session, Team } from "./types.js";

(async () => {
    // Check if we are logged in
    // If we are not, send to the login page
    if (!(await isLoggedIn())) {
        window.location.href = "/public-pages/login.html";
        return;
    }

    // Check if the user is an admin
    // If they are not, send them to the join/create team page
    const session: Session = JSON.parse(localStorage.getItem("session"));
    if (!session.user?.admin) {
        window.location.href = "/user-pages/jointeam.html";
    }

    // if (session.team == null && !session.user?.admin) {
    //     window.location.href = "/user-pages/jointeam.html";
    //     return;
    // }

    // // check if we're commitee
    // if (!session.user?.admin) {
    //     window.location.href = "/user-pages/user-dashboard.html";
    //     return;
    // }


    // Set the profile information
    document.getElementById("profileName").innerText = session.user.name;
    document.getElementById("profilePhoneNumber").innerText = session.user.phone_numb;
    document.getElementById("profileStudentNumber").innerText = session.user.s_numb.toString();
    (document.getElementById("name") as HTMLInputElement).value = session.user.name;
    (document.getElementById("phoneNumber") as HTMLInputElement).value = session.user.phone_numb;
})();