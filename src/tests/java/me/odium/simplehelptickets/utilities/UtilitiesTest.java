package me.odium.simplehelptickets.utilities;

import org.junit.Test;

import java.util.Calendar;
import java.sql.Date;

import static org.junit.Assert.*;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 4/11/2018.
 */
public class UtilitiesTest {

    @Test
    public void dateToString() {
        Date date = new Date(1541332471807L);
        String sDate = Utilities.dateToString(date);
        assertEquals("2018-11-04", sDate);
    }

    @Test
    public void getTargetTableName() {
        assertEquals(Utilities.GetTargetTableName("someRandomIdeaCommand"), Utilities.IDEA_TABLE_NAME);
        assertEquals(Utilities.GetTargetItemName(Utilities.IDEA_TABLE_NAME), "idea");
    }

    @Test
    public void numToString() {
        assertEquals("1", Utilities.NumToString(1));
    }


    @Test
    public void parseDate() {
        java.util.Date date = Utilities.parseDate("1970-01-01");
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);

            assertEquals(1970, cal.get(Calendar.YEAR));
        } else {
            assert (false);
        }
    }
}