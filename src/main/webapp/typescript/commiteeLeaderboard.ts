import { isLoggedIn, sendGet, getToken, sendPatch } from "./api.js";
import { Session } from "./types.js";

const displayInactive = () => {
    document.getElementById("leaderboardActive").classList.remove("text-bg-success");
    document.getElementById("leaderboardActive").classList.add("text-bg-danger");
    document.getElementById("leaderboardActive").innerText = "Status: Inactive";
}

const displayActive = () => {
    document.getElementById("leaderboardActive").classList.remove("text-bg-danger");
    document.getElementById("leaderboardActive").classList.add("text-bg-success");
    document.getElementById("leaderboardActive").innerText = "Status: Active";
}



(async () => {
    if (!await isLoggedIn()) {
        window.location.href = "/public-pages/login.html";
        return;
    }
    const session: Session = JSON.parse(localStorage.getItem("session"));
    if (session.user?.admin === false) {
        window.location.href = "/user-pages/user-dashboard.html";
        return;
    }

    // Get leaderboard status
    const reponse: Response = await sendGet("leaderboard/availability", { "Authorization": getToken() });
    const status: { visible: boolean } = await reponse.json();
    // const status: { visible: boolean } = { visible: true };
    
    if (!status.visible) {
        displayInactive();
    }

    document.getElementById("leaderboard-toggle").addEventListener("click", async () => {
        await sendPatch("leaderboard/availability", { visible: !status.visible }, { "Authorization": getToken() });
        status.visible = !status.visible;
        if (status.visible) {
            displayActive();
        } else {
            displayInactive();
        }
    });    

})();