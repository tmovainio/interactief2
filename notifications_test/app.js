const express = require('express')
const cors = require('cors')
const bodyParser = require('body-parser')
const webpush = require('web-push')
const app = express()
app.use(cors())
app.use(bodyParser.json())
const port = 4000
app.get('/', (req, res) => res.send('Hello World!'))

const fs = require('fs');
const db = fs.readFileSync('db.json');
const dummyDb = JSON.parse(db); //dummy in memory store

const saveToDatabase = async subscription => {
  // Since this is a demo app, I am going to save this in a dummy in memory store. Do not do this in your apps.
  // Here you should be writing your db logic to save it.
    //   dummyDb.subscription = subscription
  dummyDb.push(subscription);
  fs.writeFileSync('./db.json', JSON.stringify(dummyDb));
}
// The new /save-subscription endpoint
app.post('/save-subscription', async (req, res) => {
    console.log('save-subscription');
    const subscription = req.body
    console.log(subscription);
    await saveToDatabase(subscription) //Method to save the subscription to Database
    res.json({ message: 'success' })
})
const vapidKeys = {
    publicKey: 'BG9fXbE_8XsGgNKvG59tnwjfTmbMNvhxhHqikzX0Md0T-xC48HqxEYa4fSIQZ5ELXRwHf9I9aipa59XXnOltUdk',
    privateKey: 'c0hqIcu5oBvdO-2Q2t8YyEz6_YlmugozZQgoQzFhmzg',
}

//setting our previously generated VAPID keys
webpush.setVapidDetails(
    'mailto:myuserid@email.com',
    vapidKeys.publicKey,
    vapidKeys.privateKey
);

//function to send the notification to the subscribed device
const sendNotification = (subscription, dataToSend) => {
    webpush.sendNotification(subscription, dataToSend);
}
//route to test send notification
app.get('/send-notification', (req, res) => {
    // const subscription = dummyDb.subscription //get subscription from your databse here.
    const message = req.query.message;
    const title = req.query.title;
    dummyDb.forEach(subscription => {
        sendNotification(subscription, JSON.stringify({ title, message }));
    });
    res.json({ message: 'message sent' });
});

app.listen(port, () => console.log(`Example app listening on port ${port}!`));