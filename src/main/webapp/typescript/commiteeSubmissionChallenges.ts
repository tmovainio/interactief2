import { createFadeoutNotification, getSession, getToken, isLoggedIn, sendGet, sendPatch } from "./api.js";
import { Challenge, Session, Submission } from "./types.js";

(async () => {
    // We need to be logged in
    if (!await isLoggedIn()) {
        document.location.href = "/public-pages/login.html";
        return;
    }

    // Need to be admin
    const session: Session = JSON.parse(localStorage.getItem("session"));
    if (session.user?.admin === false) {
        document.location.href = "/user-pages/user-dashboard.html";
        return;
    }

    // Get the team name and problem id from the query string
    const urlParams = new URLSearchParams(window.location.search);
    const teamName = urlParams.get("team_name");
    const problemId = urlParams.get("problem_id");

    // Get the submission
    const res: Response = await sendGet(`submissions/admin/${teamName}/${problemId}`, { "Authorization": `${getToken()}` });
    const submission: Submission = (await res.json()).submission;

    const challengeRes: Response = await sendGet(`challenges/${submission.problem_id}`, { "Authorization": `${getToken()}` });
    const challenge: Challenge = (await challengeRes.json()).challenge;

    document.getElementById("sub_team").innerText = submission.team_name;
    document.getElementById("sub_challenge").innerText = submission.problem_id.toString();
    document.getElementById("sub_location").innerText = challenge.location.location_name;

    document.getElementById("grade_ph").innerText = `/ ${challenge.score}`;

    // Preview image
    const fileRes: Response = await sendGet(`submissions/admin/${teamName}/${problemId}/file`, { "Authorization": `${getToken()}` });
    if (fileRes.status !== 404) {
        const reader: FileReader = new FileReader();
        reader.onload = (e) => {
            (<HTMLImageElement>document.getElementById("preview")).src = e.target.result.toString();
            document.getElementById("preview").hidden = false;
        }

        reader.readAsDataURL(await fileRes.blob());
    } else { 
        (<HTMLImageElement>document.getElementById("preview")).src = "/images/University_of_Twente_panoramic.jpg";
        document.getElementById("preview").hidden = false;
    }

    document.getElementById("submit").addEventListener("click", async (e) => {
        e.preventDefault();

        const grade: number = parseInt((<HTMLInputElement>document.getElementById("grade")).value);
        const description = (<HTMLInputElement>document.getElementById("notes")).value;

        const res: Response = await sendPatch(`submissions/admin/${teamName}/${problemId}`, { "score": grade, "grading_description": description }, { "Authorization": `${getToken()}` });

        if (res.status === 200) {
            document.location.href = "/commitee-pages/submissions.html";
            return;
        } else {
            const main = document.getElementsByTagName("main")[0];
            main.prepend(createFadeoutNotification("Something went wrong grading this submission!", "error"));
        }


    });

})();