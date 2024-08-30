import { isLoggedIn, sendGet, getToken, getTeams } from "./api.js";
import { Team, TeamStats, Session } from "./types.js";

const createRow = (stats: TeamStats, myTeam: boolean = false): HTMLTableRowElement => {
    const row: HTMLTableRowElement = document.createElement("tr");
    const rank: HTMLTableCellElement = document.createElement("td");
    const teamName: HTMLTableCellElement = document.createElement("td");
    const score: HTMLTableCellElement = document.createElement("td");

    if (stats.rank === null) {
        rank.innerText = "--";
    } else {
        rank.innerText = stats.rank.toString();
    }

    if (myTeam) row.classList.add("this-team");


    rank.classList.add("rank");
    score.classList.add("score");

    switch (stats.rank) {
        case 1:
            row.classList.add("first");
            row.classList.add("shadow");
            row.classList.add("h2");
            break;
        case 2:
            row.classList.add("second");
            row.classList.add("shadow");
            row.classList.add("h3");
            break;
        case 3:
            row.classList.add("third");
            row.classList.add("shadow");
            row.classList.add("h4");
            break;
        default:
            break;
    }

    console.log(stats);
    teamName.innerText = stats.team_name;
    if (stats.total_score === null) {
        score.innerText = "--";
    } else {
        score.innerText = stats.total_score.toString();
    }
    
    row.appendChild(rank);
    row.appendChild(teamName);
    row.appendChild(score);
    
    return row;
};

(async () => {
    // Check if we are logged in
    // If we are not, send to the login page
    if (!await isLoggedIn()) {
        alert("Not logged in!");
        window.location.href = "/public-pages/login.html";
        return;
    }

    const session: Session = JSON.parse(localStorage.getItem("session"));


    // Get the leaderboard
    const leaderboardResponse: Response = await sendGet('leaderboard', { "Authorization": getToken() });
    if (document.getElementById("hiddenText") !== null) {
        if (leaderboardResponse.status === 200) {
            document.getElementById("hiddenText").hidden = true;
        } else {
            document.getElementById("hiddenText").hidden = false;
            return;
        }
    }
    
    const response = await leaderboardResponse.json();
    const leaderboard: TeamStats[] = [];
    response.forEach((stats: { score: TeamStats }) => {
        leaderboard.push(stats.score);
    });

    const teamsAdded: string[] = [];

    document.getElementById("leaderboard").innerHTML = "";

    leaderboard.forEach((stats: TeamStats) => {
        teamsAdded.push(stats.team_name);
        if (session.team !== null) {
            if (stats.team_name === session.team.team_name) {
                        document.getElementById("myRank").innerText = stats.rank.toString();
            }
        }
        
        const row: HTMLTableRowElement = session.team != null ? createRow(stats, stats.team_name === session.team.team_name) : createRow(stats, false);
        

        document.getElementById("leaderboard").appendChild(row);
    });

    // Fill in the rest of the leaderboard
    // with teams that did not complete any puzzles yet
    const teams: Team[] = await getTeams();
    teams.forEach((team: Team) => {
        console.log(team);
        if (teamsAdded.indexOf(team.team_name) !== -1) return;
        if (team.team_name === undefined) return;
        const stats: TeamStats = {
            team_name: team.team_name,
            total_score: null,
            rank: null
        };
        const row: HTMLTableRowElement = createRow(stats);
        document.getElementById("leaderboard").appendChild(row);

    });

})();