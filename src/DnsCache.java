import java.io.*;
import java.util.*;


public class DnsCache {

    private Map<String, List<String>> cache;
    private final String cacheFile;

    public DnsCache(String cacheFile){
        this.cacheFile = cacheFile;
        load();
    }

    public void load() {
        cache = new Hashtable<String, List<String>>();

        try (BufferedReader reader = new BufferedReader(new FileReader(cacheFile))) {

            String line;
            while((line = reader.readLine()) != null) {
                final String[] exploded = line.split(" ");
                put(exploded[0], exploded[1]);
            }
        }
        catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    public List<String> get(String key){
        return cache.get(key);
    }

    public boolean containsKey(String key){
        return cache.containsKey(key);
    }

    public void put(String key, String value){
        List<String> values = cache.get(key);
        if(values == null)
            cache.put(key, values = new ArrayList<String>());

        values.add(value);
    }



    public void save(){

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cacheFile, false));) {
            for (String key:cache.keySet()) {
                for(String value: cache.get(key)){
                    writer.write(String.format("%s %s\n", key, value));
                }
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }




}
