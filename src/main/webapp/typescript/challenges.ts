import { isLoggedIn, sendGet, getToken, getLocations } from "./api.js";
import { Session, Puzzle, Location, Challenge } from "./types.js";

// let locations: Location[] = [];

// const updateLocations = async () => {
//     locations = await getLocations();
// };

const renderLocations = async () => {
    const accordions = document.getElementById("accordions");

    // Get all challenges
    const response: Response = await sendGet("challenges", { "Authorization": getToken() });

    // Get the challenges
    const json: any[] = await response.json();
    const challenges: Challenge[] = json.map((obj) => obj.challenge);
    const unlockedLocations: Location[] = challenges.map((challenge: Challenge) => challenge.location);

    unlockedLocations.forEach((location: Location) => {
        const accordion = document.createElement("div");
        accordion.classList.add("accordion-item");
        const accordionHeader = document.createElement("h2");
        accordionHeader.classList.add("accordion-header");
        accordionHeader.id = `location${location.location_id}header`;
        const accordionButton = document.createElement("button");
        accordionButton.classList.add("accordion-button", "collapsed");
        accordionButton.type = "button";
        accordionButton.setAttribute("data-bs-toggle", "collapse");
        accordionButton.setAttribute("data-bs-target", `#location${location.location_id}`);
        accordionButton.setAttribute("aria-expanded", "false");
        accordionButton.setAttribute("aria-controls", `location${location.location_id}`);
        const buttonTitle = document.createElement("div");
        buttonTitle.classList.add("h2", "fw-bold");
        buttonTitle.innerText = location.location_name;
        accordionButton.appendChild(buttonTitle);
        accordionHeader.appendChild(accordionButton);
        accordion.appendChild(accordionHeader);

        // Challenges
        const locationChallenges: Challenge[] = challenges.filter((challenge: Challenge) => challenge.location.location_id === location.location_id);
        const accordionBody = document.createElement("div");
        accordionBody.classList.add("accordion-collapse", "collapse");
        accordionBody.id = `location${location.location_id}`;
        accordionBody.setAttribute("aria-labelledby", `location${location.location_id}header`);
        accordionBody.setAttribute("data-bs-parent", "#accordions");
        const accordionBodyShadow = document.createElement("div");
        accordionBodyShadow.classList.add("accordion-body", "shadow");
        const cards = document.createElement("div");
        cards.classList.add("row", "row-cols-1", "row-cols-md-2", "g-3", "text-center");
        locationChallenges.forEach((challenge: Challenge) => {
            const card = document.createElement("div");
            card.classList.add("col");
            const cardBody = document.createElement("div");
            cardBody.classList.add("card", "h-100");
            const cardBodyShadow = document.createElement("div");
            cardBodyShadow.classList.add("card-body", "d-flex", "flex-column");
            const cardTitle = document.createElement("h4");
            cardTitle.classList.add("card-title", "fw-bold");
            cardTitle.innerText = challenge.problem_name;
            const cardText = document.createElement("p");
            cardText.classList.add("card-text");
            cardText.innerText = challenge.description;
            const a = document.createElement("a");
            a.classList.add("btn", "btn-dark", "mt-auto");
            a.href = `specific_challenges.html?problem_id=${challenge.problem_id}`;
            a.innerText = "Open";
            cardBodyShadow.appendChild(cardTitle);
            cardBodyShadow.appendChild(cardText);
            cardBodyShadow.appendChild(a);
            cardBody.appendChild(cardBodyShadow);
            card.appendChild(cardBody);
            cards.appendChild(card);
        });
        accordionBodyShadow.appendChild(cards);
        accordionBody.appendChild(accordionBodyShadow);
        accordion.appendChild(accordionBody);
        accordions.appendChild(accordion);
    });



}

const renderPuzzles = async () => {
    // await updateLocations();
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
        cardText.innerText = `Unlocks location: ${puzzle.location_id}`;
        cardBody.appendChild(cardTitle);
        cardBody.appendChild(cardText);
        const a = document.createElement("a");
        a.classList.add("btn", "btn-dark", "w-100");
        a.href = `puzzle.html?problem_id=${puzzle.problem_id}`;
        a.innerText = "Open";
        cardBody.appendChild(a);

        card2.appendChild(cardBody);
        card.appendChild(card2);

        col.appendChild(card);
        puzzlesElement.appendChild(col);
    });

};

(async () => {
    // If we are not logged in redirect the user to the login page
    if (!await isLoggedIn()) {
        window.location.href = "/public-pages/login.html";
        return;
    }
    // Check if we are admin
    const session: Session = JSON.parse(localStorage.getItem("session"));
    
    if (session.user?.admin) {
        window.location.href = "/commitee-pages/commitee-dash.html";
    }

    // Get all puzzles and render them
    await renderPuzzles();

    

    // Render all location cards that are unlocked and add the challenges to them
    await renderLocations();


})();