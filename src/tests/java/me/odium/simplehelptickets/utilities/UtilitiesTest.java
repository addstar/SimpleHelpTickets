package me.odium.simplehelptickets.utilities;

import me.odium.simplehelptickets.database.DBConnection;
import me.odium.simplehelptickets.database.Database;
import me.odium.simplehelptickets.database.MySQLConnection;
import me.odium.simplehelptickets.database.Table;
import me.odium.simplehelptickets.helpers.objects.TestCommandSender;
import me.odium.simplehelptickets.helpers.TestHelper;
import me.odium.simplehelptickets.helpers.objects.TestWorld;
import me.odium.simplehelptickets.manager.TicketManager;
import me.odium.simplehelptickets.objects.Ticket;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.sql.Date;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 4/11/2018.
 */
public class UtilitiesTest {
    
    private Player testSender;
    @Before
    public void setup() {
        testSender = new TestCommandSender();
        TestWorld world = new TestWorld();
        testSender.teleport(world.getSpawnLocation());
        world.addPlayer(testSender);
    }
    @Test
    public void dateToString() {
        Date date = new Date(1541332471807L);
        String sDate = Utilities.dateToString(date, SimpleDateFormat.getDateInstance());
        assertEquals("04/11/2018", sDate);
    }

    @Test
    public void getTargetTableName() {
        assertEquals(TicketManager.getTableFromCommandString("someRandomIdeaCommand"), Table.IDEA);
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

    @Test
    public void sanitizeTest() {
        String[] args = {"1", "test", "this", "string"};
        String details = Utilities.santitizeTicketDetails(args);
    }

    @Test
    public void displayTest() {
        Ticket ticket = TestHelper.createTestTicket(true, testSender);
        Utilities.displayTicket(testSender, "ticket", ticket, false);
        testSender.sendMessage(" -------------------------");
        Utilities.ShowTicketInfo(testSender, ticket, true);
        ticket.setAdmin("TESTADMIN");
        ticket.setAdminReply("A silly test response");
        ticket.setUserReply("Yes its a very silly response");
        testSender.sendMessage(" -------------------------");
        Utilities.displayTicket(testSender, "ticket", ticket, true);
        testSender.sendMessage(" -------------------------");
        Utilities.ShowTicketInfo(testSender, ticket, true);


    }
}