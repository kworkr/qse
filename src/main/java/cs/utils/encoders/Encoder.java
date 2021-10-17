package cs.utils.encoders;

import java.util.HashMap;

public class Encoder  {
    int counter;
    HashMap<Integer, String> table;
    HashMap<String, Integer> reverseTable;
    
    public Encoder() {
        this.counter = -1;
        this.table = new HashMap<>();
        this.reverseTable = new HashMap<>();
    }
    
    public int encode(String val) {
        if (reverseTable.containsKey(val)) {
            return reverseTable.get(val);
        } else {
            this.counter++;
            table.put(counter, val);
            reverseTable.put(val, counter);
            return counter;
        }
    }
    
    
    public HashMap<Integer, String> getTable() {
        return table;
    }
    
    public String decode(int val) {
        return this.table.get(val);
    }
    
    public HashMap<String, Integer> getRevTable() {
        return reverseTable;
    }
}
