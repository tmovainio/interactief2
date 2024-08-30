package attribute_queries_tests;

import nl.utwente.di.interactief2.JDBC.attribute_queries.SettingAttributes;
import nl.utwente.di.interactief2.JDBC.attribute_queries.SubmissionAttributes;
import nl.utwente.di.interactief2.JDBC.attribute_queries.TeamAttributes;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;


public class SettingTest {

    @BeforeEach
    void setup() {
        //Setup the testing environment.
        PersonTest.insertTestPerson();
        TeamTest.createTeam(PersonTest.testStudentNumber);
        TeamTest.approveTeam();

        SubmissionTest.createSubmissionForCrazy88();
    }

    @AfterEach
    void reset() {
        //Make sure to reset the testing environment.
        SubmissionTest.removeSubmissionForCrazy88();

        TeamTest.removeTestTeam();
        PersonTest.deleteTestPerson();
    }

    @Test
    void getSettingTest() throws SQLException {

        //This should never throw an error, otherwise the score board toggle is non-functional.
        try {
            System.out.println(SettingAttributes.getSettingByName(SettingAttributes.SCORE_BOARD_TOGGLE));
        } catch (SQLException e) {
            throw new SQLException();
        }
    }

    @Test
    void leaderboardTest() {
        //Make sure there is a graded submission in the db.
        Assertions.assertTrue(SubmissionAttributes.gradeSubmission(TeamTest.testTeam, SubmissionTest.testSubmissionGradingDescription, SubmissionTest.testSubmissionProblemID, ProblemTest.testScoreCrazy88));


        boolean isVisible;

        //Execute this twice so it can test both scenarios.
        for(int i = 0; i < 2; i++) {
            try {
                isVisible = SettingAttributes.getSettingByName(SettingAttributes.SCORE_BOARD_TOGGLE);

                if (isVisible) {

                    //Get the total score of the test team.
                    int scoreOfTesTeam = Integer.MIN_VALUE;
                    List<JSONObject> scoreBoard = SubmissionAttributes.getScoreBoard();

                    //Get the test submission score out of the score board.
                    for(JSONObject score : scoreBoard) {
                        if(TeamTest.testTeam.equals(score.getJSONObject(SubmissionAttributes.SUBMISSION_SCORE).getString(TeamAttributes.TEAM_NAME))) {
                            scoreOfTesTeam = score.getJSONObject(SubmissionAttributes.SUBMISSION_SCORE).getInt(SubmissionAttributes.SCORE_OF_LEADERBOARD);
                        }

                    }

                    Assertions.assertEquals(3, scoreOfTesTeam);

                    //Make sure is will be invisible fo the next run.
                    SettingAttributes.updateSetting(SettingAttributes.SCORE_BOARD_TOGGLE, false);
                } else {
                    //If not visible, make sure is will be visible fo the next run.
                    SettingAttributes.updateSetting(SettingAttributes.SCORE_BOARD_TOGGLE, true);

                    Assertions.assertTrue(SettingAttributes.getSettingByName(SettingAttributes.SCORE_BOARD_TOGGLE));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
