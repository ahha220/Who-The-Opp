import java.util.Comparator;
/**
 * This class called SortItemsByImportance implements Comparator interface. It sorts items by their level of importance in the game because that the higher importance (because a lower level number) comes first.
 */
public class SortItemsByImportance implements Comparator<Item> {
    /**
     * Method Description: Compares two items based on their level of importance to the case.
     * 
     * Parameters: Item item1 --> the first item to compare
     *			   Item item2 --> the second item to compare
     * Return Type: int --> a negative integer, zero, or a positive integer as the first item is less than, equal to, or greater than the second
     */
    public int compare(Item item1, Item item2) {
        return Integer.compare(item1.getLevelOfImportance(), item2.getLevelOfImportance());
    }
}
