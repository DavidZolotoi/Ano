package gb.study;

public class Message {

    //todo это должен быть User (но тут вопрос - с паролем или без? может подтереть?)
    String author;
    String text;

    public boolean isMarginRight;

    //todo полностью переделать структуру в соответствии с используемой базой
    public Message(String author, String text) {
        this.author = author;
        this.text = text;
        this.isMarginRight = !author.equals("Comp");//todo переделать определение автора (индикатора отступа)
    }

    public String show() {
        return "author: " + author + System.lineSeparator() + "text: " + text;
    }
}
