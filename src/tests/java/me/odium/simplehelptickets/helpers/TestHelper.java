package me.odium.simplehelptickets.helpers;

import me.odium.simplehelptickets.objects.Ticket;
import me.odium.simplehelptickets.objects.TicketLocation;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.help.HelpMap;
import org.bukkit.inventory.*;
import org.bukkit.loot.LootTable;
import org.bukkit.map.MapView;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.*;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.CachedServerIcon;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.sql.Date;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 4/11/2018.
 */
public class TestHelper {
    public static Server createServer() {
        return new Server() {
            @Override
            public String getName() {
                return "TEST";
            }

            @Override
            public String getVersion() {
                return "1.13";
            }

            @Override
            public String getBukkitVersion() {
                return "1.13";
            }

            @Override
            public Collection<? extends Player> getOnlinePlayers() {
                return null;
            }

            @Override
            public int getMaxPlayers() {
                return 20;
            }

            @Override
            public int getPort() {
                return 9999;
            }

            @Override
            public int getViewDistance() {
                return 16;
            }

            @Override
            public String getIp() {
                return "127.0.0.1";
            }

            @Override
            public String getServerName() {
                return "TEST";
            }

            @Override
            public String getServerId() {
                return "TEST";
            }

            @Override
            public String getWorldType() {
                return "TEST";
            }

            @Override
            public boolean getGenerateStructures() {
                return false;
            }

            @Override
            public boolean getAllowEnd() {
                return false;
            }

            @Override
            public boolean getAllowNether() {
                return false;
            }

            @Override
            public boolean hasWhitelist() {
                return false;
            }

            @Override
            public void setWhitelist(boolean b) {

            }

            @Override
            public Set<OfflinePlayer> getWhitelistedPlayers() {
                return null;
            }

            @Override
            public void reloadWhitelist() {

            }

            @Override
            public int broadcastMessage(String s) {
                return 0;
            }

            @Override
            public String getUpdateFolder() {
                return null;
            }

            @Override
            public File getUpdateFolderFile() {
                return null;
            }

            @Override
            public long getConnectionThrottle() {
                return 0;
            }

            @Override
            public int getTicksPerAnimalSpawns() {
                return 0;
            }

            @Override
            public int getTicksPerMonsterSpawns() {
                return 0;
            }

            @Override
            public Player getPlayer(String s) {
                return null;
            }

            @Override
            public Player getPlayerExact(String s) {
                return null;
            }

            @Override
            public List<Player> matchPlayer(String s) {
                return null;
            }

            @Override
            public Player getPlayer(UUID uuid) {
                return null;
            }

            @Override
            public PluginManager getPluginManager() {
                return null;
            }

            @Override
            public BukkitScheduler getScheduler() {
                return null;
            }

            @Override
            public ServicesManager getServicesManager() {
                return null;
            }

            @Override
            public List<World> getWorlds() {
                return null;
            }

            @Override
            public World createWorld(WorldCreator worldCreator) {
                return null;
            }

            @Override
            public boolean unloadWorld(String s, boolean b) {
                return false;
            }

            @Override
            public boolean unloadWorld(World world, boolean b) {
                return false;
            }

            @Override
            public World getWorld(String s) {
                return null;
            }

            @Override
            public World getWorld(UUID uuid) {
                return null;
            }

            @Override
            public MapView getMap(short i) {
                return null;
            }

            @Override
            public MapView createMap(World world) {
                return null;
            }

            @Override
            public void reload() {

            }

            @Override
            public void reloadData() {

            }

            @Override
            public Logger getLogger() {
                return null;
            }

            @Override
            public PluginCommand getPluginCommand(String s) {
                return null;
            }

            @Override
            public void savePlayers() {

            }

            @Override
            public boolean dispatchCommand(CommandSender commandSender, String s) throws CommandException {
                return false;
            }

            @Override
            public boolean addRecipe(Recipe recipe) {
                return false;
            }

            @Override
            public List<Recipe> getRecipesFor(ItemStack itemStack) {
                return null;
            }

            @Override
            public Iterator<Recipe> recipeIterator() {
                return null;
            }

            @Override
            public void clearRecipes() {

            }

            @Override
            public void resetRecipes() {

            }

            @Override
            public Map<String, String[]> getCommandAliases() {
                return null;
            }

            @Override
            public int getSpawnRadius() {
                return 0;
            }

            @Override
            public void setSpawnRadius(int i) {

            }

            @Override
            public boolean getOnlineMode() {
                return false;
            }

            @Override
            public boolean getAllowFlight() {
                return false;
            }

            @Override
            public boolean isHardcore() {
                return false;
            }

            @Override
            public void shutdown() {

            }

            @Override
            public int broadcast(String s, String s1) {
                return 0;
            }

            @Override
            public OfflinePlayer getOfflinePlayer(String s) {
                return null;
            }

            @Override
            public OfflinePlayer getOfflinePlayer(UUID uuid) {
                return null;
            }

            @Override
            public Set<String> getIPBans() {
                return null;
            }

            @Override
            public void banIP(String s) {

            }

            @Override
            public void unbanIP(String s) {

            }

            @Override
            public Set<OfflinePlayer> getBannedPlayers() {
                return null;
            }

            @Override
            public BanList getBanList(BanList.Type type) {
                return null;
            }

            @Override
            public Set<OfflinePlayer> getOperators() {
                return null;
            }

            @Override
            public GameMode getDefaultGameMode() {
                return null;
            }

            @Override
            public void setDefaultGameMode(GameMode gameMode) {

            }

            @Override
            public ConsoleCommandSender getConsoleSender() {
                return null;
            }

            @Override
            public File getWorldContainer() {
                return null;
            }

            @Override
            public OfflinePlayer[] getOfflinePlayers() {
                return new OfflinePlayer[0];
            }

            @Override
            public Messenger getMessenger() {
                return null;
            }

            @Override
            public HelpMap getHelpMap() {
                return null;
            }

            @Override
            public Inventory createInventory(InventoryHolder inventoryHolder, InventoryType inventoryType) {
                return null;
            }

            @Override
            public Inventory createInventory(InventoryHolder inventoryHolder, InventoryType inventoryType, String s) {
                return null;
            }

            @Override
            public Inventory createInventory(InventoryHolder inventoryHolder, int i) throws IllegalArgumentException {
                return null;
            }

            @Override
            public Inventory createInventory(InventoryHolder inventoryHolder, int i, String s) throws IllegalArgumentException {
                return null;
            }

            @Override
            public Merchant createMerchant(String s) {
                return null;
            }

            @Override
            public int getMonsterSpawnLimit() {
                return 0;
            }

            @Override
            public int getAnimalSpawnLimit() {
                return 0;
            }

            @Override
            public int getWaterAnimalSpawnLimit() {
                return 0;
            }

            @Override
            public int getAmbientSpawnLimit() {
                return 0;
            }

            @Override
            public boolean isPrimaryThread() {
                return false;
            }

            @Override
            public String getMotd() {
                return null;
            }

            @Override
            public String getShutdownMessage() {
                return null;
            }

            @Override
            public Warning.WarningState getWarningState() {
                return null;
            }

            @Override
            public ItemFactory getItemFactory() {
                return null;
            }

            @Override
            public ScoreboardManager getScoreboardManager() {
                return null;
            }

            @Override
            public CachedServerIcon getServerIcon() {
                return null;
            }

            @Override
            public CachedServerIcon loadServerIcon(File file) throws IllegalArgumentException, Exception {
                return null;
            }

            @Override
            public CachedServerIcon loadServerIcon(BufferedImage bufferedImage) throws IllegalArgumentException, Exception {
                return null;
            }

            @Override
            public int getIdleTimeout() {
                return 0;
            }

            @Override
            public void setIdleTimeout(int i) {

            }

            @Override
            public ChunkGenerator.ChunkData createChunkData(World world) {
                return null;
            }

            @Override
            public BossBar createBossBar(String s, BarColor barColor, BarStyle barStyle, BarFlag... barFlags) {
                return null;
            }

            @Override
            public Entity getEntity(UUID uuid) {
                return null;
            }

            @Override
            public Advancement getAdvancement(NamespacedKey namespacedKey) {
                return null;
            }

            @Override
            public Iterator<Advancement> advancementIterator() {
                return null;
            }

            @Override
            public BlockData createBlockData(Material material) {
                return null;
            }

            @Override
            public BlockData createBlockData(Material material, Consumer<BlockData> consumer) {
                return null;
            }

            @Override
            public BlockData createBlockData(String s) throws IllegalArgumentException {
                return null;
            }

            @Override
            public BlockData createBlockData(Material material, String s) throws IllegalArgumentException {
                return null;
            }

            @Override
            public <T extends Keyed> Tag<T> getTag(String s, NamespacedKey namespacedKey, Class<T> aClass) {
                return null;
            }

            @Override
            public LootTable getLootTable(NamespacedKey namespacedKey) {
                return null;
            }

            @Override
            public UnsafeValues getUnsafe() {
                return null;
            }

            @Override
            public Spigot spigot() {
                return null;
            }

            @Override
            public void sendPluginMessage(Plugin plugin, String s, byte[] bytes) {

            }

            @Override
            public Set<String> getListeningPluginChannels() {
                return null;
            }
        };
    }

    public static Plugin createPlugin() {
        return new Plugin() {
            @Override
            public File getDataFolder() {
                return new File(".");
            }

            @Override
            public PluginDescriptionFile getDescription() {
                return null;
            }

            @Override
            public FileConfiguration getConfig() {
                return null;
            }

            @Override
            public InputStream getResource(String s) {
                return null;
            }

            @Override
            public void saveConfig() {

            }

            @Override
            public void saveDefaultConfig() {

            }

            @Override
            public void saveResource(String s, boolean b) {

            }

            @Override
            public void reloadConfig() {

            }

            @Override
            public PluginLoader getPluginLoader() {
                return null;
            }

            @Override
            public Server getServer() {
                return TestHelper.createServer();
            }

            @Override
            public boolean isEnabled() {
                return true;
            }

            @Override
            public void onDisable() {

            }

            @Override
            public void onLoad() {

            }

            @Override
            public void onEnable() {

            }

            @Override
            public boolean isNaggable() {
                return false;
            }

            @Override
            public void setNaggable(boolean b) {

            }

            @Override
            public ChunkGenerator getDefaultWorldGenerator(String s, String s1) {
                return null;
            }

            @Override
            public Logger getLogger() {
                return Logger.getLogger("TEST");
            }

            @Override
            public String getName() {
                return "TEST_PLUGIN";
            }

            @Override
            public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
                return true;
            }

            @Override
            public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
                return new ArrayList<>();
            }
        };
    }

    public static PermissionAttachment createPA() {
        return new PermissionAttachment(null, new Permissible() {
            @Override
            public boolean isPermissionSet(String s) {
                return false;
            }

            @Override
            public boolean isPermissionSet(Permission permission) {
                return false;
            }

            @Override
            public boolean hasPermission(String s) {
                return false;
            }

            @Override
            public boolean hasPermission(Permission permission) {
                return false;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
                return null;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin) {
                return null;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
                return null;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, int i) {
                return null;
            }

            @Override
            public void removeAttachment(PermissionAttachment permissionAttachment) {

            }

            @Override
            public void recalculatePermissions() {

            }

            @Override
            public Set<PermissionAttachmentInfo> getEffectivePermissions() {
                return null;
            }

            @Override
            public boolean isOp() {
                return false;
            }

            @Override
            public void setOp(boolean b) {

            }
        });
    }

    public static Ticket createTestTicket() {
        Ticket ticket = new Ticket(1, UUID.randomUUID(), "This is a Test ticket", new Date(System.currentTimeMillis()), new TicketLocation(0D, 0D, 0D, "TEST", 0F, 0F, "TEST_SERVER"));
        return ticket;
    }

}
