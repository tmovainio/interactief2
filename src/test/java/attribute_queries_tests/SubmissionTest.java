package attribute_queries_tests;

import attribute_queries_tests.PersonTest;
import attribute_queries_tests.ProblemTest;
import nl.utwente.di.interactief2.JDBC.attribute_queries.SubmissionAttributes;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SubmissionTest {


    static final String NOT = " not real";

    static int testSubmissionProblemID;
    static final String testSubmissionSubmission = "/this/is/a/test/submission";
    static final String testSubmissionGradingDescription = "testSubmissionGradingDescription";
    static final int pass = 1;
    static final int fail = 0;

    @BeforeEach
    void setUp() {
        PersonTest.insertTestPerson();
        TeamTest.createTeam(PersonTest.testStudentNumber);
        TeamTest.approveTeam();

        createSubmissionForCrazy88();
    }

    @AfterEach
    void reset() {
        removeSubmissionForCrazy88();

        TeamTest.removeTestTeam();
        PersonTest.deleteTestPerson();
    }

    @Test
    void approveSubmissionTest() {
        //Make sure we have the ungraded test submission in the db.
        JSONObject submission = SubmissionAttributes.getSubmissionByTeamNameAndProblemID(TeamTest.testTeam, testSubmissionProblemID).getJSONObject(SubmissionAttributes.SUBMISSION_COLUMN);
        Assertions.assertFalse(submission.has(SubmissionAttributes.GRADING_DESCRIPTION));

        //Make sure we are able to grade the test submission.
        Assertions.assertTrue(SubmissionAttributes.gradeSubmission(TeamTest.testTeam, testSubmissionGradingDescription, testSubmissionProblemID, fail));
        JSONObject newSubmission = SubmissionAttributes.getSubmissionByTeamNameAndProblemID(TeamTest.testTeam, testSubmissionProblemID).getJSONObject(SubmissionAttributes.SUBMISSION_COLUMN);
        Assertions.assertEquals(fail, newSubmission.getInt(SubmissionAttributes.SUBMISSION_SCORE));

        //Make sure we are able to grade the test submission (again)
        Assertions.assertTrue(SubmissionAttributes.gradeSubmission(TeamTest.testTeam, testSubmissionGradingDescription, testSubmissionProblemID, pass));
        JSONObject newSubmissionPass = SubmissionAttributes.getSubmissionByTeamNameAndProblemID(TeamTest.testTeam, testSubmissionProblemID).getJSONObject(SubmissionAttributes.SUBMISSION_COLUMN);
        Assertions.assertEquals(pass, newSubmissionPass.getInt(SubmissionAttributes.SUBMISSION_SCORE));
    }

    @Test
    void askForHint() {
        //Make sure we have the new test submission.
        JSONObject submission = SubmissionAttributes.getSubmissionByTeamNameAndProblemID(TeamTest.testTeam, testSubmissionProblemID);
        Assertions.assertFalse(submission.getJSONObject(SubmissionAttributes.SUBMISSION_COLUMN).has(SubmissionAttributes.USED_HINT));

        //Make sure the test team can ask for a hint.
        SubmissionAttributes.askHint(TeamTest.testTeam, testSubmissionProblemID);
        submission = SubmissionAttributes.getSubmissionByTeamNameAndProblemID(TeamTest.testTeam, testSubmissionProblemID);
        Assertions.assertTrue(submission.getJSONObject(SubmissionAttributes.SUBMISSION_COLUMN).getBoolean(SubmissionAttributes.USED_HINT));
    }

    @Test
    void scoreTest() {
        //Make sure there is only the test submission in the database.
        Assertions.assertEquals(0, SubmissionAttributes.getTotalScoreOfTeam(TeamTest.testTeam).getJSONObject(SubmissionAttributes.SUBMISSION_SCORE).getInt(SubmissionAttributes.SCORE_OF_LEADERBOARD));

        //Make sure asking for hint reduces the score of the test team by 1.
        SubmissionAttributes.askHint(TeamTest.testTeam, testSubmissionProblemID);
        Assertions.assertEquals(-1, SubmissionAttributes.getTotalScoreOfTeam(TeamTest.testTeam).getJSONObject(SubmissionAttributes.SUBMISSION_SCORE).getInt(SubmissionAttributes.SCORE_OF_LEADERBOARD));

        //Make sure approving the submission with (at minimum) a pass improves the score (in this case by 1.
        Assertions.assertTrue(SubmissionAttributes.gradeSubmission(TeamTest.testTeam, testSubmissionGradingDescription, testSubmissionProblemID, pass));
        Assertions.assertEquals(0, SubmissionAttributes.getTotalScoreOfTeam(TeamTest.testTeam).getJSONObject(SubmissionAttributes.SUBMISSION_SCORE).getInt(SubmissionAttributes.SCORE_OF_LEADERBOARD));
    }

    static void createSubmissionForCrazy88() {
        //Make sure there is a crazy88 problem in the db.
        ProblemTest.createTestCrazy88();

        //Get the problemID for the submission.
        testSubmissionProblemID = ProblemTest.testCrazy88ID;

        //Make sure there is a new submission in the db
        Assertions.assertTrue(SubmissionAttributes.submitSubmission(TeamTest.testTeam, testSubmissionProblemID, testSubmissionSubmission));
    }

    static void removeSubmissionForCrazy88() {
        //Make sure the test submission is removed from the db.
        Assertions.assertTrue(SubmissionAttributes.deleteSubmission(testSubmissionProblemID, TeamTest.testTeam));

        //Make sure the test crazy88 problem is removed from the db.
        ProblemTest.deleteTestCrazy88();
    }






}
