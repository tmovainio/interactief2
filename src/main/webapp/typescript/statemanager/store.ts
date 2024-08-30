// const createLeader = () => {
//     let info = {
//         direction: "north",
//         velocity: 10
//     };

//     type Selector = keyof typeof info;

//     type Drone = {
//         selector: Selector,
//         callback: (val: any) => void
//     };

//     // type Drone = (newInfo: typeof info) => void;

//     const drones: Drone[] = [];

//     const joinSwarm = (selector: Selector, callback: (val: any) => void) => {
//         drones.push({selector, callback});
//     }

//     // const joinSwarm = (drone: Drone) => {
//     //     drones.push(drone);
//     // }

//     const leaveSwarm = (drone: Drone) => {
//         drones.splice(drones.indexOf(drone), 1);
//     }

//     const notify = (newInfo: typeof info) => {
//         for (const { selector, callback } of drones) {
//             // Check if the direction has changed since 
//             // the last notify call
//             // if so, notify all drones with the "direction" selector
//             if (info.direction !== newInfo.direction && selector === "direction") {
//                 callback(newInfo.direction);
//             } else if (info.velocity !== newInfo.velocity && selector === "velocity") {
//                 callback(newInfo.velocity);
//             }
//         }

//         info = newInfo;
//     }

//     // const notify = (newInfo: typeof info) => {
//     //     drones.forEach(drone => drone(newInfo));
//     // }

//     return { joinSwarm, leaveSwarm, notify };
// };

type Subscriber<STATE> = (state: STATE) => void;

type Store<STATE, ACTIONS> = {
    state: STATE,
    subscribe: (subscriber: Subscriber<STATE>) => () => void,
} & ACTIONS;


const createStore = <STATE, ACTIONS>(
    initialState: STATE,
    actions: (set: (state:
        | Partial<STATE>
        | ((current: STATE) => Partial<STATE>)
    ) => void) => ACTIONS
): Store<STATE, ACTIONS> => {
    let state: STATE = initialState;

    
    const subscribers: Subscriber<STATE>[] = [];

    const subscribe = (subscriber: Subscriber<STATE>): () => void => {
        subscribers.push(subscriber);

        // Return a function that can be used to unsubscribe
        return () => {
            subscribers.splice(subscribers.indexOf(subscriber), 1);
        }
    };

    const notify = (newState: STATE) => {
        for (const subscriber of subscribers) {
            subscriber(newState);
        }

        state = newState;
    };

    const actualActions = actions((setStateAction) => {
        const newVal = typeof setStateAction === "function" ? setStateAction(state) : setStateAction;

        notify({ ...state, ...newVal });
    });

    return {state, subscribe, ...actualActions};
};



// (() => {
//     const counter = createStore({ count: 0 }, (update: any) => ({
//         increment: () => update((state: any) => ({ count: state.count + 1 })),
//         decrement: () => update((state: any) => ({ count: state.count - 1 })),
//         reset: () => update({ count: 0 }),
//         set: (count: number) => update({ count })
//     }));
//     const counter1 = counter.subscribe((state: any) => console.log("1: ", state));
//     const counter2 = counter.subscribe((state: any) => console.log("2: ", state));
    
//     counter.increment();
//     counter.decrement();
//     counter.decrement();
//     counter.reset();
//     counter.set(100);

//     counter1();
//     counter2();
//     counter.reset();
    
// })();


export { createStore, Store };