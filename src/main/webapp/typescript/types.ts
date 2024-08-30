type Person = {
    s_numb: Number;
    name: string;
    phone_numb: string;
    teamName?: string | null;
    admin: boolean;
};

type Session = {
    token: string;
    user: Person;
    team: Team;
};

type Participant = {
    studentNumber: Number;
    name: string;
    phoneNumber: string;
    teamName: string;
};

type Administrator = {
    studentNumber: Number;
    privileges: string;
    getPerson(): Person;
};

type Team = {
    team_name: string;
    captain: {
        participant: Person;
    };
    content: string; // invite_link .....
    team_members: [
        {
            participant: Person;
        }
    ];
    // getMembers(): Person[];
};

type TeamInformation = {
    name: string;
    points: Number;
    ranking: Number;
    challengesSolved: Number;
};

type Submission = {
    team_name: string;
    problem_id: Number;
    submission: string;
    grading_description: string;
    score: Number;
    used_hint: boolean;
};

type Puzzle = {
    problem_id: Number;
    problem_name: string;
    score: Number;
    location_id: Number;
    image: string;
};

type Location = {
    location_id: Number;
    location_name: string;
};

type TeamStats = {
    team_name: string;
    total_score: Number;
    rank: Number;
};

type Problem = {
    problem_id: Number;
    problem_name: string;
    score: Number;
};

type Challenge = {
    problem_id: Number;
    location: Location;
    description: string;
    score: Number;
    problem_name: string;
};

type Crazy88 = {
    problem_id: Number;
    problem_name: string;
    score: Number;
    description: string;
}

export { Team, Participant, Person, Session, TeamStats, Submission, Problem, Challenge, Location, Puzzle, Crazy88 };