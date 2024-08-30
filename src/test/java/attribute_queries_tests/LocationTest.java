package attribute_queries_tests;

import nl.utwente.di.interactief2.JDBC.attribute_queries.LocationAttributes;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import java.util.List;

class LocationTest {


    static final String NOT = " not real";

    static final String testName = "testLocation";
    static int locationID;

    @BeforeEach
    void insertTestLocation() {
        createTestLocation();
    }

    @AfterEach
    void reset() {
        removeTestLocation();
    }

    @Test
    void alterLocationTest() {
        //Make sure you have the unaltered location in the db.
        JSONObject location = LocationAttributes.getLocationByID(locationID).getJSONObject(LocationAttributes.LOCATION_COLUMN);
        Assertions.assertEquals(testName, location.getString(LocationAttributes.LOCATION_NAME));

        //Make sure you can alter the location in the database.
        Assertions.assertTrue(LocationAttributes.updateLocation(testName + NOT, locationID));

        JSONObject newLocation = LocationAttributes.getLocationByID(locationID).getJSONObject(LocationAttributes.LOCATION_COLUMN);
        Assertions.assertEquals(testName + NOT, newLocation.getString(LocationAttributes.LOCATION_NAME));
    }

    static void removeTestLocation() {
        //Make sure the test location is deleted.
        Assertions.assertTrue(LocationAttributes.deleteLocation(locationID));
        locationID = Integer.MIN_VALUE;
    }
    static void createTestLocation() {

        //Make sure that a new test location gets inserted.
        Assertions.assertTrue(LocationAttributes.addLocation(testName));

        //Get the locationID of the newly inserted test location.
        List<JSONObject> allLocations = LocationAttributes.getAllLocations();
        for(JSONObject location : allLocations) {
            if(testName.equals(location.getJSONObject(LocationAttributes.LOCATION_COLUMN).getString(LocationAttributes.LOCATION_NAME))) {
                locationID = location.getJSONObject(LocationAttributes.LOCATION_COLUMN).getInt(LocationAttributes.LOCATION_ID);
            }
        }

        //Make sure we have a new locationID.
        Assertions.assertTrue(locationID >= 0);
    }

}
