import { isLoggedIn, getSubmissions, sendGet, getToken } from "./api.js";
import { Session, Submission } from "./types.js";

const createUngradedRow = (submission: Submission, i: Number): HTMLTableRowElement => {
    console.log(submission);
    const tr = document.createElement("tr");
    const nr = document.createElement("th");
    nr.scope = "row";
    nr.innerText = i.toString();
    tr.appendChild(nr);

    const challenge = document.createElement("td");
    challenge.innerText = submission.problem_id.toString();
    tr.appendChild(challenge);

    const team = document.createElement("td");
    team.innerText = submission.team_name;
    tr.appendChild(team);

    const buttonTd = document.createElement("td");
    const button = document.createElement("button");
    button.addEventListener("click", () => document.location.href = `/commitee-pages/submission-challenges.html?team_name=${submission.team_name}&problem_id=${submission.problem_id}`);
    button.className = "btn btn-primary w-100";
    button.innerText = "Grade";
    buttonTd.appendChild(button);
    tr.appendChild(buttonTd);

    return tr;
};

const createGradedRow = (submission: Submission, i: Number): HTMLTableRowElement => {
    const tr = document.createElement("tr");
    const nr = document.createElement("th");
    nr.scope = "row";
    nr.innerText = i.toString();
    tr.appendChild(nr);

    const challenge = document.createElement("td");
    challenge.innerText = submission.problem_id.toString();
    tr.appendChild(challenge);

    const team = document.createElement("td");
    team.innerText = submission.team_name;
    tr.appendChild(team);

    const grade = document.createElement("td");
    grade.innerText = submission.score.toString();
    tr.appendChild(grade);

    const buttonTd = document.createElement("td");
    const button = document.createElement("button");
    button.addEventListener("click", () => document.location.href = `/commitee-pages/submission-challenges.html?team_name=${submission.team_name}&problem_id=${submission.problem_id}`);
    button.className = "btn btn-primary w-100";
    button.innerText = "Check";
    buttonTd.appendChild(button);
    tr.appendChild(buttonTd);

    return tr;
};

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
    
    // Get ungraded submissions
    const ungradedChallenges = await getSubmissions(false, true);
    // const ungraded: Submission[] = [
    //     {
    //         problem_id: 107,
    //         team_name: "driemteam",
    //         score: 0,
    //         used_hint: false,
    //         submission: "dasadsfasf",
    //         grading_description: "Description",
    //     }
    // ];

    const ungradedChallengesTable = document.getElementById("c_ungraded") as HTMLTableElement;
    const ungradedChallengesTableBody = ungradedChallengesTable.getElementsByTagName("tbody")[0];
    console.log("test 1");
    
    ungradedChallengesTableBody.innerHTML = "";
    let i = 1;
    for (const submission of ungradedChallenges) {
        console.log("test 2");
        ungradedChallengesTableBody.appendChild(createUngradedRow(submission, i));
        i++;
    }


    // Get graded submissions
    const gradedChallenges = await getSubmissions(true, false);

    const gradedChallengesTable = document.getElementById("c_graded") as HTMLTableElement;
    const gradedChallengesTableBody = gradedChallengesTable.getElementsByTagName("tbody")[0];
    gradedChallengesTableBody.innerHTML = "";
    i = 1;
    for (const submission of gradedChallenges) {
        gradedChallengesTableBody.appendChild(createGradedRow(submission, i));
        i++;
    }

})();