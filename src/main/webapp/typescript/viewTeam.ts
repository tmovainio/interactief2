import { getSession, getToken, isLoggedIn } from "./api.js";
import { Person, Session, Team } from "./types.js";

const createRow = (member: Person, team: Team, captain: boolean) => {
    const row = document.createElement("tr");
    const name = document.createElement("td");
    name.innerText = member.name;
    const phone = document.createElement("td");
    phone.innerText = member.phone_numb;
    const role = document.createElement("td");
    role.innerText = "Member";
    if (member.s_numb == team.captain.participant.s_numb) {
        role.innerText = "Captain";
    }
    row.appendChild(name);
    row.appendChild(phone);
    row.appendChild(role);
    if (captain) {
        const kick = document.createElement("td");
        if (member.s_numb !== team.captain.participant.s_numb) {
            const kickButton = document.createElement("button");
            kickButton.innerText = "Kick";
            kickButton.classList.add("btn");
            kickButton.classList.add("btn-danger");
            kickButton.onclick = async () => {

            };

            kick.appendChild(kickButton);
        }
        
        row.appendChild(kick);
    }
    return row;
}

(async () => {
    // Get and update the session, if we are not logged in this will send us to the login page as well
    const session: Session = await getSession(getToken());
    const team: Team = session.team;

    const captain = team.captain.participant.s_numb === session.user.s_numb;

    if (team.content == undefined) {
        document.getElementById("joinCodeDiv").style.display = "none";
    } else if (captain) {
        document.getElementById("joinCode").innerText = team.content
    }

    if (captain) {
        const kick = document.createElement("th");
        kick.scope = "col";
        kick.innerText = "Kick";
        document.getElementById("membersHead").appendChild(kick);
    }


    const table = document.getElementById("members") as HTMLTableElement;
    table.innerHTML = "";

    team.team_members.forEach(member => {
        table.appendChild(createRow(member.participant, team, captain));
    });

    document.getElementById("copyInviteLink").addEventListener("click", async (e) => {
        e.preventDefault();

        await navigator.clipboard.writeText(team.content);
    });





})();