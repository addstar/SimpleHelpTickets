package me.odium.simplehelptickets.manager;

import javafx.util.converter.TimeStringConverter;
import me.odium.simplehelptickets.database.DBConnection;
import me.odium.simplehelptickets.database.Database;
import me.odium.simplehelptickets.database.MySQLConnection;
import me.odium.simplehelptickets.database.Table;
import me.odium.simplehelptickets.helpers.objects.TestCommandSender;
import me.odium.simplehelptickets.helpers.TestHelper;
import me.odium.simplehelptickets.helpers.objects.TestWorld;
import me.odium.simplehelptickets.objects.Pair;
import me.odium.simplehelptickets.objects.Ticket;
import me.odium.simplehelptickets.objects.TicketLocation;
import me.odium.simplehelptickets.utilities.Utilities;
import org.bukkit.Location;
import org.bukkit.Utility;
import org.bukkit.entity.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.logging.Logger;

import static org.junit.Assert.*;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 5/11/2018.
 */
public class TicketManagerTest {
    TicketManager manager;
    Player testSender;
    DBConnection database;

    @Before
    public void Setup() throws FileNotFoundException {
        TemporaryFolder path = new TemporaryFolder();
        try {
            path.create();
            File file = path.newFolder("ticketTest");
            Logger log = Logger.getLogger("Test");
            database = new DBConnection(file, log);
            database.open();
            manager = new TicketManager(database, log, true);
        } catch (IOException e) {
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        testSender = new TestCommandSender();
        TestWorld world = new TestWorld();
        testSender.teleport(world.getSpawnLocation());
        world.addPlayer(testSender);
        
    }

    private void createTables() {
        manager.createTables();
    }
    
    @Test
    public void createTableTest() {
        assertTrue(manager.createTables());
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

    //@Test
    public void saveTicket() {
        Ticket ticket = TestHelper.createTestTicket(false, testSender);
        createTables();
        assertTrue(manager.saveTicket(ticket, Table.TICKET));
        List<Ticket> result = manager.getTickets(Table.TICKET,
                "uuid = '" + testSender.getUniqueId() + "'",
                1);
        assertEquals(1, result.size());
        assertEquals(result.get(0).getOwnerName(), ticket.getOwnerName());
        assertEquals(result.get(0).getLocation().getWorld(), ticket.getLocation().getWorld());
        Ticket updated = result.get(0);
        String adminReply = "A random Response";
        updated.setAdminReply(adminReply);
        manager.saveTicket(updated, Table.TICKET);
        Pair<Integer, Long> out = manager.getTicketCount(testSender, Table.TICKET,
                Ticket.Status.OPEN,
                null);
        assertEquals((Integer) 1, out.object1);
        result = manager.getTickets(Table.TICKET, "id =" + updated.getId(), 1);
        assertEquals(adminReply, result.get(0).getAdminReply());
        testSender.teleport((Location) null);
        ticket = TestHelper.createTestTicket(false, testSender);
        manager.saveTicket(ticket, Table.TICKET);
        result = manager.getTickets(Table.TICKET,
                "uuid = '" + testSender.getUniqueId() + "'",
                1);
        assertEquals(1, result.size());
        Utilities.displayTicket(testSender, Table.TICKET.type, result.get(0), true);
    }

    //@Test
    public void deleteTickets() {
        createTables();
        Ticket ticket = TestHelper.createTestTicket(false, testSender);
        ticket.setStatus(Ticket.Status.CLOSE);
        manager.saveTicket(ticket, Table.TICKET);
        Pair<Integer, Long> closed = manager.getTicketCount(null, Table.TICKET, Ticket.Status.CLOSE, null);
        assertNotSame((Integer) 0, closed.object1);
        manager.deleteTickets(Table.TICKET, Ticket.Status.CLOSE);
        Pair<Integer, Long> update = manager.getTicketCount(null, Table.TICKET,
                Ticket.Status.CLOSE, null);
        assertEquals((Integer) 0, update.object1);
    
    }

    //@Test
    public void deleteTicketbyId() {
        Ticket ticket = TestHelper.createTestTicket(false, testSender);
        manager.saveTicket(ticket, Table.TICKET);
        Pair<Integer, Long> update = manager.getTicketCount(testSender, Table.TICKET,
                null, null);
        assertEquals((Integer) 1, update.object1);
        List<Ticket> result = manager.getTickets(Table.TICKET,
                "uuid='" + testSender.getUniqueId().toString() + "'", 1);
        Ticket updated = result.get(0);
        manager.deleteTicketbyId(Table.TICKET, updated.getId());
        update = manager.getTicketCount(testSender, Table.TICKET,
                null, null);
        assertEquals((Integer) 0, update.object1);
    }

    // @Test
    public void saveTickets() {
        createTables();
        Ticket ticket = TestHelper.createTestTicket(false, testSender);
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(ticket);
        manager.saveTickets(tickets, Table.TICKET);
        Pair<Integer, Long> update = manager.getTicketCount(testSender, Table.TICKET,
                null, null);
        assertEquals((Integer) 1, update.object1);
    
    }
    
    @Test
    public void getTicketsTest() {
        createTables();
        Ticket ticket = TestHelper.createTestTicket(false, testSender);
        Ticket ticket2 = TestHelper.createTestTicket(false, testSender);
        List<Ticket> tickets = new ArrayList<>();
        tickets.add(ticket);
        tickets.add(ticket2);
        manager.saveTickets(tickets, Table.TICKET);
        List<Ticket> results = manager.getTickets(Table.TICKET, testSender, Ticket.Status.OPEN);
        assertEquals(2, results.size());
        manager.deleteTicketbyId(Table.TICKET, results.get(0).getId());
        results = manager.getTickets(Table.TICKET, testSender, Ticket.Status.OPEN);
        assertEquals(1, results.size());
    
    }
    @Test
    public void expireTicketsTest() {
        createTables();
        Ticket ticket = TestHelper.createTestTicket(false, testSender);
        Timestamp time = new Timestamp(System.currentTimeMillis() - 100000000);
        ticket.setExpirationDate(time);
        manager.saveTicket(ticket, Table.TICKET);
        List<Ticket> results = manager.getTickets(Table.TICKET, testSender, Ticket.Status.OPEN);
        assertEquals(1, results.size());
        manager.expireItems(Table.TICKET);
        results = manager.getTickets(Table.TICKET, testSender, Ticket.Status.OPEN);
    
    }
    
}