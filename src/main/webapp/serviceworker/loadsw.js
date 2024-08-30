const registerServiceWorker = async () => {
    if ("serviceWorker" in navigator) {
        try {
            const registration = await navigator.serviceWorker.register("/serviceworker/sw.js");
            await registration.update(); // force update, at least for development
            if (registration.installing) {
                console.log("Service worker installing");
            } else if (registration.waiting) {
                console.log("Service worker installed");
            } else if (registration.active) {
                console.log("Service worker active");
            } 
            return registration;
        } catch (error) {
            console.error(`Registration failed with ${error}`);
        }
    }
};

const requestNotificationPermission = async () => {
    const permission = await window.Notification.requestPermission();
    // value of permission can be 'granted', 'default', 'denied'
    // granted: user has accepted the request
    // default: user has dismissed the notification permission popup by clicking on x
    // denied: user has denied the request.
    if(permission !== 'granted'){
        throw new Error('Permission not granted for Notification');
    } else {
        console.log('Permission granted for Notification');
    }
}

(async () => {
    const swRegistration = await registerServiceWorker();
    await requestNotificationPermission();
})();