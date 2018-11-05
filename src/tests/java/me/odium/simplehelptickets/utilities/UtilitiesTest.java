package me.odium.simplehelptickets.utilities;

import me.odium.simplehelptickets.database.Table;
import me.odium.simplehelptickets.helpers.TestCommandSender;
import me.odium.simplehelptickets.helpers.TestHelper;
import me.odium.simplehelptickets.manager.TicketManager;
import me.odium.simplehelptickets.objects.Ticket;
import org.bukkit.command.CommandSender;
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
        CommandSender sender = new TestCommandSender();
        Ticket ticket = TestHelper.createTestTicket(true);
        Utilities.displayTicket(sender, "ticket", ticket, false);
        sender.sendMessage(" -------------------------");
        Utilities.ShowTicketInfo(sender, ticket, true);
        ticket.setAdmin("TESTADMIN");
        ticket.setAdminReply("A silly test response");
        ticket.setUserReply("Yes its a very silly response");
        sender.sendMessage(" -------------------------");
        Utilities.displayTicket(sender, "ticket", ticket, false);
        sender.sendMessage(" -------------------------");
        Utilities.ShowTicketInfo(sender, ticket, true);


    }
}