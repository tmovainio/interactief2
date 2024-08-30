import { getToken, isLoggedIn, sendGet, sendPost, getLocations, createFadeoutNotification } from "./api.js";
import { Location, Puzzle } from "./types.js";

const ALLOWED_TYPES: string[] = ["image/jpeg", "image/png", "image/jpg"];

let locations: Location[] = [];

const renderLocations = async () => {
    // Get the locations and set them in the dropdowns
    locations = await getLocations();
    const challengeLocationSelect = document.getElementById("c_location");
    const puzzleLocationSelect = document.getElementById("p_location");
    locations.forEach((location: Location) => {
        const option: HTMLOptionElement = document.createElement("option");
        option.value = location.location_id.toString();
        option.innerText = location.location_name;
        challengeLocationSelect.appendChild(option);
        puzzleLocationSelect.appendChild(option.cloneNode(true));
    });
};

const renderPuzzles = async () => {
    const res: Response = await sendGet("puzzles", {
        "Authorization": getToken()
    });
    const json: [
        {
            "puzzle": Puzzle;
        }
    ] = await res.json();

    const puzzles: Puzzle[] = json.map((obj) => obj.puzzle);
    const puzzlesElement = document.getElementById("puzzlesContent");
    puzzlesElement.innerHTML = "";

    puzzles.forEach((puzzle: Puzzle) => {
        const col = document.createElement("div");
        col.classList.add("col");
        const card = document.createElement("div");
        card.classList.add("card", "mb-3", "shadow", "card-submitted", "h-100");
        const card2 = document.createElement("div");
        card2.classList.add("row", "g-0", "h-100");
        const cardImage = document.createElement("div");
        cardImage.classList.add("col-md-4", "row", "g-0", "h-100");
        const image = document.createElement("img");
        image.classList.add("img-fluid", "rounded-start", "h-100");
        image.style.objectFit = "cover";
        image.style.objectPosition = "center";
        image.src = "../images/Background Cookies.jpg";
        cardImage.appendChild(image);
        card2.appendChild(cardImage);
        

        const cardBody = document.createElement("div");
        cardBody.classList.add("card-body", "col-md-8");
        const cardTitle = document.createElement("h2");
        cardTitle.classList.add("card-title", "fw-bold", "display-5");
        cardTitle.innerText = puzzle.problem_name;
        const cardText = document.createElement("p");
        cardText.classList.add("card-text");
        cardText.innerText = `Unlocks location: ${locations.filter((location: Location) => location.location_id === puzzle.location_id)[0].location_name}`;
        cardBody.appendChild(cardTitle);
        cardBody.appendChild(cardText);
        const a = document.createElement("a");
        a.classList.add("btn", "btn-dark", "w-100");
        a.href = `commitee-puzzle.html?problem_id=${puzzle.problem_id}`;
        a.innerText = "Edit";
        cardBody.appendChild(a);

        card2.appendChild(cardBody);
        card.appendChild(card2);

        col.appendChild(card);
        puzzlesElement.appendChild(col);
    });

};

(async () => {
    if (!(await isLoggedIn())) {
        window.location.href = "/public-pages/login.html";
        return;
    }

    await renderLocations();
    

    // The create location part
    const createLocationButton = document.getElementById("l_createButton");
    createLocationButton.addEventListener("click", async (e) => {
        e.preventDefault();
        const name = (<HTMLInputElement>document.getElementById("l_location")).value;

        const res: Response = await sendPost("locations", { location_name: name }, {
            "Authorization": getToken()
        });

        if (res.status === 200) {
            (<HTMLInputElement>document.getElementById("l_location")).value = "";
            
            // Show success notification
            const main = document.getElementsByTagName("main")[0];
            main.prepend(createFadeoutNotification("Location created successfully!", "success"));
            await renderLocations();
        } else {
            // Show error notification
            const main = document.getElementsByTagName("main")[0];
            main.prepend(createFadeoutNotification("Error creating location, try again!", "danger"));
        }

        
        });
    
    // Create a challenge
    document.getElementById("c_createButton").addEventListener("click", async (e) => {
        e.preventDefault();

        const nameElement = (<HTMLInputElement>document.getElementById("c_name"));
        const locationElement = (<HTMLInputElement>document.getElementById("c_location"));
        const scoreElement = (<HTMLInputElement>document.getElementById("c_score"));
        const descriptionElement = (<HTMLInputElement>document.getElementById("c_description"));

        const name = nameElement.value;
        const location: number = Number(locationElement.value);
        const score: number = Number(scoreElement.value);
        const description = descriptionElement.value;

        const res: Response = await sendPost("challenges", {
            problem_name: name,
            location_id: location,
            score: score,
            description: description
        }, {
            "Authorization": getToken()
        });
        if (res.status === 200) {
            nameElement.value = "";
            locationElement.value = "";
            scoreElement.value = "";
            descriptionElement.value = "";

            // Show success notificationdescriptionElement
            const main = document.getElementsByTagName("main")[0];
            main.prepend(createFadeoutNotification("Challenge created successfully!", "success"));
        } else {
            // Show error notification
            const main = document.getElementsByTagName("main")[0];
            main.prepend(createFadeoutNotification("Error creating challenge, try again!", "danger"));
        }
    });

    // Create a puzzle
    const nameElement = (<HTMLInputElement>document.getElementById("p_name"));
    const locationElement = (<HTMLInputElement>document.getElementById("p_location"));
    const scoreElement = (<HTMLInputElement>document.getElementById("p_score"));
    const descriptionElement = (<HTMLInputElement>document.getElementById("p_description"));

    descriptionElement.accept = ALLOWED_TYPES.join(",");

    let description: File = null;
    document.getElementById("p_create").addEventListener("click", async (e) => {
        e.preventDefault();

        const name = nameElement.value;
        const location: number = Number(locationElement.value);

        if (description == null) {
            // Show error notification
            const main = document.getElementsByTagName("main")[0];
            main.prepend(createFadeoutNotification("Please select a file!", "danger"));
            descriptionElement.classList.add("is-invalid");
            return;
        }

        const formData = new FormData();
        const body = {
            problem_name: name,
            location_id: location,
        };
        formData.append("body", JSON.stringify(body));
        formData.append("file", description);

        console.log(formData.get("body"));
        console.log(formData.get("file"));

        const res: Response = await fetch("/api/puzzles", {
            method: "POST",
            headers: {
                "Authorization": getToken(),
                // "Content-Type": "multipart/form-data"
            },
            body: formData
        });

        if (res.status === 200) {
            nameElement.value = "";
            locationElement.value = "";
            scoreElement.value = "";
            descriptionElement.value = null;

            // Show success notification
            const main = document.getElementsByTagName("main")[0];
            main.prepend(createFadeoutNotification("Puzzle created successfully!", "success"));

            // Rerender all puzzles
            await renderPuzzles();
        } else {
            // Show error notification
            const main = document.getElementsByTagName("main")[0];
            main.prepend(createFadeoutNotification("Error creating puzzle, try again!", "danger"));
        }



    });

    

    descriptionElement.addEventListener("change", (e) => {
        console.log("file changed!");
        
        description = (<HTMLInputElement>e.target).files[0];
        if (description == null) {
            document.getElementById("p_preview").hidden = true;
            return;
        };

        if (ALLOWED_TYPES.indexOf(description.type) === -1) {
            // This type is not allowed
            document.getElementById("p_preview").hidden = true;
            // document.getElementById("incorrectFileFormatMessage").innerText = "This file type is not allowed. Please use a .png, .jpg, or .jpeg file.";
            // document.getElementById("incorrectFileFormatMessage").hidden = false;
            descriptionElement.classList.add("is-invalid");
            const main = document.getElementsByTagName("main")[0];
            main.prepend(createFadeoutNotification("This file type is not supported! Please try again.", "danger"));
        } else {
            // document.getElementById("incorrectFileFormatMessage").hidden = true;
            descriptionElement.classList.remove("is-invalid");
            // Show the preview
            const reader: FileReader = new FileReader();
            
            reader.onload = (e) => {
                document.getElementById("p_preview").setAttribute("src", e.target.result.toString());
                document.getElementById("p_preview").hidden = false;
                
            }

            reader.readAsDataURL(description);
        }
    });

    


    // Display all things
    // Display all puzzles
    await renderPuzzles();
})();