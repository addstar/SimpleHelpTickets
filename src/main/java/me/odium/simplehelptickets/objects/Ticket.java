package me.odium.simplehelptickets.objects;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 4/11/2018.
 */
public class Ticket {

    private final UUID owner;
    private final String description;
    private final LocalDateTime createdDate;
    private final TicketLocation location;
    private Integer id;
    private String ownerName = "";
    private String adminReply = "NONE";
    private String userReply = "NONE";
    private Status status = Status.OPEN;
    private String admin = "";
    private Timestamp expirationDate;

    public Ticket(int id, UUID owner, String description, LocalDateTime createdDate, Location location) {
        this.id = id;
        this.owner = owner;
        this.description = description;
        this.createdDate = createdDate;
        this.location = new TicketLocation(location, Bukkit.getServer().getServerId());
    }

    public Ticket(int id, UUID owner, String description, LocalDateTime createdDate, TicketLocation location) {
        this.id = id;
        this.owner = owner;
        this.description = description;
        this.createdDate = createdDate;
        this.location = location;
    }

    public Ticket(UUID owner, String description, LocalDateTime createdDate, Location location) {
        this.owner = owner;
        this.description = description;
        this.createdDate = createdDate;
        this.location = new TicketLocation(location, Bukkit.getServer().getServerId());
    }

    public Ticket(UUID owner, String description, LocalDateTime createdDate, TicketLocation location) {
        this.owner = owner;
        this.description = description;
        this.createdDate = createdDate;
        this.location = location;
    }
    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UUID getOwner() {
        return owner;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public TicketLocation getLocation() {
        return location;
    }

    public String getAdminReply() {
        return adminReply;
    }

    public void setAdminReply(String adminReply) {
        this.adminReply = adminReply;
    }

    public String getUserReply() {
        return userReply;
    }

    public void setUserReply(String userReply) {
        this.userReply = userReply;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getAdmin() {
        return admin;
    }

    public void setAdmin(String admin) {
        this.admin = admin;
    }

    public Timestamp getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Timestamp expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isOpen() {
        return (status == Status.OPEN);
    }

    public boolean hasAdminReply() {
        return !adminReply.equalsIgnoreCase("NONE");
    }
    public boolean hasUserReply() {
        return !userReply.equalsIgnoreCase("NONE");
    }

    public enum Status {
        OPEN,
        CLOSE
    }

}
