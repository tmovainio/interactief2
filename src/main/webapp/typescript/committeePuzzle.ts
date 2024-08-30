import { getToken, sendGet, getLocations } from "./api.js";
import { Puzzle, Location } from "./types.js";

(async () => {
    // Get the problem id from the url
    const urlParams = new URLSearchParams(window.location.search);
    const problemId = urlParams.get('problem_id');

    // Set the spinner animation to be visible
    document.getElementById("loading").hidden = false;

    // Load all stuff
    const res: Response = await sendGet(`puzzles/${problemId}`, { "Authorization": getToken() });
    const puzzle: Puzzle = (await res.json()).puzzle;

    // Load the image
    const fileRes: Response = await sendGet(`puzzles/${problemId}/file`, { "Authorization": getToken() });
    if (fileRes.status === 404) {
        (<HTMLImageElement>document.getElementById("descriptionImage")).src = "/images/University_of_Twente_panoramic.jpg";
    } else {
        const reader: FileReader = new FileReader();
        reader.onload = (e) => {
            (<HTMLImageElement>document.getElementById("descriptionImage")).src = e.target.result.toString();
        }

        reader.readAsDataURL(await fileRes.blob());
    }
    
    
    // Set the title of the challenge
    document.getElementById("current_challenge").innerText = puzzle.problem_name;

    // Set the max points of the challenge
    document.getElementById("current_points").innerText = puzzle.score.toString();

    // Do the locations
    const locations: Location[] = await getLocations();
    const locationSelector = document.getElementById("location-selector");
    locations.forEach((location: Location) => {
        const option: HTMLOptionElement = document.createElement("option");
        option.innerText = location.location_name;
        option.value = location.location_id.toString();
        if (location.location_id === puzzle.location_id) {
            option.selected = true;
        }
        locationSelector.appendChild(option);
    });



    // Done loading, hide the spinner
    document.getElementById("loading").hidden = true;
})();