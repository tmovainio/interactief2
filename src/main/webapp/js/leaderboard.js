var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    function adopt(value) { return value instanceof P ? value : new P(function (resolve) { resolve(value); }); }
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (_) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
import { isLoggedIn, sendGet, getToken, getTeams } from "./api.js";
var createRow = function (stats, myTeam) {
    if (myTeam === void 0) { myTeam = false; }
    var row = document.createElement("tr");
    var rank = document.createElement("td");
    var teamName = document.createElement("td");
    var score = document.createElement("td");
    if (stats.rank === null) {
        rank.innerText = "--";
    }
    else {
        rank.innerText = stats.rank.toString();
    }
    if (myTeam)
        row.classList.add("this-team");
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
    }
    else {
        score.innerText = stats.total_score.toString();
    }
    row.appendChild(rank);
    row.appendChild(teamName);
    row.appendChild(score);
    return row;
};
(function () { return __awaiter(void 0, void 0, void 0, function () {
    var session, leaderboardResponse, response, leaderboard, teamsAdded, teams;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4, isLoggedIn()];
            case 1:
                if (!(_a.sent())) {
                    alert("Not logged in!");
                    window.location.href = "/public-pages/login.html";
                    return [2];
                }
                session = JSON.parse(localStorage.getItem("session"));
                return [4, sendGet('leaderboard', { "Authorization": getToken() })];
            case 2:
                leaderboardResponse = _a.sent();
                if (document.getElementById("hiddenText") !== null) {
                    if (leaderboardResponse.status === 200) {
                        document.getElementById("hiddenText").hidden = true;
                    }
                    else {
                        document.getElementById("hiddenText").hidden = false;
                        return [2];
                    }
                }
                return [4, leaderboardResponse.json()];
            case 3:
                response = _a.sent();
                leaderboard = [];
                response.forEach(function (stats) {
                    leaderboard.push(stats.score);
                });
                teamsAdded = [];
                document.getElementById("leaderboard").innerHTML = "";
                leaderboard.forEach(function (stats) {
                    teamsAdded.push(stats.team_name);
                    if (session.team !== null) {
                        if (stats.team_name === session.team.team_name) {
                            document.getElementById("myRank").innerText = stats.rank.toString();
                        }
                    }
                    var row = session.team != null ? createRow(stats, stats.team_name === session.team.team_name) : createRow(stats, false);
                    document.getElementById("leaderboard").appendChild(row);
                });
                return [4, getTeams()];
            case 4:
                teams = _a.sent();
                teams.forEach(function (team) {
                    console.log(team);
                    if (teamsAdded.indexOf(team.team_name) !== -1)
                        return;
                    if (team.team_name === undefined)
                        return;
                    var stats = {
                        team_name: team.team_name,
                        total_score: null,
                        rank: null
                    };
                    var row = createRow(stats);
                    document.getElementById("leaderboard").appendChild(row);
                });
                return [2];
        }
    });
}); })();
//# sourceMappingURL=leaderboard.js.map