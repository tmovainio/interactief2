import { Team, Participant, Session, Person, Submission, Location } from "./types.js";

const getToken = (): string => {
    const session: Session = JSON.parse(localStorage.getItem("session"));
    if (session == null) return null;
    return session.token;
};

const getSession = async (token: string): Promise<Session> => {
    const session: Session = {
        token: token,
        user: null,
        team: null
    };

    // First get the user
    const userResponse: Response = await sendGet('user', { "Authorization": token });
    const response = await userResponse.json();
    let user: Person = response?.person;
    if (user == null) {
        user = response?.participant;
    }
    session.user = user;
    console.log("Got the user: ");
    console.log(session.user);

    // Then get the team if applicable
    if (session.user.admin) {
        // Admins don't have a team
        return session;
    }

    console.log("Getting team");
    // Get the team
    const teamResponse: Response = await sendGet('user/team', { "Authorization": token });
    if (teamResponse.status === 404) {
        console.log("Team is null!");
        // User is not in a team
        session.team = null;
    } else {
        const json = await teamResponse.json();
        const team: Team = json.team;
        session.team = team;
    }

    localStorage.setItem("session", JSON.stringify(session));

    return session;
};


const sendPost = async (url: String, data: any, headers?: any): Promise<Response> => {
    if (headers === undefined) headers = {};
    const res: Response = await fetch('/api/' + url, {
        method: 'POST',
        credentials: 'same-origin',
        headers: { ...{
            'Content-Type': 'application/json'
        }, ...headers },
        body: JSON.stringify(data) 
    });

    if (res.status === 401) {
        // Unauthorized, so we need to log in again
        localStorage.removeItem("session");
        window.location.href = "/public-pages/login.html";
        return;
    }

    return res;
};

const sendPut = async (url: String, data: any, headers?: any): Promise<Response> => {
    if (headers === undefined) headers = {};
    const res: Response = await fetch('/api/' + url, {
        method: 'PUT',
        credentials: 'same-origin',
        headers: {
            ...{
                'Content-Type': 'application/json'
            }, ...headers
        },
        body: JSON.stringify(data)
    });

    if (res.status === 401) {
        // Unauthorized, so we need to log in again
        localStorage.removeItem("session");
        window.location.href = "/public-pages/login.html";
        return;
    }

    return res;
};

const sendDelete = async (url: String, headers?: any): Promise<Response> => {
    if (headers === undefined) headers = {};
    const res: Response = await fetch('/api/' + url, {
        method: 'DELETE',
        credentials: 'same-origin',
        headers: {
            ...{
                'Content-Type': 'application/json'
            }, ...headers
        },
    });

    if (res.status === 401) {
        // Unauthorized, so we need to log in again
        localStorage.removeItem("session");
        window.location.href = "/public-pages/login.html";
        return;
    }

    return res;
};

const sendPatch = async (url: String, data: any, headers?: any): Promise<Response> => {
    console.log("Sending patch request");
    
    if (headers === undefined) headers = {};
    const res: Response = await fetch('/api/' + url, {
        method: 'PATCH',
        credentials: 'same-origin',
        headers: {
            ...{
                'Content-Type': 'application/json'
            }, ...headers
        },
        body: JSON.stringify(data)
    });

    if (res.status === 401) {
        // Unauthorized, so we need to log in again
        localStorage.removeItem("session");
        window.location.href = "/public-pages/login.html";
        return;
    }

    return res;
};


const sendGet = async (url: String, headers?: any) => {
    if (headers === undefined) headers = {};
    const res: Response = await fetch('/api/' + url, {
        method: 'GET',
        credentials: 'same-origin',
        headers: headers,
    });

    if (res.status === 401) {
        // Unauthorized, so we need to log in again
        localStorage.removeItem("session");
        window.location.href = "/public-pages/login.html";
        return;
    }

    return res;
};

const isLoggedIn = async (): Promise<Boolean> => {
    const session: Session = JSON.parse(localStorage.getItem("session"));
    // if (session == null) {
    //     return new Promise((resolve, _) => {
    //         resolve(false);
    //     });
    // }

    const token: string = session?.token;
    if (token == null) {
        // Token isn't saved in the localstorage so we are not logged in anyway
        return new Promise((resolve, _) => {
            resolve(false);
        });
    }

    // Check if the token is valid
    const response: Response = await sendGet('user', { "Authorization": token });
    return new Promise(async (resolve, _) => {
        const res: { person?: Person, participant?: Person } = await response.json();
        let user: Person = res.person;
        // if (user == null) {
        //     user = res.participant;
        // }
        // session.user = user;
        // localStorage.setItem("session", JSON.stringify(session));
        // if (session.team == null && (session.user?.admin == null || session.user?.admin == false)) {
        //     const team: Team = await getMyTeam();
        //     session.team = team;
        // }
        // localStorage.setItem("session", JSON.stringify(session));

        resolve(response.status === 200);
    });
};

const getTeams = async (): Promise<Team[]> => {
    const response: Response = await sendGet('teams', { "Authorization": getToken() });
    const json = await response.json();
    const teams: Team[] = [];
    json.forEach((team: { team: Team }) => teams.push(team.team));
    return teams;
};

const getTeam = async (teamId: string): Promise<Team> => {
    const response: Response = await sendGet(`team/${teamId}`, { "Authorization": getToken() });
    return response.json();
};

const getMyTeam = async (): Promise<Team> => {
    const response: Response = await sendGet('user/team', { "Authorization": getToken() });
    return await response.json();
};

const getSubmissions = async (graded: boolean = false, ungraded: boolean = false, team_name: string = null): Promise<Submission[]> => {
    let url = 'submissions?';
    if (graded) {
        url += 'graded=true&';
    }
    if (ungraded) {
        url += 'ungraded=true&';
    }
    if (team_name != null) {
        url += `team_name=${team_name}&`;
    }
    
    const response: Response = await sendGet(url, { "Authorization": getToken() });
    const json = await response.json();
    const submissions: Submission[] = [];
    json.forEach((submission: { submission: Submission }) => submissions.push(submission.submission));
    return submissions;
};

const getLocations = async (): Promise<Location[]> => {
    const res: Response = await sendGet("locations", { "Authorization": getToken() });
    const json: [
        {
            "location": Location;
        }
    ] = await res.json();
    const locations: Location[] = json.map((obj) => obj.location);
    return locations;
};

const createFadeoutNotification = (message: string, type: string): HTMLDivElement => {
    const notification = document.createElement("div");
    notification.classList.add("alert", `alert-${type}`, "fadeout");
    notification.setAttribute("role", "alert");
    const text = document.createElement("span");
    text.innerHTML = message;
    notification.appendChild(text);


    setTimeout(() => {
        // Does this look kind of sketchy? yes. Does it work? To be honest, I just hope it does but I'm not sure.
        // notification.getRootNode().removeChild(notification);
        notification.remove();
    }, 4000);
    return notification;
};

export { createFadeoutNotification, sendPost, sendPut, sendPatch, sendGet, sendDelete, isLoggedIn, getTeams, getTeam, getMyTeam, getToken, getSession, getSubmissions, getLocations };