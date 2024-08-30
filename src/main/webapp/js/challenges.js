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
import { isLoggedIn, sendGet, getToken } from "./api.js";
var renderLocations = function () { return __awaiter(void 0, void 0, void 0, function () {
    var accordions, response, json, challenges, unlockedLocations;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                accordions = document.getElementById("accordions");
                return [4, sendGet("challenges", { "Authorization": getToken() })];
            case 1:
                response = _a.sent();
                return [4, response.json()];
            case 2:
                json = _a.sent();
                challenges = json.map(function (obj) { return obj.challenge; });
                unlockedLocations = challenges.map(function (challenge) { return challenge.location; });
                unlockedLocations.forEach(function (location) {
                    var accordion = document.createElement("div");
                    accordion.classList.add("accordion-item");
                    var accordionHeader = document.createElement("h2");
                    accordionHeader.classList.add("accordion-header");
                    accordionHeader.id = "location".concat(location.location_id, "header");
                    var accordionButton = document.createElement("button");
                    accordionButton.classList.add("accordion-button", "collapsed");
                    accordionButton.type = "button";
                    accordionButton.setAttribute("data-bs-toggle", "collapse");
                    accordionButton.setAttribute("data-bs-target", "#location".concat(location.location_id));
                    accordionButton.setAttribute("aria-expanded", "false");
                    accordionButton.setAttribute("aria-controls", "location".concat(location.location_id));
                    var buttonTitle = document.createElement("div");
                    buttonTitle.classList.add("h2", "fw-bold");
                    buttonTitle.innerText = location.location_name;
                    accordionButton.appendChild(buttonTitle);
                    accordionHeader.appendChild(accordionButton);
                    accordion.appendChild(accordionHeader);
                    var locationChallenges = challenges.filter(function (challenge) { return challenge.location.location_id === location.location_id; });
                    var accordionBody = document.createElement("div");
                    accordionBody.classList.add("accordion-collapse", "collapse");
                    accordionBody.id = "location".concat(location.location_id);
                    accordionBody.setAttribute("aria-labelledby", "location".concat(location.location_id, "header"));
                    accordionBody.setAttribute("data-bs-parent", "#accordions");
                    var accordionBodyShadow = document.createElement("div");
                    accordionBodyShadow.classList.add("accordion-body", "shadow");
                    var cards = document.createElement("div");
                    cards.classList.add("row", "row-cols-1", "row-cols-md-2", "g-3", "text-center");
                    locationChallenges.forEach(function (challenge) {
                        var card = document.createElement("div");
                        card.classList.add("col");
                        var cardBody = document.createElement("div");
                        cardBody.classList.add("card", "h-100");
                        var cardBodyShadow = document.createElement("div");
                        cardBodyShadow.classList.add("card-body", "d-flex", "flex-column");
                        var cardTitle = document.createElement("h4");
                        cardTitle.classList.add("card-title", "fw-bold");
                        cardTitle.innerText = challenge.problem_name;
                        var cardText = document.createElement("p");
                        cardText.classList.add("card-text");
                        cardText.innerText = challenge.description;
                        var a = document.createElement("a");
                        a.classList.add("btn", "btn-dark", "mt-auto");
                        a.href = "specific_challenges.html?problem_id=".concat(challenge.problem_id);
                        a.innerText = "Open";
                        cardBodyShadow.appendChild(cardTitle);
                        cardBodyShadow.appendChild(cardText);
                        cardBodyShadow.appendChild(a);
                        cardBody.appendChild(cardBodyShadow);
                        card.appendChild(cardBody);
                        cards.appendChild(card);
                    });
                    accordionBodyShadow.appendChild(cards);
                    accordionBody.appendChild(accordionBodyShadow);
                    accordion.appendChild(accordionBody);
                    accordions.appendChild(accordion);
                });
                return [2];
        }
    });
}); };
var renderPuzzles = function () { return __awaiter(void 0, void 0, void 0, function () {
    var res, json, puzzles, puzzlesElement;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4, sendGet("puzzles", {
                    "Authorization": getToken()
                })];
            case 1:
                res = _a.sent();
                return [4, res.json()];
            case 2:
                json = _a.sent();
                puzzles = json.map(function (obj) { return obj.puzzle; });
                puzzlesElement = document.getElementById("puzzlesContent");
                puzzlesElement.innerHTML = "";
                puzzles.forEach(function (puzzle) {
                    var col = document.createElement("div");
                    col.classList.add("col");
                    var card = document.createElement("div");
                    card.classList.add("card", "mb-3", "shadow", "card-submitted", "h-100");
                    var card2 = document.createElement("div");
                    card2.classList.add("row", "g-0", "h-100");
                    var cardImage = document.createElement("div");
                    cardImage.classList.add("col-md-4", "row", "g-0", "h-100");
                    var image = document.createElement("img");
                    image.classList.add("img-fluid", "rounded-start", "h-100");
                    image.style.objectFit = "cover";
                    image.style.objectPosition = "center";
                    image.src = "../images/Background Cookies.jpg";
                    cardImage.appendChild(image);
                    card2.appendChild(cardImage);
                    var cardBody = document.createElement("div");
                    cardBody.classList.add("card-body", "col-md-8");
                    var cardTitle = document.createElement("h2");
                    cardTitle.classList.add("card-title", "fw-bold", "display-5");
                    cardTitle.innerText = puzzle.problem_name;
                    var cardText = document.createElement("p");
                    cardText.classList.add("card-text");
                    cardText.innerText = "Unlocks location: ".concat(puzzle.location_id);
                    cardBody.appendChild(cardTitle);
                    cardBody.appendChild(cardText);
                    var a = document.createElement("a");
                    a.classList.add("btn", "btn-dark", "w-100");
                    a.href = "puzzle.html?problem_id=".concat(puzzle.problem_id);
                    a.innerText = "Open";
                    cardBody.appendChild(a);
                    card2.appendChild(cardBody);
                    card.appendChild(card2);
                    col.appendChild(card);
                    puzzlesElement.appendChild(col);
                });
                return [2];
        }
    });
}); };
(function () { return __awaiter(void 0, void 0, void 0, function () {
    var session;
    var _a;
    return __generator(this, function (_b) {
        switch (_b.label) {
            case 0: return [4, isLoggedIn()];
            case 1:
                if (!(_b.sent())) {
                    window.location.href = "/public-pages/login.html";
                    return [2];
                }
                session = JSON.parse(localStorage.getItem("session"));
                if ((_a = session.user) === null || _a === void 0 ? void 0 : _a.admin) {
                    window.location.href = "/commitee-pages/commitee-dash.html";
                }
                return [4, renderPuzzles()];
            case 2:
                _b.sent();
                return [4, renderLocations()];
            case 3:
                _b.sent();
                return [2];
        }
    });
}); })();
//# sourceMappingURL=challenges.js.map