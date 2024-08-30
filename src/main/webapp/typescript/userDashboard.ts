import { getToken, isLoggedIn, sendGet } from "./api.js";
import { Session, Team } from "./types.js";

(async () => {
    // Check if we are logged in
    // If we are not, send to the login page
    if (!(await isLoggedIn())) {
        window.location.href = "/public-pages/login.html";
        return;
    }

    // Check if the user is in a team
    // If they are not, send them to the join/create team page
    const session: Session = JSON.parse(localStorage.getItem("session"));
    console.log(session);
    if (session.team == null) {
        window.location.href = "/user-pages/jointeam.html";
        return;
    }


    // Set the teamname
    document.getElementById("dash-team-name").innerText = session.team.team_name;

    // Get the team stats


    // Set the profile information
    document.getElementById("profileName").innerText = session.user.name;
    (document.getElementById("name") as HTMLInputElement).value = session.user.name;
    document.getElementById("profilePhoneNumber").innerText = session.user.phone_numb;
    (document.getElementById("phoneNumber") as HTMLInputElement).value = session.user.phone_numb;
    document.getElementById("profileStudentNumber").innerText = session.user.s_numb.toString();

    // Set the rank info
    const rankingResponse: Response = await sendGet(`leaderboard/${session.team.team_name}`, { "Authorization": getToken() });
    const ranking: {
        rank: Number;
        challenges: Number;
        total_score: Number;
    } = (await rankingResponse.json()).score;
    
    document.getElementById("rank").innerText = ranking.rank.toString();
    // document.getElementById("challengesCompleted").innerText = ranking.challenges.toString();
    document.getElementById("points").innerText = ranking.total_score.toString();

})();