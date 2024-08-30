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
import { isLoggedIn, getSubmissions } from "./api.js";
var createUngradedRow = function (submission, i) {
    console.log(submission);
    var tr = document.createElement("tr");
    var nr = document.createElement("th");
    nr.scope = "row";
    nr.innerText = i.toString();
    tr.appendChild(nr);
    var challenge = document.createElement("td");
    challenge.innerText = submission.problem_id.toString();
    tr.appendChild(challenge);
    var team = document.createElement("td");
    team.innerText = submission.team_name;
    tr.appendChild(team);
    var buttonTd = document.createElement("td");
    var button = document.createElement("button");
    button.addEventListener("click", function () { return document.location.href = "/commitee-pages/submission-challenges.html?team_name=".concat(submission.team_name, "&problem_id=").concat(submission.problem_id); });
    button.className = "btn btn-primary w-100";
    button.innerText = "Grade";
    buttonTd.appendChild(button);
    tr.appendChild(buttonTd);
    return tr;
};
var createGradedRow = function (submission, i) {
    var tr = document.createElement("tr");
    var nr = document.createElement("th");
    nr.scope = "row";
    nr.innerText = i.toString();
    tr.appendChild(nr);
    var challenge = document.createElement("td");
    challenge.innerText = submission.problem_id.toString();
    tr.appendChild(challenge);
    var team = document.createElement("td");
    team.innerText = submission.team_name;
    tr.appendChild(team);
    var grade = document.createElement("td");
    grade.innerText = submission.score.toString();
    tr.appendChild(grade);
    var buttonTd = document.createElement("td");
    var button = document.createElement("button");
    button.addEventListener("click", function () { return document.location.href = "/commitee-pages/submission-challenges.html?team_name=".concat(submission.team_name, "&problem_id=").concat(submission.problem_id); });
    button.className = "btn btn-primary w-100";
    button.innerText = "Check";
    buttonTd.appendChild(button);
    tr.appendChild(buttonTd);
    return tr;
};
(function () { return __awaiter(void 0, void 0, void 0, function () {
    var session, ungradedChallenges, ungradedChallengesTable, ungradedChallengesTableBody, i, _i, ungradedChallenges_1, submission, gradedChallenges, gradedChallengesTable, gradedChallengesTableBody, _a, gradedChallenges_1, submission;
    var _b;
    return __generator(this, function (_c) {
        switch (_c.label) {
            case 0: return [4, isLoggedIn()];
            case 1:
                if (!(_c.sent())) {
                    window.location.href = "/public-pages/login.html";
                    return [2];
                }
                session = JSON.parse(localStorage.getItem("session"));
                if (((_b = session.user) === null || _b === void 0 ? void 0 : _b.admin) === false) {
                    window.location.href = "/user-pages/user-dashboard.html";
                    return [2];
                }
                return [4, getSubmissions(false, true)];
            case 2:
                ungradedChallenges = _c.sent();
                ungradedChallengesTable = document.getElementById("c_ungraded");
                ungradedChallengesTableBody = ungradedChallengesTable.getElementsByTagName("tbody")[0];
                console.log("test 1");
                ungradedChallengesTableBody.innerHTML = "";
                i = 1;
                for (_i = 0, ungradedChallenges_1 = ungradedChallenges; _i < ungradedChallenges_1.length; _i++) {
                    submission = ungradedChallenges_1[_i];
                    console.log("test 2");
                    ungradedChallengesTableBody.appendChild(createUngradedRow(submission, i));
                    i++;
                }
                return [4, getSubmissions(true, false)];
            case 3:
                gradedChallenges = _c.sent();
                gradedChallengesTable = document.getElementById("c_graded");
                gradedChallengesTableBody = gradedChallengesTable.getElementsByTagName("tbody")[0];
                gradedChallengesTableBody.innerHTML = "";
                i = 1;
                for (_a = 0, gradedChallenges_1 = gradedChallenges; _a < gradedChallenges_1.length; _a++) {
                    submission = gradedChallenges_1[_a];
                    gradedChallengesTableBody.appendChild(createGradedRow(submission, i));
                    i++;
                }
                return [2];
        }
    });
}); })();
//# sourceMappingURL=commiteeSubmissions.js.map