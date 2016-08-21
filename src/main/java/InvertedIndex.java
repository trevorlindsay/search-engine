import java.io.*;
import java.util.*;
import java.util.regex.*;
import edu.stanford.nlp.simple.*;
import org.apache.commons.lang3.math.NumberUtils;


/**
 * Creates and stores an inverted index
 * Includes method to search for words in index
 */
public class InvertedIndex {

    private HashMap<String, List<Posting>> index;
    private HashSet<String> books;
    private String DIRECTORY = "data";

    /**
     * Initializes an empty index
     */
    public InvertedIndex() {
        this.index = new HashMap<>();
        this.books = new HashSet<>();
    }

    /**
     * Initializes an index from a file
     * @param filename Path to file containing pre-created indewx
     */
    public InvertedIndex(String filename) {
        this.index = new HashMap<>();
        this.books = new HashSet<>();
        this.fromFile(filename);
    }

    public static void main(String[] args) {
        InvertedIndex i = new InvertedIndex("index/inverted_index.txt");
        HashMap<String, HashMap<Integer, Integer>> results = i.search("cat, dog");
        for (String ngram : results.keySet()) {
            for (int year : results.get(ngram).keySet()) {
                System.out.println(ngram + "\t" + year + "\t" + results.get(ngram).get(year));
            }
        }
    }

    /**
     * Get list of postings for word
     * @param token word to search for in index
     * @return list of postings for word (each posting has document ID and where word appears in document
     */
    public List<Posting> get(String token) {
        return index.get(token);
    }

    /**
     * Get a list of documents used to build the index
     * @return list of files that were used to create index
     */
    public HashSet<String> getBooks() {
        return books;
    }

    /**
     * Get words in index
     * @return set containing the words in the index
     */
    public Set<String> getTokens() {
        return index.keySet();
    }

    protected HashMap<String, List<Posting>> getIndex() {
        return this.index;
    }

    /**
     * Get teh list of files available to build an index from
     * @return list of files in DIRECTORY
     */
    private List<String> getPaths() {

        List<String> files = new ArrayList<>();

        File dir = new File(DIRECTORY);
        String[] subdirs = dir.list();
        File file;

        if (subdirs == null) {
            throw new NullPointerException("There are no folders/files in the directory!");
        }

        for (String subdir : subdirs) {
            if ((file = new File(DIRECTORY + "/" + subdir)).isDirectory()) {
                if (file.list() != null) {
                    for (String f : file.list()) {
                        files.add(DIRECTORY + "/" + subdir + "/" + f);
                    }
                }
            }
        }
        return files;
    }

    /**
     * Merge the another InvertedIndex with this index
     * @param mergeIndex InvertedIndex to merge with
     */
    public void merge(HashMap<String, Posting> mergeIndex) {

        Set<String> tokens = mergeIndex.keySet();
        for (String token : tokens) {

            if (index.containsKey(token)) {
                index.get(token).add(mergeIndex.get(token));
            } else {
                index.put(token, Collections.singletonList(mergeIndex.get(token)));
                for (Posting posting : index.get(token)) {
                    books.add(posting.id);
                }
            }
        }
    }

    /**
     * Update the list of books used to create this index
     */
    public void updateBooks() {

        for (List<Posting> posting : index.values()) {
            for (Posting post : posting) {
                books.add(post.id);
            }
        }
    }

    /**
     * Build an index for every file in the DIRECTORY and merge them all
     * together into one index
     */
    public void buildAll() {

        List<String> files = getPaths();

        for (String file : files) {
            System.out.print(file);
            this.build(file);
        }

        System.out.println("Tokens in main index: " + this.getTokens().size());
        this.toFile("index/inverted_index.txt");
        System.out.println("Main index written to file.");
    }

    /**
     * Build the index for a file in the DIRECTORY and merge with main index
     * @param pathname path to file used to build index
     */
    public void build(String pathname) {

        String bookId = pathname.substring(DIRECTORY.length() + 1, pathname.length() - 4);
        HashMap<String, Posting> map = new HashMap<>();
        String line = null;
        int pos = 0;

        try {

            FileReader fileReader = new FileReader(pathname);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {

                try {

                    Sentence tokens = new Sentence(line.toLowerCase());
                    for (String token : tokens.lemmas()) {

                        if (!map.containsKey(token) && !token.contains("|")) {
                            Posting posting = new Posting(bookId);
                            posting.locs.add(pos);
                            map.put(token, posting);
                        } else if (map.containsKey(token) && !token.contains("|")) {
                            map.get(token).locs.add(pos);
                        }

                        pos++;
                    }

                } catch (IllegalStateException ignored) {}
            }
            bufferedReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(pathname + "\t" + map.keySet().size());
        this.merge(map);
    }

    public void toFile(String filename) {

        try {

            FileWriter fileWriter = new FileWriter(filename);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            for (String token : this.getTokens()) {

                String line = token + "||";
                List<Posting> postings = index.get(token);
                StringJoiner bookJoiner = new StringJoiner(";");

                for (Posting posting : postings) {
                    StringJoiner postJoiner = new StringJoiner(",");
                    for (int pos : posting.locs) {
                        postJoiner.add(Integer.toString(pos));
                    }
                    String post = posting.id + ":" + postJoiner.toString();
                    bookJoiner.add(post);
                }

                line += bookJoiner.toString();
                bufferedWriter.write(line + "\n");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fromFile(String filename) {

        try {

            if (!index.isEmpty()) {
                throw new IllegalArgumentException("The index has already been created!");
            }

            FileReader fileReader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line = null;
            while ((line = bufferedReader.readLine()) != null) {

                if (line.isEmpty()) {
                    continue;
                }

                String[] splitLine = line.split(Pattern.quote("||"));
                String token = splitLine[0];
                String[] posts = splitLine[1].split(";");
                List<Posting> postings = new ArrayList<>();

                for (int i = 0; i < posts.length; i++) {

                    String post = posts[i];
                    String[] splitPost = post.split(":");
                    String id = splitPost[0];
                    String[] locStr = splitPost[1].split(",");
                    List<Integer> locs = new ArrayList<>(locStr.length);

                    for (int j = 0; j < locStr.length; j++) {
                        locs.add(Integer.parseInt(locStr[j]));
                    }

                    Posting posting = new Posting();
                    posting.id = id;
                    posting.locs.addAll(locs);
                    postings.add(posting);
                }
                index.put(token, postings);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, HashMap<Integer, Integer>> search(String query) {

        if (query.equals("")) {
            return null;
        }

        HashMap<String, HashMap<Integer, Integer>> results = new HashMap<>();
        String[] ngrams = query.toLowerCase().replace(", ", ",").split(",");
        File dir = new File(DIRECTORY);
        final String[] years = dir.list();

        for (int i = 0; i < ngrams.length; i++) {

            HashMap<Integer, Integer> counts = new HashMap<>(years.length);
            for (String year : years) {
                if (NumberUtils.isNumber(year)) {
                    counts.put(Integer.parseInt(year), 0);
                }
            }

            String ngram = ngrams[i];
            List<Set<String>> books = new ArrayList<>();
            List<String> lemmas = new Sentence(ngram).lemmas();

            for (String word : lemmas) {

                Set<String> booksWithWord = new HashSet<>();

                if (index.containsKey(word)) {
                    for (Posting posting : index.get(word)) {
                        booksWithWord.add(posting.id);
                    }
                }
                books.add(booksWithWord);
            }

            Set<String> booksWithAllWords = new HashSet<>();

            if (!books.isEmpty()) {
                booksWithAllWords.addAll(books.remove(0));
                for (Set<String> bookSet : books) {
                    booksWithAllWords.retainAll(bookSet);
                }
            }

            for (String bookId : booksWithAllWords) {

                int year = Integer.parseInt(bookId.split("/")[0]);
                List<Set<Integer>> locs = new ArrayList<>();

                for (String word : lemmas) {
                    Set<Integer> wordLocs = new HashSet<>();
                    for (Posting posting : index.get(word)) {
                        if (posting.id.equals(bookId)) {
                            wordLocs.addAll(posting.locs);
                        }
                    }
                    locs.add(wordLocs);
                }

                int k = 0;
                for (int l = 0; l < locs.size(); l++) {
                    Set<Integer> wordLocs = locs.get(l);
                    Set<Integer> newWordLocs = new HashSet<>();
                    for (int pos : wordLocs) {
                        newWordLocs.add(pos - k);
                    }
                    k++;
                    locs.set(l, newWordLocs);
                }

                Set<Integer> intersection = new HashSet<>(locs.remove(0));
                for (Set<Integer> wordLocs : locs) {
                    intersection.retainAll(wordLocs);
                }
                int new_count = counts.get(year) + intersection.size();
                counts.put(year, new_count);
            }
            results.put(ngram, counts);
        }
        return results;
    }
}