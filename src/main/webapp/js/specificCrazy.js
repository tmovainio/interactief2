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
import { createFadeoutNotification, getToken, isLoggedIn, sendDelete, sendGet, sendPatch } from "./api.js";
(function () { return __awaiter(void 0, void 0, void 0, function () {
    var session, urlParams, problemId, res, json, crazy88, currentChallenge, currentPoints, description, originalContent;
    var _a;
    return __generator(this, function (_b) {
        switch (_b.label) {
            case 0:
                if (!isLoggedIn()) {
                    window.location.href = "/public-pages/login.html";
                    return [2];
                }
                session = JSON.parse(localStorage.getItem("session"));
                if (!((_a = session.user) === null || _a === void 0 ? void 0 : _a.admin)) {
                    window.location.href = "/user-pages/jointeam.html";
                    return [2];
                }
                urlParams = new URLSearchParams(window.location.search);
                problemId = urlParams.get("problem_id");
                return [4, sendGet("crazy88/".concat(problemId), { "Authorization": getToken() })];
            case 1:
                res = _b.sent();
                return [4, res.json()];
            case 2:
                json = _b.sent();
                crazy88 = json.crazy88;
                currentChallenge = document.getElementById("current_challenge");
                currentPoints = document.getElementById("current_points");
                description = document.getElementById("descriptionParagraph");
                currentChallenge.innerText = crazy88.problem_name;
                currentPoints.innerText = crazy88.score.toString();
                description.innerText = crazy88.description;
                document.getElementById("deleteBtn").addEventListener("click", function () { return __awaiter(void 0, void 0, void 0, function () {
                    var res;
                    return __generator(this, function (_a) {
                        switch (_a.label) {
                            case 0: return [4, sendDelete("crazy88/".concat(problemId), { "Authorization": getToken() })];
                            case 1:
                                res = _a.sent();
                                if (res.status === 200) {
                                    window.location.href = "/commitee-pages/commitee-crazy.html";
                                    return [2];
                                }
                                return [2];
                        }
                    });
                }); });
                originalContent = null;
                document.getElementById('editBtn').addEventListener('click', function () {
                    var elementsToEdit = ['current_challenge', 'current_points', 'description'];
                    elementsToEdit.forEach(function (elementId) {
                        document.getElementById(elementId).setAttribute("contentEditable", "true");
                        document.getElementById(elementId).classList.add("editable");
                    });
                    document.getElementById('editBtn').style.display = 'none';
                    document.getElementById('saveBtn').style.display = 'inline-block';
                    document.getElementById('discardBtn').style.display = 'inline-block';
                });
                document.getElementById('discardBtn').addEventListener('click', function () {
                    var container = document.querySelector('.container');
                    container.parentNode.replaceChild(originalContent, container);
                    document.getElementById('saveBtn').style.display = 'none';
                    document.getElementById('discardBtn').style.display = 'none';
                    document.getElementById('editBtn').style.display = 'inline-block';
                });
                document.getElementById('saveBtn').addEventListener('click', function () { return __awaiter(void 0, void 0, void 0, function () {
                    var newChallenge, newPoints, newDescription, res, main, editableElements;
                    return __generator(this, function (_a) {
                        switch (_a.label) {
                            case 0:
                                newChallenge = currentChallenge.innerText;
                                newPoints = currentPoints.innerText;
                                newDescription = description.innerText;
                                return [4, sendPatch("crazy88/".concat(problemId), {
                                        problem_name: newChallenge,
                                        score: parseInt(newPoints),
                                        description: newDescription,
                                        problem_id: parseInt(problemId)
                                    }, { "Authorization": getToken() })];
                            case 1:
                                res = _a.sent();
                                if (res.status !== 200) {
                                    main = document.getElementsByTagName("main")[0];
                                    main.prepend(createFadeoutNotification("Something went wrong with updating this crazy88 challenge. Please try again later.", "danger"));
                                    return [2];
                                }
                                editableElements = document.querySelectorAll('.editable');
                                editableElements.forEach(function (element) {
                                    element.setAttribute('contentEditable', "false");
                                    element.classList.remove('editable');
                                });
                                document.getElementById('saveBtn').style.display = 'none';
                                document.getElementById('discardBtn').style.display = 'none';
                                document.getElementById('editBtn').style.display = 'inline-block';
                                return [2];
                        }
                    });
                }); });
                return [2];
        }
    });
}); })();
//# sourceMappingURL=specificCrazy.js.map