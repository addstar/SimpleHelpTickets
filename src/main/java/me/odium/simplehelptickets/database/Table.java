package me.odium.simplehelptickets.database;

import java.util.HashMap;
import java.util.Map;

/**
 * Created for the AddstarMC Project. Created by Narimm on 5/11/2018.
 */
public enum Table {
    IDEA("sht_ideas", "idea"),
    TICKET("sht_tickets", "ticket");
    
    private static final Map<String, Table> BY_TYPE = new HashMap<>();
    private static final Map<String, Table> BY_TABLENAME = new HashMap<>();
    
    static {
        Table[] var3;
        var3 = values();
        for (Table table : var3) {
            BY_TYPE.put(table.type, table);
            BY_TABLENAME.put(table.tableName, table);
        }
        
    }
    
    public String tableName;
    public String type;
    
    Table(String tableName, String name) {
        this.tableName = tableName;
        this.type = name;
    }
    
    public static Table matchIdentifier(String id) {
        return BY_TYPE.get(id);
    }
    
    public static Table matchTableName(String tablename) {
        return BY_TABLENAME.get(tablename);
    }
}
