package fiitstu.gulis.cmsimulator.elements;

public class TestWord {

    private String word;
    private Integer accepted;
    private Boolean result;

    public TestWord(){
        this.word = null;
        this.accepted = 0;
        this.result = null;
    }

    public TestWord(String word, int accepted, Boolean result){
        this.word = word;
        this.accepted = accepted;
        this.result = result;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int isAccepted() {
        return accepted;
    }

    public void setAccepted(int accepted) {
        this.accepted = accepted;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

}
