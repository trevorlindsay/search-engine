import java.util.ArrayList;
import java.util.List;

/**
 * Stores information about where a word occurs in
 * a particular document.
 */
public class Posting {

    public String id;
    public List<Integer> locs;

    public Posting() {
        locs = new ArrayList<>();
    }

    public Posting(String id) {
        this.id = id;
        this.locs = new ArrayList<>();
    }
}

