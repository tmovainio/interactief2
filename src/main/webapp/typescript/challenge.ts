import { getToken, sendGet, sendPost } from "./api.js";
import { Challenge } from "./types.js";

let file: File;

const ALLOWED_TYPES: string[] = ["image/jpeg", "image/png", "image/jpg"];

(async () => {
    // Get the challenge ID from the URL
    const urlParams: URLSearchParams = new URLSearchParams(window.location.search);
    const problemId: string = urlParams.get("problem_id");

    // Get the challenge
    const challengeResponse: Response = await sendGet(`challenges/${problemId}`, { "Authorization": getToken() });
    const challenge: Challenge = (await challengeResponse.json()).challenge;

    const currentChallenge: HTMLElement = document.getElementById("current_challenge");
    currentChallenge.innerText = challenge.problem_name;

    const currentPoints = document.getElementById("current_points");
    currentPoints.innerText = `0 / ${challenge.score}`;

    const description = document.getElementById("descriptionContent");
    description.innerText = challenge.description;

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
        document.getElementById("file-upload").addEventListener("change", event => {
        file = (<HTMLInputElement>event.target).files[0];
        if (file == null) return;

        if (ALLOWED_TYPES.indexOf(file.type) === -1) {
            // This type is not allowed
            document.getElementById("incorrectFileFormatMessage").innerText = "This file type is not allowed. Please use a .png, .jpg, or .jpeg file.";
            document.getElementById("incorrectFileFormatMessage").hidden = false;
            document.getElementById("file-upload").classList.add("is-invalid");
        } else {
            document.getElementById("incorrectFileFormatMessage").hidden = true;
            document.getElementById("file-upload").classList.remove("is-invalid");

            // Show the preview
            const reader: FileReader = new FileReader();
            
            reader.onload = (e) => {
                document.getElementById("preview").setAttribute("src", e.target.result.toString());
                console.log("doing stuff");
                
            }

            reader.readAsDataURL(file);
        }
    });
        document.getElementById("submit").addEventListener("click", async (event) => {
            event.preventDefault();

            // If the file is null show an error message
            if (file == null) {
                document.getElementById("incorrectFileFormatMessage").innerText = "Please select a file.";
                document.getElementById("incorrectFileFormatMessage").hidden = false;
                document.getElementById("file-upload").classList.add("is-invalid");
                document.getElementById("preview").setAttribute("src", "../images/Upload Image Placeholder.png");
                return;
            }

            // const comment: string = (document.getElementById("comment") as HTMLInputElement).value;

            // Send POST with file
            const formData: FormData = new FormData();
            formData.append("file", file);
            formData.append("problem_id", problemId);

            const response: Response = await fetch("/api/submissions", {
                method: "POST",
                headers: {
                    "Authorization": getToken()
                },
                body: formData
            });

            // const res: { fileID: string } = await response.json();

            // TODO: better error handling
            if (response.status !== 200) {
                // Something went wrong
                // oops
                document.getElementById("incorrectFileFormatMessage").innerText = "Something went wrong, please try again and maybe use a different file.";
                document.getElementById("incorrectFileFormatMessage").hidden = false;
                document.getElementById("file-upload").classList.add("is-invalid");
                document.getElementById("preview").setAttribute("src", "../images/Upload Image Placeholder.png");
                file = null;
            } else {
                // Success
                document.location = "/user-pages/challenges.html";
            }
        });
    }
})();