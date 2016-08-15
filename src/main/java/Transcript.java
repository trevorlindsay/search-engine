public class Transcript {

    private String name;
    private String date;
    private String ticker;
    private String text;

    Transcript() {}

    void setName(String name) {
        this.name = name;
    }

    void setDate(String date) {
        this.date = date;
    }

    void setTicker(String ticker) {
        this.ticker = ticker;
    }

    void setText(String text) {
        this.text = text;
    }

    String getName() {
        return name;
    }

    String getDate() {
        return date;
    }

    String getTicker() {
        return ticker;
    }

    String getText() {
        return text;
    }

}
