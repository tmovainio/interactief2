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
var createStore = function (initialState, actions) {
    var state = initialState;
    var subscribers = [];
    var subscribe = function (subscriber) {
        subscribers.push(subscriber);
        return function () {
            subscribers.splice(subscribers.indexOf(subscriber), 1);
        };
    };
    var notify = function (newState) {
        for (var _i = 0, subscribers_1 = subscribers; _i < subscribers_1.length; _i++) {
            var subscriber = subscribers_1[_i];
            subscriber(newState);
        }
        state = newState;
    };
    var actualActions = actions(function (setStateAction) {
        var newVal = typeof setStateAction === "function" ? setStateAction(state) : setStateAction;
        notify(__assign(__assign({}, state), newVal));
    });
    return __assign({ state: state, subscribe: subscribe }, actualActions);
};
export { createStore };
//# sourceMappingURL=store.js.map