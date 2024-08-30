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
import { getToken, isLoggedIn, sendGet, sendPost, getLocations, createFadeoutNotification } from "./api.js";
var ALLOWED_TYPES = ["image/jpeg", "image/png", "image/jpg"];
var locations = [];
var renderLocations = function () { return __awaiter(void 0, void 0, void 0, function () {
    var challengeLocationSelect, puzzleLocationSelect;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4, getLocations()];
            case 1:
                locations = _a.sent();
                challengeLocationSelect = document.getElementById("c_location");
                puzzleLocationSelect = document.getElementById("p_location");
                locations.forEach(function (location) {
                    var option = document.createElement("option");
                    option.value = location.location_id.toString();
                    option.innerText = location.location_name;
                    challengeLocationSelect.appendChild(option);
                    puzzleLocationSelect.appendChild(option.cloneNode(true));
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
                    cardText.innerText = "Unlocks location: ".concat(locations.filter(function (location) { return location.location_id === puzzle.location_id; })[0].location_name);
                    cardBody.appendChild(cardTitle);
                    cardBody.appendChild(cardText);
                    var a = document.createElement("a");
                    a.classList.add("btn", "btn-dark", "w-100");
                    a.href = "commitee-puzzle.html?problem_id=".concat(puzzle.problem_id);
                    a.innerText = "Edit";
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
    var createLocationButton, nameElement, locationElement, scoreElement, descriptionElement, description;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4, isLoggedIn()];
            case 1:
                if (!(_a.sent())) {
                    window.location.href = "/public-pages/login.html";
                    return [2];
                }
                return [4, renderLocations()];
            case 2:
                _a.sent();
                createLocationButton = document.getElementById("l_createButton");
                createLocationButton.addEventListener("click", function (e) { return __awaiter(void 0, void 0, void 0, function () {
                    var name, res, main, main;
                    return __generator(this, function (_a) {
                        switch (_a.label) {
                            case 0:
                                e.preventDefault();
                                name = document.getElementById("l_location").value;
                                return [4, sendPost("locations", { location_name: name }, {
                                        "Authorization": getToken()
                                    })];
                            case 1:
                                res = _a.sent();
                                if (!(res.status === 200)) return [3, 3];
                                document.getElementById("l_location").value = "";
                                main = document.getElementsByTagName("main")[0];
                                main.prepend(createFadeoutNotification("Location created successfully!", "success"));
                                return [4, renderLocations()];
                            case 2:
                                _a.sent();
                                return [3, 4];
                            case 3:
                                main = document.getElementsByTagName("main")[0];
                                main.prepend(createFadeoutNotification("Error creating location, try again!", "danger"));
                                _a.label = 4;
                            case 4: return [2];
                        }
                    });
                }); });
                document.getElementById("c_createButton").addEventListener("click", function (e) { return __awaiter(void 0, void 0, void 0, function () {
                    var nameElement, locationElement, scoreElement, descriptionElement, name, location, score, description, res, main, main;
                    return __generator(this, function (_a) {
                        switch (_a.label) {
                            case 0:
                                e.preventDefault();
                                nameElement = document.getElementById("c_name");
                                locationElement = document.getElementById("c_location");
                                scoreElement = document.getElementById("c_score");
                                descriptionElement = document.getElementById("c_description");
                                name = nameElement.value;
                                location = Number(locationElement.value);
                                score = Number(scoreElement.value);
                                description = descriptionElement.value;
                                return [4, sendPost("challenges", {
                                        problem_name: name,
                                        location_id: location,
                                        score: score,
                                        description: description
                                    }, {
                                        "Authorization": getToken()
                                    })];
                            case 1:
                                res = _a.sent();
                                if (res.status === 200) {
                                    nameElement.value = "";
                                    locationElement.value = "";
                                    scoreElement.value = "";
                                    descriptionElement.value = "";
                                    main = document.getElementsByTagName("main")[0];
                                    main.prepend(createFadeoutNotification("Challenge created successfully!", "success"));
                                }
                                else {
                                    main = document.getElementsByTagName("main")[0];
                                    main.prepend(createFadeoutNotification("Error creating challenge, try again!", "danger"));
                                }
                                return [2];
                        }
                    });
                }); });
                nameElement = document.getElementById("p_name");
                locationElement = document.getElementById("p_location");
                scoreElement = document.getElementById("p_score");
                descriptionElement = document.getElementById("p_description");
                descriptionElement.accept = ALLOWED_TYPES.join(",");
                description = null;
                document.getElementById("p_create").addEventListener("click", function (e) { return __awaiter(void 0, void 0, void 0, function () {
                    var name, location, main, formData, body, res, main, main;
                    return __generator(this, function (_a) {
                        switch (_a.label) {
                            case 0:
                                e.preventDefault();
                                name = nameElement.value;
                                location = Number(locationElement.value);
                                if (description == null) {
                                    main = document.getElementsByTagName("main")[0];
                                    main.prepend(createFadeoutNotification("Please select a file!", "danger"));
                                    descriptionElement.classList.add("is-invalid");
                                    return [2];
                                }
                                formData = new FormData();
                                body = {
                                    problem_name: name,
                                    location_id: location,
                                };
                                formData.append("body", JSON.stringify(body));
                                formData.append("file", description);
                                console.log(formData.get("body"));
                                console.log(formData.get("file"));
                                return [4, fetch("/api/puzzles", {
                                        method: "POST",
                                        headers: {
                                            "Authorization": getToken(),
                                        },
                                        body: formData
                                    })];
                            case 1:
                                res = _a.sent();
                                if (!(res.status === 200)) return [3, 3];
                                nameElement.value = "";
                                locationElement.value = "";
                                scoreElement.value = "";
                                descriptionElement.value = null;
                                main = document.getElementsByTagName("main")[0];
                                main.prepend(createFadeoutNotification("Puzzle created successfully!", "success"));
                                return [4, renderPuzzles()];
                            case 2:
                                _a.sent();
                                return [3, 4];
                            case 3:
                                main = document.getElementsByTagName("main")[0];
                                main.prepend(createFadeoutNotification("Error creating puzzle, try again!", "danger"));
                                _a.label = 4;
                            case 4: return [2];
                        }
                    });
                }); });
                descriptionElement.addEventListener("change", function (e) {
                    console.log("file changed!");
                    description = e.target.files[0];
                    if (description == null) {
                        document.getElementById("p_preview").hidden = true;
                        return;
                    }
                    ;
                    if (ALLOWED_TYPES.indexOf(description.type) === -1) {
                        document.getElementById("p_preview").hidden = true;
                        descriptionElement.classList.add("is-invalid");
                        var main = document.getElementsByTagName("main")[0];
                        main.prepend(createFadeoutNotification("This file type is not supported! Please try again.", "danger"));
                    }
                    else {
                        descriptionElement.classList.remove("is-invalid");
                        var reader = new FileReader();
                        reader.onload = function (e) {
                            document.getElementById("p_preview").setAttribute("src", e.target.result.toString());
                            document.getElementById("p_preview").hidden = false;
                        };
                        reader.readAsDataURL(description);
                    }
                });
                return [4, renderPuzzles()];
            case 3:
                _a.sent();
                return [2];
        }
    });
}); })();
//# sourceMappingURL=createItems.js.map