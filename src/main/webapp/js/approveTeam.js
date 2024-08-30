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
import { getToken, sendGet, sendPatch } from "./api.js";
var teams = [];
var approveTeam = function (team, approved) { return __awaiter(void 0, void 0, void 0, function () {
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4, sendPatch("teams/".concat(team.team_name), { approve: approved }, { "Authorization": getToken() })];
            case 1: return [2, _a.sent()];
        }
    });
}); };
var createRow = function (team) {
    var tr = document.createElement("tr");
    var captain = document.createElement("td");
    captain.scope = "row";
    captain.innerText = team.captain.participant.name;
    tr.appendChild(captain);
    var name = document.createElement("td");
    name.innerText = team.team_name;
    tr.appendChild(name);
    var buttonsTd = document.createElement("td");
    var buttons = document.createElement("div");
    buttons.className = "d-none d-md-flex gap-2";
    var approve = document.createElement("button");
    approve.className = "btn btn-primary flex-grow-1 w-50";
    approve.innerText = "Approve";
    approve.type = "button";
    approve.onclick = function () { return __awaiter(void 0, void 0, void 0, function () {
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    console.log("Approving team!");
                    return [4, approveTeam(team, true)];
                case 1:
                    _a.sent();
                    tr.style.transition = "opacity 0.5s";
                    tr.style.opacity = "0";
                    setTimeout(function () {
                        tr.remove();
                    }, 500);
                    return [2];
            }
        });
    }); };
    buttons.appendChild(approve);
    var deny = document.createElement("button");
    deny.className = "btn btn-danger flex-grow-1 w-50";
    deny.innerText = "Deny";
    deny.type = "button";
    deny.onclick = function () { return __awaiter(void 0, void 0, void 0, function () {
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0: return [4, approveTeam(team, false)];
                case 1:
                    _a.sent();
                    tr.style.transition = "opacity 0.5s";
                    tr.style.opacity = "0";
                    setTimeout(function () {
                        tr.remove();
                    }, 500);
                    return [2];
            }
        });
    }); };
    buttons.appendChild(deny);
    buttonsTd.appendChild(buttons);
    tr.appendChild(buttonsTd);
    return tr;
};
var update = function () { return __awaiter(void 0, void 0, void 0, function () {
    var response, teamResponse, table, thead, tr, captain, teamName, actions;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4, sendGet("teams?status=AWAITING_APPROVAL", {
                    "Authorization": getToken()
                })];
            case 1:
                response = _a.sent();
                return [4, response.json()];
            case 2:
                teamResponse = _a.sent();
                teams = teamResponse.map(function (team) { return team.team; });
                table = document.getElementsByTagName("table")[0];
                table.innerHTML = "";
                thead = document.createElement("thead");
                tr = document.createElement("tr");
                tr.className = "bg-primary text-white";
                captain = document.createElement("th");
                captain.scope = "col";
                captain.innerText = "Captain";
                tr.appendChild(captain);
                teamName = document.createElement("th");
                teamName.scope = "col";
                teamName.innerText = "Team Name";
                tr.appendChild(teamName);
                actions = document.createElement("th");
                actions.scope = "col";
                actions.innerText = "Actions";
                tr.appendChild(actions);
                thead.appendChild(tr);
                table.appendChild(thead);
                teams.forEach(function (team) {
                    table.appendChild(createRow(team));
                });
                return [2];
        }
    });
}); };
var setup = function () { return __awaiter(void 0, void 0, void 0, function () {
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4, update()];
            case 1:
                _a.sent();
                return [2];
        }
    });
}); };
(function () { return __awaiter(void 0, void 0, void 0, function () {
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4, setup()];
            case 1:
                _a.sent();
                return [2];
        }
    });
}); })();
//# sourceMappingURL=approveTeam.js.map