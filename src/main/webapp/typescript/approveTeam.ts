import { getToken, sendGet, sendPut, sendPatch } from "./api.js";
import { Team } from "./types.js";
// import { Store, createStore } from "./statemanager/store.js";

let teams: Team[] = [];

const approveTeam = async (team: Team, approved: boolean): Promise<Response> => {
    return await sendPatch(`teams/${team.team_name}`, { approve: approved }, { "Authorization": getToken() });
};

/**
 * Create a row element for a team and add all elements to it
 * @param team the team to create a row for
 * @returns HTMLTableRowElement the row that was created
 */
const createRow = (team: Team): HTMLTableRowElement => {
    const tr = document.createElement("tr");

    const captain = document.createElement("td");
    captain.scope = "row";
    captain.innerText = team.captain.participant.name;
    tr.appendChild(captain);

    const name = document.createElement("td");
    name.innerText = team.team_name;
    tr.appendChild(name);

    const buttonsTd = document.createElement("td");
    const buttons = document.createElement("div");
    buttons.className = "d-none d-md-flex gap-2";
    const approve = document.createElement("button");
    approve.className = "btn btn-primary flex-grow-1 w-50";
    approve.innerText = "Approve";
    approve.type = "button";
    approve.onclick = async () => {
        console.log("Approving team!");
        
        await approveTeam(team, true);
        tr.style.transition = "opacity 0.5s";
        tr.style.opacity = "0";
        setTimeout(() => {
            tr.remove();
        }, 500);
    };
    buttons.appendChild(approve);

    const deny = document.createElement("button");
    deny.className = "btn btn-danger flex-grow-1 w-50";
    deny.innerText = "Deny";
    deny.type = "button";
    deny.onclick = async () => {
        await approveTeam(team, false);
        tr.style.transition = "opacity 0.5s";
        tr.style.opacity = "0";
        setTimeout(() => {
            tr.remove();
        }, 500);
    };
    buttons.appendChild(deny);

    buttonsTd.appendChild(buttons);

    tr.appendChild(buttonsTd);

    return tr;
};

const update = async () => {
    const response: Response = await sendGet("teams?status=AWAITING_APPROVAL", {
        "Authorization": getToken()
    });

    type TeamResponse = [
        {
            "team": Team
        }
    ];
    const teamResponse: TeamResponse = await response.json();
    teams = teamResponse.map((team) => team.team);

    const table = document.getElementsByTagName("table")[0] as HTMLTableElement;
    table.innerHTML = "";

    // Add the headers
    const thead = document.createElement("thead");
    const tr = document.createElement("tr");
    tr.className = "bg-primary text-white";
    const captain = document.createElement("th");
    captain.scope = "col";
    captain.innerText = "Captain";
    tr.appendChild(captain);

    const teamName = document.createElement("th");
    teamName.scope = "col";
    teamName.innerText = "Team Name";
    tr.appendChild(teamName);

    const actions = document.createElement("th");
    actions.scope = "col";
    actions.innerText = "Actions";
    tr.appendChild(actions);
    thead.appendChild(tr);
    table.appendChild(thead);

    teams.forEach((team: Team) => {
        table.appendChild(createRow(team));
    });
}

const setup = async () => {
    await update();
};

// const loop = async () => {
//     await update();
// };

(async () => {
    await setup();
    // setInterval(loop, 5000); // Update the page every 5 seconds
})();