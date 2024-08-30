var __assign = (this && this.__assign) || function () {
    __assign = Object.assign || function(t) {
        for (var s, i = 1, n = arguments.length; i < n; i++) {
            s = arguments[i];
            for (var p in s) if (Object.prototype.hasOwnProperty.call(s, p))
                t[p] = s[p];
        }
        return t;
    };
    return __assign.apply(this, arguments);
};
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
var getToken = function () {
    var session = JSON.parse(localStorage.getItem("session"));
    if (session == null)
        return null;
    return session.token;
};
var getSession = function (token) { return __awaiter(void 0, void 0, void 0, function () {
    var session, userResponse, response, user, teamResponse, json, team;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                session = {
                    token: token,
                    user: null,
                    team: null
                };
                return [4, sendGet('user', { "Authorization": token })];
            case 1:
                userResponse = _a.sent();
                return [4, userResponse.json()];
            case 2:
                response = _a.sent();
                user = response === null || response === void 0 ? void 0 : response.person;
                if (user == null) {
                    user = response === null || response === void 0 ? void 0 : response.participant;
                }
                session.user = user;
                console.log("Got the user: ");
                console.log(session.user);
                if (session.user.admin) {
                    return [2, session];
                }
                console.log("Getting team");
                return [4, sendGet('user/team', { "Authorization": token })];
            case 3:
                teamResponse = _a.sent();
                if (!(teamResponse.status === 404)) return [3, 4];
                console.log("Team is null!");
                session.team = null;
                return [3, 6];
            case 4: return [4, teamResponse.json()];
            case 5:
                json = _a.sent();
                team = json.team;
                session.team = team;
                _a.label = 6;
            case 6:
                localStorage.setItem("session", JSON.stringify(session));
                return [2, session];
        }
    });
}); };
var sendPost = function (url, data, headers) { return __awaiter(void 0, void 0, void 0, function () {
    var res;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                if (headers === undefined)
                    headers = {};
                return [4, fetch('/api/' + url, {
                        method: 'POST',
                        credentials: 'same-origin',
                        headers: __assign({
                            'Content-Type': 'application/json'
                        }, headers),
                        body: JSON.stringify(data)
                    })];
            case 1:
                res = _a.sent();
                if (res.status === 401) {
                    localStorage.removeItem("session");
                    window.location.href = "/public-pages/login.html";
                    return [2];
                }
                return [2, res];
        }
    });
}); };
var sendPut = function (url, data, headers) { return __awaiter(void 0, void 0, void 0, function () {
    var res;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                if (headers === undefined)
                    headers = {};
                return [4, fetch('/api/' + url, {
                        method: 'PUT',
                        credentials: 'same-origin',
                        headers: __assign({
                            'Content-Type': 'application/json'
                        }, headers),
                        body: JSON.stringify(data)
                    })];
            case 1:
                res = _a.sent();
                if (res.status === 401) {
                    localStorage.removeItem("session");
                    window.location.href = "/public-pages/login.html";
                    return [2];
                }
                return [2, res];
        }
    });
}); };
var sendDelete = function (url, headers) { return __awaiter(void 0, void 0, void 0, function () {
    var res;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                if (headers === undefined)
                    headers = {};
                return [4, fetch('/api/' + url, {
                        method: 'DELETE',
                        credentials: 'same-origin',
                        headers: __assign({
                            'Content-Type': 'application/json'
                        }, headers),
                    })];
            case 1:
                res = _a.sent();
                if (res.status === 401) {
                    localStorage.removeItem("session");
                    window.location.href = "/public-pages/login.html";
                    return [2];
                }
                return [2, res];
        }
    });
}); };
var sendPatch = function (url, data, headers) { return __awaiter(void 0, void 0, void 0, function () {
    var res;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                console.log("Sending patch request");
                if (headers === undefined)
                    headers = {};
                return [4, fetch('/api/' + url, {
                        method: 'PATCH',
                        credentials: 'same-origin',
                        headers: __assign({
                            'Content-Type': 'application/json'
                        }, headers),
                        body: JSON.stringify(data)
                    })];
            case 1:
                res = _a.sent();
                if (res.status === 401) {
                    localStorage.removeItem("session");
                    window.location.href = "/public-pages/login.html";
                    return [2];
                }
                return [2, res];
        }
    });
}); };
var sendGet = function (url, headers) { return __awaiter(void 0, void 0, void 0, function () {
    var res;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                if (headers === undefined)
                    headers = {};
                return [4, fetch('/api/' + url, {
                        method: 'GET',
                        credentials: 'same-origin',
                        headers: headers,
                    })];
            case 1:
                res = _a.sent();
                if (res.status === 401) {
                    localStorage.removeItem("session");
                    window.location.href = "/public-pages/login.html";
                    return [2];
                }
                return [2, res];
        }
    });
}); };
var isLoggedIn = function () { return __awaiter(void 0, void 0, void 0, function () {
    var session, token, response;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                session = JSON.parse(localStorage.getItem("session"));
                token = session === null || session === void 0 ? void 0 : session.token;
                if (token == null) {
                    return [2, new Promise(function (resolve, _) {
                            resolve(false);
                        })];
                }
                return [4, sendGet('user', { "Authorization": token })];
            case 1:
                response = _a.sent();
                return [2, new Promise(function (resolve, _) { return __awaiter(void 0, void 0, void 0, function () {
                        var res, user;
                        return __generator(this, function (_a) {
                            switch (_a.label) {
                                case 0: return [4, response.json()];
                                case 1:
                                    res = _a.sent();
                                    user = res.person;
                                    resolve(response.status === 200);
                                    return [2];
                            }
                        });
                    }); })];
        }
    });
}); };
var getTeams = function () { return __awaiter(void 0, void 0, void 0, function () {
    var response, json, teams;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4, sendGet('teams', { "Authorization": getToken() })];
            case 1:
                response = _a.sent();
                return [4, response.json()];
            case 2:
                json = _a.sent();
                teams = [];
                json.forEach(function (team) { return teams.push(team.team); });
                return [2, teams];
        }
    });
}); };
var getTeam = function (teamId) { return __awaiter(void 0, void 0, void 0, function () {
    var response;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4, sendGet("team/".concat(teamId), { "Authorization": getToken() })];
            case 1:
                response = _a.sent();
                return [2, response.json()];
        }
    });
}); };
var getMyTeam = function () { return __awaiter(void 0, void 0, void 0, function () {
    var response;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4, sendGet('user/team', { "Authorization": getToken() })];
            case 1:
                response = _a.sent();
                return [4, response.json()];
            case 2: return [2, _a.sent()];
        }
    });
}); };
var getSubmissions = function (graded, ungraded, team_name) {
    if (graded === void 0) { graded = false; }
    if (ungraded === void 0) { ungraded = false; }
    if (team_name === void 0) { team_name = null; }
    return __awaiter(void 0, void 0, void 0, function () {
        var url, response, json, submissions;
        return __generator(this, function (_a) {
            switch (_a.label) {
                case 0:
                    url = 'submissions?';
                    if (graded) {
                        url += 'graded=true&';
                    }
                    if (ungraded) {
                        url += 'ungraded=true&';
                    }
                    if (team_name != null) {
                        url += "team_name=".concat(team_name, "&");
                    }
                    return [4, sendGet(url, { "Authorization": getToken() })];
                case 1:
                    response = _a.sent();
                    return [4, response.json()];
                case 2:
                    json = _a.sent();
                    submissions = [];
                    json.forEach(function (submission) { return submissions.push(submission.submission); });
                    return [2, submissions];
            }
        });
    });
};
var getLocations = function () { return __awaiter(void 0, void 0, void 0, function () {
    var res, json, locations;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4, sendGet("locations", { "Authorization": getToken() })];
            case 1:
                res = _a.sent();
                return [4, res.json()];
            case 2:
                json = _a.sent();
                locations = json.map(function (obj) { return obj.location; });
                return [2, locations];
        }
    });
}); };
var createFadeoutNotification = function (message, type) {
    var notification = document.createElement("div");
    notification.classList.add("alert", "alert-".concat(type), "fadeout");
    notification.setAttribute("role", "alert");
    var text = document.createElement("span");
    text.innerHTML = message;
    notification.appendChild(text);
    setTimeout(function () {
        notification.remove();
    }, 4000);
    return notification;
};
export { createFadeoutNotification, sendPost, sendPut, sendPatch, sendGet, sendDelete, isLoggedIn, getTeams, getTeam, getMyTeam, getToken, getSession, getSubmissions, getLocations };
//# sourceMappingURL=api.js.map