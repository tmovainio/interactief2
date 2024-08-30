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
import { getToken, isLoggedIn, sendGet } from "./api.js";
var getCrazy88 = function () { return __awaiter(void 0, void 0, void 0, function () {
    var res, json;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4, sendGet("crazy88", { "Authorization": getToken() })];
            case 1:
                res = _a.sent();
                return [4, res.json()];
            case 2:
                json = _a.sent();
                return [2, json.map(function (crazy) { return crazy.crazy88; })];
        }
    });
}); };
var createCrazy88Element = function (crazy88, fileURL) {
    var div = document.createElement("div");
    div.classList.add("p-0", "m-0");
    div.id = "crazy88-".concat(crazy88.problem_id);
    var button = document.createElement("button");
    button.classList.add("list-group-item", "list-group-item-action", "d-flex", "justify-content-between", "align-items-center", "border-0", "border-bottom");
    button.dataset.toggle = "collapse";
    button.type = "button";
    button.setAttribute("data-bs-target", "#crazy88-".concat(crazy88.problem_id, "-collapse"));
    button.setAttribute("data-bs-toggle", "collapse");
    var title = document.createElement("div");
    title.classList.add("fw-bold", "h5");
    title.innerText = crazy88.problem_name;
    button.appendChild(title);
    div.appendChild(button);
    var collapse = document.createElement("div");
    collapse.classList.add("collapse");
    collapse.id = "crazy88-".concat(crazy88.problem_id, "-collapse");
    var card = document.createElement("div");
    card.classList.add("card", "card-body", "rounded-0");
    var h4 = document.createElement("h4");
    h4.classList.add("fw-bold", "h4");
    h4.innerText = "Description";
    card.appendChild(h4);
    var p = document.createElement("p");
    p.innerText = crazy88.description;
    card.appendChild(p);
    var h5 = document.createElement("h5");
    h5.classList.add("fw-bold", "h5");
    h5.innerText = "Max score";
    card.appendChild(h5);
    var p2 = document.createElement("p");
    p2.innerText = "Max score: ".concat(crazy88.score.toString());
    card.appendChild(p2);
    var a = document.createElement("a");
    a.classList.add("btn", "btn-dark", "w-100");
    a.innerText = "Open";
    a.href = "".concat(fileURL, "?problem_id=").concat(crazy88.problem_id);
    card.appendChild(a);
    collapse.appendChild(card);
    div.appendChild(collapse);
    return div;
};
var renderCrazy88 = function () { return __awaiter(void 0, void 0, void 0, function () {
    var crazy88, list;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4, getCrazy88()];
            case 1:
                crazy88 = _a.sent();
                list = document.getElementById("crazy88list");
                list.innerHTML = "";
                crazy88.forEach(function (crazy) {
                    list.appendChild(createCrazy88Element(crazy, "crazy-specific.html"));
                });
                return [2];
        }
    });
}); };
(function () { return __awaiter(void 0, void 0, void 0, function () {
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                if (!isLoggedIn()) {
                    window.location.href = "/public-pages/login.html";
                    return [2];
                }
                return [4, renderCrazy88()];
            case 1:
                _a.sent();
                return [2];
        }
    });
}); })();
//# sourceMappingURL=userCrazy.js.map