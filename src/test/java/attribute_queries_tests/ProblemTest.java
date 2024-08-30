package attribute_queries_tests;

import attribute_queries_tests.LocationTest;
import nl.utwente.di.interactief2.JDBC.attribute_queries.LocationAttributes;
import nl.utwente.di.interactief2.JDBC.attribute_queries.ProblemAttributes;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;

public class ProblemTest {

    static final String NOT = " not real";


    static final String testCrazy88Name = "testCrazy88";
    static final String testCrazy88Description = "This is a test crazy88 problem";
    static final int testScoreCrazy88 = 3;
    static int testCrazy88ID;

    static final String testChallengeName = "testChallenge";
    static final String testChallengeDescription = "This is a test challenge";
    static final int testScoreChallenge = 2;
    static int testChallengeID;

    static final String testPuzzleName = "testPuzzle";
    static final String testPuzzleFilePath = "/this/is/a/test/file/path";
    static int testPuzzleID;

    @BeforeEach
    void setup() {
        LocationTest.createTestLocation();

        createTestCrazy88();
        createTestChallenge();
        createTestPuzzle();
    }

    @AfterEach
    void reset() {
        //Remove the test location from the database.
        LocationTest.removeTestLocation();

        //Delete the test problems from the database.
        deleteTestCrazy88();
        deleteTestChallenge();
        deleteTestPuzzle();
    }

    @Test
    void alterProblemsTest() {
        //Make sure you have the unaltered crazy88 problem in the db.
        JSONObject crazy88 = ProblemAttributes.getCrazy88ByID(testCrazy88ID).getJSONObject(ProblemAttributes.CRAZY88_COLUMN);
        Assertions.assertEquals(testCrazy88Name, crazy88.getString(ProblemAttributes.PROBLEM_NAME));
        Assertions.assertEquals(testCrazy88Description, crazy88.getString(ProblemAttributes.DESCRIPTION));

        //Make sure the crazy88 problem can be altered in the database.
        Assertions.assertTrue(ProblemAttributes.updateCrazy88(testCrazy88ID, testCrazy88Name + NOT, testScoreCrazy88 + 1, testCrazy88Description + NOT));
        JSONObject newCrazy88 = ProblemAttributes.getCrazy88ByID(testCrazy88ID).getJSONObject(ProblemAttributes.CRAZY88_COLUMN);
        Assertions.assertEquals(testCrazy88Name + NOT, newCrazy88.getString(ProblemAttributes.PROBLEM_NAME));
        Assertions.assertEquals(testCrazy88Description + NOT, newCrazy88.getString(ProblemAttributes.DESCRIPTION));

        //Make sure you have the unaltered challenge in the db.
        JSONObject challenge = ProblemAttributes.getChallengeByID(testChallengeID).getJSONObject(ProblemAttributes.CHALLENGE_COLUMN);
        Assertions.assertEquals(testChallengeName, challenge.getString(ProblemAttributes.PROBLEM_NAME));
        Assertions.assertEquals(LocationTest.locationID, challenge.getJSONObject(LocationAttributes.LOCATION_COLUMN).getInt(LocationAttributes.LOCATION_ID));
        Assertions.assertEquals(testChallengeDescription, challenge.getString(ProblemAttributes.DESCRIPTION));

        //Make sure the challenge can be altered in the database.
        Assertions.assertTrue(ProblemAttributes.updateChallenge(testChallengeID, testChallengeName + NOT, testScoreChallenge + 1, LocationTest.locationID, testChallengeDescription + NOT));
        JSONObject newChallenge = ProblemAttributes.getChallengeByID(testChallengeID).getJSONObject(ProblemAttributes.CHALLENGE_COLUMN);
        Assertions.assertEquals(testChallengeName + NOT, newChallenge.getString(ProblemAttributes.PROBLEM_NAME));
        Assertions.assertEquals(LocationTest.locationID, newChallenge.getJSONObject(LocationAttributes.LOCATION_COLUMN).getInt(LocationAttributes.LOCATION_ID));
        Assertions.assertEquals(testChallengeDescription + NOT, newChallenge.getString(ProblemAttributes.DESCRIPTION));

        //Make sure you have the unaltered puzzle in the db.
        JSONObject puzzle = ProblemAttributes.getPuzzleByID(testPuzzleID).getJSONObject(ProblemAttributes.PUZZLE_COLUMN);
        Assertions.assertEquals(testPuzzleName, puzzle.getString(ProblemAttributes.PROBLEM_NAME));
        Assertions.assertEquals(LocationTest.locationID, puzzle.getInt(LocationAttributes.LOCATION_ID));
        Assertions.assertEquals(testPuzzleFilePath, puzzle.getString(ProblemAttributes.FILE_PATH));

        //Make sure the puzzle can be altered in the database.
        Assertions.assertTrue(ProblemAttributes.updatePuzzle(testPuzzleID, testPuzzleName + NOT, testPuzzleFilePath + "/not/real", LocationTest.locationID));
        JSONObject newPuzzle = ProblemAttributes.getPuzzleByID(testPuzzleID).getJSONObject(ProblemAttributes.PUZZLE_COLUMN);
        Assertions.assertEquals(testPuzzleName + NOT, newPuzzle.getString(ProblemAttributes.PROBLEM_NAME));
        Assertions.assertEquals(LocationTest.locationID, newPuzzle.getInt(LocationAttributes.LOCATION_ID));
        Assertions.assertEquals(testPuzzleFilePath + "/not/real", newPuzzle.getString(ProblemAttributes.FILE_PATH));
    }

    //Static method to create a new test crazy88 problem.
    static void createTestCrazy88() {
        //Make sure we have a new test crazy88 in the database.
        Assertions.assertTrue(ProblemAttributes.insertCrazy88(testCrazy88Name, testCrazy88Description, testScoreCrazy88));

        //Get the new test crazy88 ID.
        List<JSONObject> allCrazy88 = ProblemAttributes.getAllCrazy88();
        JSONObject testCrazy88 = null;
        for(JSONObject crazy88 : allCrazy88) {
            JSONObject actualCrazy88 = crazy88.getJSONObject(ProblemAttributes.CRAZY88_COLUMN);
            if(testCrazy88Name.equals(actualCrazy88.getString(ProblemAttributes.PROBLEM_NAME))) {
                testCrazy88 = actualCrazy88;
            }
        }

        //Make sure we got an ID back and give it to the global variable.
        Assertions.assertNotNull(testCrazy88);
        testCrazy88ID = testCrazy88.getInt(ProblemAttributes.PROBLEM_ID);
    }

    //Static method to make sure the test crazy88 problem is liquidated.
    static void deleteTestCrazy88() {
        //Make sure it is gone, forever...
        Assertions.assertTrue(ProblemAttributes.deleteCrazy88(testCrazy88ID));
        testCrazy88ID = Integer.MIN_VALUE;
    }

    //Static method to create a new test challenge.
    static void createTestChallenge() {
        //Make sure we have a new test challenge in the database.
        Assertions.assertTrue(ProblemAttributes.insertChallenge(testChallengeName, LocationTest.locationID, testChallengeDescription, testScoreChallenge));

        //Get the new test challenge ID.
        List<JSONObject> allChallenges = ProblemAttributes.getAllChallenges();
        JSONObject testChallenge = null;
        for(JSONObject challenge : allChallenges) {
            JSONObject actualChallenge = challenge.getJSONObject(ProblemAttributes.CHALLENGE_COLUMN);
            if(testChallengeName.equals(actualChallenge.getString(ProblemAttributes.PROBLEM_NAME))) {
                testChallenge = actualChallenge;
            }
        }

        //Make sure we got an ID back and give it to the global variable.
        Assertions.assertNotNull(testChallenge);
        testChallengeID = testChallenge.getInt(ProblemAttributes.PROBLEM_ID);
    }

    //Static method to make sure the test challenge is destroyed.
    static void deleteTestChallenge() {
        //Make sure the test challenge does not have a continued existence in the database.
        Assertions.assertTrue(ProblemAttributes.deleteChallenge(testChallengeID));
        testChallengeID = Integer.MIN_VALUE;
    }

    //Static method to create a new test puzzle.
    static void createTestPuzzle() {
        //Make sure we have a new test puzzle in the database.
        Assertions.assertTrue(ProblemAttributes.insertPuzzle(testPuzzleName, testPuzzleFilePath, LocationTest.locationID));

        //Get the new test puzzle ID.
        List<JSONObject> allPuzzles = ProblemAttributes.getAllPuzzles();
        JSONObject testPuzzle = null;
        for(JSONObject puzzle : allPuzzles) {
            JSONObject actualChallenge = puzzle.getJSONObject(ProblemAttributes.PUZZLE_COLUMN);
            if(testPuzzleName.equals(actualChallenge.getString(ProblemAttributes.PROBLEM_NAME))) {
                testPuzzle = actualChallenge;
            }
        }

        //Make sure we got an ID back and give it to the global variable.
        Assertions.assertNotNull(testPuzzle);
        testPuzzleID = testPuzzle.getInt(ProblemAttributes.PROBLEM_ID);
    }


    //Static method to make sure the test puzzle is struck off the database.
    static void deleteTestPuzzle() {
        //Make sure it the test puzzle will not be found again in the database.
        Assertions.assertTrue(ProblemAttributes.deletePuzzle(testPuzzleID));
        testPuzzleID = Integer.MIN_VALUE;
    }
}
