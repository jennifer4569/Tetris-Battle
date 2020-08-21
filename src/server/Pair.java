package src.server;

/** 
 * <b>Pair</b> is a representation of a tuple with two items.
 * @author Michael Ruvinshteyn
 * @version 1.0
 */
public class Pair<K, V> {

    /** Key and Value of the Pair */
    private final K key;
    private final V value;

    /** 
     * Creates a new instance of the Pair class
     * @param key The key of the Pair
     * @param value The value of the Pair
     */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /** 
     * Gets the key of the Pair
     * @return The key of the Pair
     */
    public K getKey() {
        return key;
    }

    /** 
     * Gets the value of the Pair
     * @return The value of the Pair
     */
    public V getValue() {
        return value;
    }

    /** 
     * Compares this with Object o
     * @param o The object to compare this to
     * @return True if Object o is a Pair with the same key and value, 
     * false otherwise
     */
    public boolean equals(Object o) {
        if (!(o instanceof Pair))
            return false;
        Pair pairo = (Pair) o;
        return this.key.equals(pairo.getKey()) && this.value.equals(pairo.getValue());
    }

    /** 
     * toString() function for the Pair class
     * @return The String representation of this
     */
    public String toString() {
        return key + "," + value;
    }
}