self.addEventListener("install", (event) => {
    console.log("Service worker installing..."); 
});

// saveSubscription saves the subscription to the backend
const saveSubscription = async subscription => {
    const SERVER_URL = 'http://localhost:4000/save-subscription'
    const response = await fetch(SERVER_URL, {
        method: 'post',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(subscription),
    })
    return response.json()
}


// urlB64ToUint8Array is a magic function that will encode the base64 public key
// to Array buffer which is needed by the subscription option
const urlB64ToUint8Array = base64String => {
    const padding = '='.repeat((4 - (base64String.length % 4)) % 4)
    const base64 = (base64String + padding).replace(/\-/g, '+').replace(/_/g, '/')
    const rawData = atob(base64)
    const outputArray = new Uint8Array(rawData.length)
    for (let i = 0; i < rawData.length; ++i) {
        outputArray[i] = rawData.charCodeAt(i)
    }
    return outputArray
}

self.addEventListener("activate", async (event) => {
    console.log("Hello from service worker");
    // main();
    try {
        const applicationServerKey = urlB64ToUint8Array("BG9fXbE_8XsGgNKvG59tnwjfTmbMNvhxhHqikzX0Md0T-xC48HqxEYa4fSIQZ5ELXRwHf9I9aipa59XXnOltUdk");
        const options = { applicationServerKey, userVisibleOnly: true };
        const subscription = await self.registration.pushManager.subscribe(options)
        console.log(JSON.stringify(subscription))
        saveSubscription(subscription);
    } catch (err) {
        console.log('Error', err)
    }
});

self.addEventListener('push', function (event) {
    if (event.data) {
        console.log('Push event!! ', event.data.text())
        const { title, message } = event.data.json()
        showLocalNotification(title, message, self.registration)
    } else {
        console.log('Push event but no data')
    }
});

self.addEventListener('notificationclick', function (event) {
    console.log(event);
});

const showLocalNotification = (title, body, swRegistration) => {
    const options = {
        body,
        // here you can add more properties like icon, image, vibrate, etc.
    }
    swRegistration.showNotification(title, options)
}