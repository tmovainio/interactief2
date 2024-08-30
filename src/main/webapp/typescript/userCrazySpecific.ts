import { createFadeoutNotification, getToken, isLoggedIn, sendDelete, sendGet, sendPatch, sendPost } from "./api.js";
import { Crazy88, Session } from "./types.js";

let file: File;

const ALLOWED_TYPES: string[] = ["image/jpeg", "image/png", "image/jpg"];

(async () => {

    if (!isLoggedIn()) {
        window.location.href = "/public-pages/login.html";
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
    currentPoints.innerText = `0 / ${crazy88.score.toString()}`;
    description.innerText = crazy88.description;

    // Have we already completed this challenge?
    const submissionRes: Response = await sendGet(`submissions/${problemId}`, { "Authorization": getToken() });
    if (submissionRes.status === 200) {
        // We have already submitted this challenge
        // Get the submission from the server and display the picture
        // Also display our score if we have it

        const fileRes: Response = await sendGet(`submissions/${problemId}/file`, { "Authorization": getToken() });
        if (fileRes.status === 404) {
            (<HTMLImageElement>document.getElementById("preview")).src = "/images/University_of_Twente_panoramic.jpg";
        } else {
            const reader: FileReader = new FileReader();
            reader.onload = (e) => {
                (<HTMLImageElement>document.getElementById("preview")).src = e.target.result.toString();
            }

            reader.readAsDataURL(await fileRes.blob());
        }

        // Stop from file uploading to work
        document.getElementById("file-upload").addEventListener("click", (e) => {
            e.preventDefault();
            return;
        });

        // Hide the submit button
        document.getElementById("submit").hidden = true;

    } else {
        // Handle the file upload
        const fileElement: HTMLInputElement = document.getElementById("file-upload") as HTMLInputElement;
        fileElement.addEventListener("change", (e) => {
            
            file = (<HTMLInputElement>e.target).files[0];
            if (file == null) {
                document.getElementById("p_preview").hidden = true;
                return;
            };

            if (ALLOWED_TYPES.indexOf(file.type) === -1) {
                // This type is not allowed
                document.getElementById("p_preview").hidden = true;
                // document.getElementById("incorrectFileFormatMessage").innerText = "This file type is not allowed. Please use a .png, .jpg, or .jpeg file.";
                document.getElementById("incorrectFileFormatMessage").hidden = false;
                fileElement.classList.add("is-invalid");
                const main = document.getElementsByTagName("main")[0];
                main.prepend(createFadeoutNotification("This file type is not supported! Please try again.", "danger"));
            } else {
                document.getElementById("incorrectFileFormatMessage").hidden = true;
                fileElement.classList.remove("is-invalid");
                // Show the preview
                const reader: FileReader = new FileReader();
                
                reader.onload = (e) => {
                    document.getElementById("preview").setAttribute("src", e.target.result.toString());
                    document.getElementById("preview").hidden = false;
                    
                }

                reader.readAsDataURL(file);
            }
        });

        // Handle the submission
        document.getElementById("submit").addEventListener("click", async (e) => {
            e.preventDefault();

            // If the file is null show an error message
            if (file == null) {
                document.getElementById("incorrectFileFormatMessage").innerText = "Please select a file.";
                document.getElementById("incorrectFileFormatMessage").hidden = false;
                fileElement.classList.add("is-invalid");
                document.getElementById("preview").setAttribute("src", "../images/Upload Image Placeholder.png");
                return;
            }

            // Send POST with file
            const formData: FormData = new FormData();
            formData.append("file", file);
            formData.append("problem_id", problemId);
            const res: Response = await fetch(`/api/submissions`, {
                method: "POST",
                headers: {
                    "Authorization": getToken(),
                },
                body: formData
            });

            if (res.status !== 200) {
                // Show error notification
                const main = document.getElementsByTagName("main")[0];
                main.prepend(createFadeoutNotification("Error submitting challenge, try again!", "danger"));

                // document.getElementById("incorrectFileFormatMessage").innerText = "Please select a file.";
                // document.getElementById("incorrectFileFormatMessage").hidden = false;
                fileElement.classList.add("is-invalid");
            } else {
                // Redirect back to crazy88 page
                window.location.href = "/user-pages/crazy.html";
                return;
            }
        });
    }


    

})();