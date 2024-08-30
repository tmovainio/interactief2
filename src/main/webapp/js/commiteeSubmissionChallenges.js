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
import { createFadeoutNotification, getToken, isLoggedIn, sendGet, sendPatch } from "./api.js";
(function () { return __awaiter(void 0, void 0, void 0, function () {
    var session, urlParams, teamName, problemId, res, submission, challengeRes, challenge, fileRes, reader, _a, _b;
    var _c;
    return __generator(this, function (_d) {
        switch (_d.label) {
            case 0: return [4, isLoggedIn()];
            case 1:
                if (!(_d.sent())) {
                    document.location.href = "/public-pages/login.html";
                    return [2];
                }
                session = JSON.parse(localStorage.getItem("session"));
                if (((_c = session.user) === null || _c === void 0 ? void 0 : _c.admin) === false) {
                    document.location.href = "/user-pages/user-dashboard.html";
                    return [2];
                }
                urlParams = new URLSearchParams(window.location.search);
                teamName = urlParams.get("team_name");
                problemId = urlParams.get("problem_id");
                return [4, sendGet("submissions/admin/".concat(teamName, "/").concat(problemId), { "Authorization": "".concat(getToken()) })];
            case 2:
                res = _d.sent();
                return [4, res.json()];
            case 3:
                submission = (_d.sent()).submission;
                return [4, sendGet("challenges/".concat(submission.problem_id), { "Authorization": "".concat(getToken()) })];
            case 4:
                challengeRes = _d.sent();
                return [4, challengeRes.json()];
            case 5:
                challenge = (_d.sent()).challenge;
                document.getElementById("sub_team").innerText = submission.team_name;
                document.getElementById("sub_challenge").innerText = submission.problem_id.toString();
                document.getElementById("sub_location").innerText = challenge.location.location_name;
                document.getElementById("grade_ph").innerText = "/ ".concat(challenge.score);
                return [4, sendGet("submissions/admin/".concat(teamName, "/").concat(problemId, "/file"), { "Authorization": "".concat(getToken()) })];
            case 6:
                fileRes = _d.sent();
                if (!(fileRes.status !== 404)) return [3, 8];
                reader = new FileReader();
                reader.onload = function (e) {
                    document.getElementById("preview").src = e.target.result.toString();
                    document.getElementById("preview").hidden = false;
                };
                _b = (_a = reader).readAsDataURL;
                return [4, fileRes.blob()];
            case 7:
                _b.apply(_a, [_d.sent()]);
                return [3, 9];
            case 8:
                document.getElementById("preview").src = "/images/University_of_Twente_panoramic.jpg";
                document.getElementById("preview").hidden = false;
                _d.label = 9;
            case 9:
                document.getElementById("submit").addEventListener("click", function (e) { return __awaiter(void 0, void 0, void 0, function () {
                    var grade, description, res, main;
                    return __generator(this, function (_a) {
                        switch (_a.label) {
                            case 0:
                                e.preventDefault();
                                grade = parseInt(document.getElementById("grade").value);
                                description = document.getElementById("notes").value;
                                return [4, sendPatch("submissions/admin/".concat(teamName, "/").concat(problemId), { "score": grade, "grading_description": description }, { "Authorization": "".concat(getToken()) })];
                            case 1:
                                res = _a.sent();
                                if (res.status === 200) {
                                    document.location.href = "/commitee-pages/submissions.html";
                                    return [2];
                                }
                                else {
                                    main = document.getElementsByTagName("main")[0];
                                    main.prepend(createFadeoutNotification("Something went wrong grading this submission!", "error"));
                                }
                                return [2];
                        }
                    });
                }); });
                return [2];
        }
    });
}); })();
//# sourceMappingURL=commiteeSubmissionChallenges.js.map