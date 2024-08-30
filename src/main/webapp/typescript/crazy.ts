import { createFadeoutNotification, getToken, sendGet, sendPost } from "./api.js";
import { Crazy88 } from "./types.js";

const getCrazy88 = async () => {
    const res: Response = await sendGet("crazy88", { "Authorization": getToken() });

    const json: [
        {
            "crazy88": Crazy88;
        }
    ] = await res.json();
    return json.map((crazy) => crazy.crazy88);
};

const createCrazy88Element = (crazy88: Crazy88): HTMLDivElement => {
    const div = document.createElement("div");
    div.classList.add("p-0", "m-0");
    div.id = `crazy88-${crazy88.problem_id}`;
    const button: HTMLButtonElement = document.createElement("button");
    button.classList.add("list-group-item", "list-group-item-action", "d-flex", "justify-content-between", "align-items-center", "border-0", "border-bottom");
    button.dataset.toggle = "collapse";
    button.type = "button";
    button.setAttribute("data-bs-target", `#crazy88-${crazy88.problem_id}-collapse`);
    button.setAttribute("data-bs-toggle", "collapse");
    const title = document.createElement("div");
    title.classList.add("fw-bold", "h5");
    title.innerText = crazy88.problem_name;
    button.appendChild(title);
    div.appendChild(button);

    // When the thing is not collapsed anymore we show this
    const collapse = document.createElement("div");
    collapse.classList.add("collapse");
    collapse.id = `crazy88-${crazy88.problem_id}-collapse`;
    const card = document.createElement("div");
    card.classList.add("card", "card-body", "rounded-0");
    const h4 = document.createElement("h4");
    h4.classList.add("fw-bold", "h4");
    h4.innerText = "Description";
    card.appendChild(h4);
    const p = document.createElement("p");
    p.innerText = crazy88.description;
    card.appendChild(p);
    const h5 = document.createElement("h5");
    h5.classList.add("fw-bold", "h5");
    h5.innerText = "Max score";
    card.appendChild(h5);
    const p2 = document.createElement("p");
    p2.innerText = `Max score: ${crazy88.score.toString()}`;
    card.appendChild(p2);
    const a = document.createElement("a");
    a.classList.add("btn", "btn-dark", "w-100");
    a.innerText = "Open";
    a.href = `commitee-crazy-specific.html?problem_id=${crazy88.problem_id}`;
    card.appendChild(a);
    collapse.appendChild(card);
    div.appendChild(collapse);

    return div;
};

const renderCrazy88 = async () => {
    const crazy88: Crazy88[] = await getCrazy88();
    const create = document.getElementById("create");
    
    crazy88.forEach((crazy) => {
        create.parentElement.appendChild(createCrazy88Element(crazy));
    });
};


(async () => {
    await renderCrazy88();
    

    document.getElementById("c_createButton").addEventListener("click", async (e) => {
        e.preventDefault();
        const nameElement: HTMLInputElement = document.getElementById("c_name") as HTMLInputElement;
        const descriptionElement: HTMLInputElement = document.getElementById("c_description") as HTMLInputElement;
        const scoreElement: HTMLInputElement = document.getElementById("c_score") as HTMLInputElement;
    
        const name = nameElement.value;
        const description = descriptionElement.value;
        const score: number = parseInt(scoreElement.value);

        const res: Response = await sendPost("crazy88", {
            "problem_name": name,
            "description": description,
            "score": score
        }, {
            "Authorization": getToken()
        })

        if (res.status === 200) {
            nameElement.value = "";
            descriptionElement.value = "";
            scoreElement.value = "";
            const main = document.getElementsByTagName("main")[0];
            main.prepend(createFadeoutNotification("Crazy 88 created successfully!", "success"));
            // Update page
            await renderCrazy88();
        } else {
            const main = document.getElementsByTagName("main")[0];
            main.prepend(createFadeoutNotification("Error creating crazy 88, try again!", "danger"));
        }
    });


})();