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
import { getSession, getToken } from "./api.js";
var createRow = function (member, team, captain) {
    var row = document.createElement("tr");
    var name = document.createElement("td");
    name.innerText = member.name;
    var phone = document.createElement("td");
    phone.innerText = member.phone_numb;
    var role = document.createElement("td");
    role.innerText = "Member";
    if (member.s_numb == team.captain.participant.s_numb) {
        role.innerText = "Captain";
    }
    row.appendChild(name);
    row.appendChild(phone);
    row.appendChild(role);
    if (captain) {
        var kick = document.createElement("td");
        if (member.s_numb !== team.captain.participant.s_numb) {
            var kickButton = document.createElement("button");
            kickButton.innerText = "Kick";
            kickButton.classList.add("btn");
            kickButton.classList.add("btn-danger");
            kickButton.onclick = function () { return __awaiter(void 0, void 0, void 0, function () {
                return __generator(this, function (_a) {
                    return [2];
                });
            }); };
            kick.appendChild(kickButton);
        }
        row.appendChild(kick);
    }
    return row;
};
(function () { return __awaiter(void 0, void 0, void 0, function () {
    var session, team, captain, kick, table;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4, getSession(getToken())];
            case 1:
                session = _a.sent();
                team = session.team;
                captain = team.captain.participant.s_numb === session.user.s_numb;
                if (team.content == undefined) {
                    document.getElementById("joinCodeDiv").style.display = "none";
                }
                else if (captain) {
                    document.getElementById("joinCode").innerText = team.content;
                }
                if (captain) {
                    kick = document.createElement("th");
                    kick.scope = "col";
                    kick.innerText = "Kick";
                    document.getElementById("membersHead").appendChild(kick);
                }
                table = document.getElementById("members");
                table.innerHTML = "";
                team.team_members.forEach(function (member) {
                    table.appendChild(createRow(member.participant, team, captain));
                });
                document.getElementById("copyInviteLink").addEventListener("click", function (e) { return __awaiter(void 0, void 0, void 0, function () {
                    return __generator(this, function (_a) {
                        switch (_a.label) {
                            case 0:
                                e.preventDefault();
                                return [4, navigator.clipboard.writeText(team.content)];
                            case 1:
                                _a.sent();
                                return [2];
                        }
                    });
                }); });
                return [2];
        }
    });
}); })();
//# sourceMappingURL=viewTeam.js.map