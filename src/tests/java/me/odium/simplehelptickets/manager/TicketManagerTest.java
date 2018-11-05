package me.odium.simplehelptickets.manager;

import me.odium.simplehelptickets.database.Database;
import me.odium.simplehelptickets.database.MySQLConnection;
import me.odium.simplehelptickets.database.Table;
import me.odium.simplehelptickets.helpers.TestHelper;
import me.odium.simplehelptickets.objects.Ticket;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Before;
import org.junit.Test;
import sun.rmi.runtime.Log;

import javax.xml.crypto.Data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 5/11/2018.
 */
public class TicketManagerTest {
    TicketManager manager;

    @Before
    public void Setup() throws FileNotFoundException {
        Logger log = Logger.getLogger("Test");
        Database database = new MySQLConnection(null, log);
        manager = new TicketManager(database, log);
    }

    @Test
    public void getTargetItemName() {
        Table table = TicketManager.getTargetItemName(Table.IDEA.tableName);
        assertEquals(table, Table.IDEA);
    }

    @Test
    public void getTableFromCommandString() {
        Table table = TicketManager.getTableFromCommandString("somerandmomecommandidea");
        assertEquals(table, Table.IDEA);
    }

    @Test
    public void getTableName() {
        Table table = TicketManager.getTableName("idea");
        assertEquals(table, Table.IDEA);
    }

    @Test
    public void saveTicket() {
        Ticket ticket = TestHelper.createTestTicket(false);
        assertTrue(manager.saveTicket(ticket, Table.TICKET));
        List<Ticket> result = manager.getTickets(Table.TICKET, "owner = '" + ticket.getOwner().toString() + "'", 1);
        assert (result.size() == 1);
        assertEquals(result.get(0), ticket);
    }

    @Test
    public void deleteTickets() {
    }

    @Test
    public void deleteTicketbyId() {
    }

    @Test
    public void saveTickets() {
    }

    @Test
    public void getTicketCount() {
    }

    @Test
    public void getTickets() {
    }

    @Test
    public void expireItems() {
    }

    @Test
    public void findTickets() {
    }
}