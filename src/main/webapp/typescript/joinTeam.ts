import { isLoggedIn, sendPost, getToken, getSession } from "./api.js";
import { Session, Team } from "./types.js";

type JoinTeamResponse = {
    
};

(async () => {
    // If we are not logged in redirect the user to the login page
    if (!await isLoggedIn()) {
        window.location.href = "/public-pages/login.html";
        return;
    }
    // Check if we are admin
    const session: Session = JSON.parse(localStorage.getItem("session"));
    
    if (session.user?.admin) {
        window.location.href = "/commitee-pages/commitee-dash.html";
    }

    // Are they already in a team?
    if (session.team != null) {
        window.location.href = "/user-pages/user-dashboard.html";
        return;
    }

    // Stuff for when someone tries to join a team
    const joinTeamButton: HTMLButtonElement = document.getElementById("joinTeamButton") as HTMLButtonElement;
    const joinTeamCode: HTMLInputElement = document.getElementById("join-team-code") as HTMLInputElement;
    joinTeamButton.addEventListener("click", async (e) => {
        const code: string = joinTeamCode.value;
        const response: Response = await sendPost(`teams/join/${code}`, {}, { "Authorization": getToken() });
        if (response.status === 200) {
            await getSession(getToken());
            window.location.href = "/user-pages/user-dashboard.html";
            return;
        }
        // TODO handle errors
    });

    // Stuff for when someone tries to create a team
    const createTeamButton: HTMLButtonElement = document.getElementById("createTeamButton") as HTMLButtonElement;
    const createTeamName: HTMLInputElement = document.getElementById("create-team") as HTMLInputElement;
    createTeamButton.addEventListener("click", async () => {
        const name: string = createTeamName.value;
        // const participant: Participant = JSON.parse(localStorage.getItem("user"));
        const response: Response = await sendPost(`teams`, { teamName: name }, { "Authorization": getToken() });
        if (response.status === 200) {
            const createdTeam: Team = (await response.json()).team;
            session.team = createdTeam;
            localStorage.setItem("session", JSON.stringify(session));
            window.location.href = "/user-pages/user-dashboard.html";
            return;
        }
        // TODO handle errors
    });
})();