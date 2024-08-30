import { createFadeoutNotification, getToken, isLoggedIn, sendDelete, sendGet, sendPatch } from "./api.js";
import { Crazy88, Session } from "./types.js";

(async () => {

    if (!isLoggedIn()) {
        window.location.href = "/public-pages/login.html";
        return;
    }

    // Check if the user is an admin
    // If they are not, send them to the join/create team page
    const session: Session = JSON.parse(localStorage.getItem("session"));
    if (!session.user?.admin) {
        window.location.href = "/user-pages/jointeam.html";
        return;
    }

    // Get the crazy88 id from the url
    const urlParams = new URLSearchParams(window.location.search);
    const problemId = urlParams.get("problem_id");

    // Get the crazy88 challenge
    const res: Response = await sendGet(`crazy88/${problemId}`, { "Authorization": getToken() });
    const json: {
        "crazy88": Crazy88;
    } = await res.json();
    const crazy88: Crazy88 = json.crazy88;

    // Set the crazy88 information
    const currentChallenge: HTMLElement = document.getElementById("current_challenge") as HTMLElement;
    const currentPoints: HTMLElement = document.getElementById("current_points") as HTMLElement;
    const description: HTMLElement = document.getElementById("descriptionParagraph") as HTMLElement;

    currentChallenge.innerText = crazy88.problem_name;
    currentPoints.innerText = crazy88.score.toString();
    description.innerText = crazy88.description;

    document.getElementById("deleteBtn").addEventListener("click", async () => {
        const res: Response = await sendDelete(`crazy88/${problemId}`, { "Authorization": getToken() });
        if (res.status === 200) {
            window.location.href = "/commitee-pages/commitee-crazy.html";
            return;
        }
    });

    let originalContent: Node = null;

    // You click the edit button
    document.getElementById('editBtn').addEventListener('click', () => {
        const elementsToEdit: string[] = ['current_challenge', 'current_points', 'description'];
        elementsToEdit.forEach((elementId: string) => {
            document.getElementById(elementId).setAttribute("contentEditable", "true");
            document.getElementById(elementId).classList.add("editable");
        });

        // Hide the edit button and show the save and discard buttons
        document.getElementById('editBtn').style.display = 'none';
        document.getElementById('saveBtn').style.display = 'inline-block';
        document.getElementById('discardBtn').style.display = 'inline-block';
    });

    // You click the discard button
    document.getElementById('discardBtn').addEventListener('click', function() {
        // Replace the edited content with the original content
        const container = document.querySelector('.container');
        container.parentNode.replaceChild(originalContent, container);
                        
        // Hide the save and discard buttons and show the edit button
        document.getElementById('saveBtn').style.display = 'none';
        document.getElementById('discardBtn').style.display = 'none';
        document.getElementById('editBtn').style.display = 'inline-block';
    });

    // You click the save button
    document.getElementById('saveBtn').addEventListener('click', async () => {
        // Send the new stuff to the server
        const newChallenge: string = currentChallenge.innerText;
        const newPoints: string = currentPoints.innerText;
        const newDescription: string = description.innerText;
        const res: Response = await sendPatch(`crazy88/${problemId}`, {
            problem_name: newChallenge,
            score: parseInt(newPoints),
            description: newDescription,
            problem_id: parseInt(problemId)
        }, { "Authorization": getToken() });
        if (res.status !== 200) {
            const main = document.getElementsByTagName("main")[0];
            main.prepend(createFadeoutNotification("Something went wrong with updating this crazy88 challenge. Please try again later.", "danger"));
            return;
        }

        // Make each element non-editable
        const editableElements = document.querySelectorAll('.editable');
        editableElements.forEach(function(element) {
            element.setAttribute('contentEditable', "false");
            element.classList.remove('editable');
        });
                        
        // Hide the save and discard buttons and show the edit button
        document.getElementById('saveBtn').style.display = 'none';
        document.getElementById('discardBtn').style.display = 'none';
        document.getElementById('editBtn').style.display = 'inline-block';
    });
})();