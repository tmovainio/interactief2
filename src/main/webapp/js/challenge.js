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
import { getToken, sendGet } from "./api.js";
var file;
var ALLOWED_TYPES = ["image/jpeg", "image/png", "image/jpg"];
(function () { return __awaiter(void 0, void 0, void 0, function () {
    var urlParams, problemId, challengeResponse, challenge, currentChallenge, currentPoints, description, submissionRes, fileRes, reader, _a, _b;
    return __generator(this, function (_c) {
        switch (_c.label) {
            case 0:
                urlParams = new URLSearchParams(window.location.search);
                problemId = urlParams.get("problem_id");
                return [4, sendGet("challenges/".concat(problemId), { "Authorization": getToken() })];
            case 1:
                challengeResponse = _c.sent();
                return [4, challengeResponse.json()];
            case 2:
                challenge = (_c.sent()).challenge;
                currentChallenge = document.getElementById("current_challenge");
                currentChallenge.innerText = challenge.problem_name;
                currentPoints = document.getElementById("current_points");
                currentPoints.innerText = "0 / ".concat(challenge.score);
                description = document.getElementById("descriptionContent");
                description.innerText = challenge.description;
                return [4, sendGet("submissions/".concat(problemId), { "Authorization": getToken() })];
            case 3:
                submissionRes = _c.sent();
                if (!(submissionRes.status === 200)) return [3, 8];
                return [4, sendGet("submissions/".concat(problemId, "/file"), { "Authorization": getToken() })];
            case 4:
                fileRes = _c.sent();
                if (!(fileRes.status === 404)) return [3, 5];
                document.getElementById("preview").src = "/images/University_of_Twente_panoramic.jpg";
                return [3, 7];
            case 5:
                reader = new FileReader();
                reader.onload = function (e) {
                    document.getElementById("preview").src = e.target.result.toString();
                };
                _b = (_a = reader).readAsDataURL;
                return [4, fileRes.blob()];
            case 6:
                _b.apply(_a, [_c.sent()]);
                _c.label = 7;
            case 7:
                document.getElementById("file-upload").addEventListener("click", function (e) {
                    e.preventDefault();
                    return;
                });
                document.getElementById("submit").hidden = true;
                return [3, 9];
            case 8:
                document.getElementById("file-upload").addEventListener("change", function (event) {
                    file = event.target.files[0];
                    if (file == null)
                        return;
                    if (ALLOWED_TYPES.indexOf(file.type) === -1) {
                        document.getElementById("incorrectFileFormatMessage").innerText = "This file type is not allowed. Please use a .png, .jpg, or .jpeg file.";
                        document.getElementById("incorrectFileFormatMessage").hidden = false;
                        document.getElementById("file-upload").classList.add("is-invalid");
                    }
                    else {
                        document.getElementById("incorrectFileFormatMessage").hidden = true;
                        document.getElementById("file-upload").classList.remove("is-invalid");
                        var reader = new FileReader();
                        reader.onload = function (e) {
                            document.getElementById("preview").setAttribute("src", e.target.result.toString());
                            console.log("doing stuff");
                        };
                        reader.readAsDataURL(file);
                    }
                });
                document.getElementById("submit").addEventListener("click", function (event) { return __awaiter(void 0, void 0, void 0, function () {
                    var formData, response;
                    return __generator(this, function (_a) {
                        switch (_a.label) {
                            case 0:
                                event.preventDefault();
                                if (file == null) {
                                    document.getElementById("incorrectFileFormatMessage").innerText = "Please select a file.";
                                    document.getElementById("incorrectFileFormatMessage").hidden = false;
                                    document.getElementById("file-upload").classList.add("is-invalid");
                                    document.getElementById("preview").setAttribute("src", "../images/Upload Image Placeholder.png");
                                    return [2];
                                }
                                formData = new FormData();
                                formData.append("file", file);
                                formData.append("problem_id", problemId);
                                return [4, fetch("/api/submissions", {
                                        method: "POST",
                                        headers: {
                                            "Authorization": getToken()
                                        },
                                        body: formData
                                    })];
                            case 1:
                                response = _a.sent();
                                if (response.status !== 200) {
                                    document.getElementById("incorrectFileFormatMessage").innerText = "Something went wrong, please try again and maybe use a different file.";
                                    document.getElementById("incorrectFileFormatMessage").hidden = false;
                                    document.getElementById("file-upload").classList.add("is-invalid");
                                    document.getElementById("preview").setAttribute("src", "../images/Upload Image Placeholder.png");
                                    file = null;
                                }
                                else {
                                    document.location = "/user-pages/challenges.html";
                                }
                                return [2];
                        }
                    });
                }); });
                _c.label = 9;
            case 9: return [2];
        }
    });
}); })();
//# sourceMappingURL=challenge.js.map