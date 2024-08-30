package attribute_queries_tests;

import nl.utwente.di.interactief2.JDBC.attribute_queries.PersonAttributes;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.Objects;

class PersonTest {


    static final String NOT = " not real";

    static final int testStudentNumber = 1000000;
    static final String testName = "testPerson";
    static final String testPhoneNumber = "+31 6 00000000";
    static final String testPass = "password";


    @BeforeEach
    void setup() {
        insertTestPerson();
    }

    @AfterEach
    void reset() {
        deleteTestPerson();
    }

    @Test
    void checkPasswordTest() {
        //Check if a wrong password returns false.
        Assertions.assertFalse(PersonAttributes.checkPassword(testStudentNumber, "wrong " + testPass));

        //Check if the correct password returns true.
        Assertions.assertTrue(PersonAttributes.checkPassword(testStudentNumber, testPass));
    }

    @Test
    void checkIfAdminTest() {
        //Make sure the check returns false.
        Assertions.assertFalse(PersonAttributes.checkIfAdmin(testStudentNumber));

        //If person becomes admin, check it if the method returns true now.
        //This is also a test to test granting and revoking person admin powers.
        if(grantPersonAdmin()) {
            Assertions.assertTrue(PersonAttributes.checkIfAdmin(testStudentNumber));

            Assertions.assertTrue(revokePersonAdmin());

            Assertions.assertFalse(PersonAttributes.checkIfAdmin(testStudentNumber));
        }

    }

    @Test
    void participantTest() {
        //Make sure the test person is not a participant.
        Assertions.assertNull(PersonAttributes.getParticipantByID(testStudentNumber));

        //Check if a team can be created.
        Assertions.assertTrue(TeamTest.createTeam(testStudentNumber));

        //Make sure the test person is now a participant.
        Assertions.assertNotNull(PersonAttributes.getParticipantByID(testStudentNumber));

        //Make sure the test team is deleted.
        Assertions.assertTrue(TeamTest.removeTestTeam());
    }

    @Test
    void changePerson() {
        //Make sure that the initial values are in the test person.
        JSONObject person = PersonAttributes.getPersonByID(testStudentNumber).getJSONObject(PersonAttributes.PERSON_OBJECT_NAME);
        if(!Objects.isNull(person)) {
            Assertions.assertEquals(testStudentNumber, person.getInt(PersonAttributes.STUDENT_NUMBER));
            Assertions.assertEquals(testName, person.getString(PersonAttributes.NAME));
            Assertions.assertEquals(testPhoneNumber, person.getString(PersonAttributes.PHONE_NUMBER));
            Assertions.assertTrue(PersonAttributes.checkPassword(testStudentNumber, testPass));

            //Edit phone number
            if(PersonAttributes.changePerson(testStudentNumber, null, testPhoneNumber + NOT)) {
                JSONObject personTemp = PersonAttributes.getPersonByID(testStudentNumber).getJSONObject(PersonAttributes.PERSON_OBJECT_NAME);
                Assertions.assertEquals(testPhoneNumber + NOT, personTemp.getString(PersonAttributes.PHONE_NUMBER));
            }

            //Edit password
            if(PersonAttributes.changePerson(testStudentNumber, testPass + NOT, null)) {
                Assertions.assertTrue(PersonAttributes.checkPassword(testStudentNumber, testPass + NOT));
            }
        }

    }

    //Method to grant the test persons admin powers.
    private boolean grantPersonAdmin() {
        return PersonAttributes.insertAdmin(testStudentNumber);
    }

    //Method to revoke the test persons admin powers.
    private boolean revokePersonAdmin() {
        return PersonAttributes.removeAdmin(testStudentNumber);
    }

    static void insertTestTeamMember(int index) {
        Assertions.assertTrue(PersonAttributes.insertPerson(testStudentNumber + index, testName + "TeamMember", testPhoneNumber, testPass));
    }

    static void deleteTestTeamMember(int index) {
        //Make sure that the test team member person is deleted.
        boolean check = true;
        try {
            PersonAttributes.deletePerson(testStudentNumber + index);
        } catch (SQLException e) {
            check = false;
        }

        Assertions.assertTrue(check);
    }

    //Static method to delete a test person.
    static void deleteTestPerson() {
        //Make sure that the test person is deleted.
        boolean check = true;
        try {
            PersonAttributes.deletePerson(testStudentNumber);
        } catch (SQLException e) {
            check = false;
        }

        Assertions.assertTrue(check);
    }

    //Static method to insert a test person.
    static void insertTestPerson() {
        //Make sure there is a blank slate test person in the database.
        Assertions.assertTrue(PersonAttributes.insertPerson(testStudentNumber, testName, testPhoneNumber, testPass));
    }

}
