package gb.study;

import javax.swing.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;

public class Message {
    private AnoWindow anoWindow;   //БД в его свойстве
    private Log log;

    private Integer id;
    public Integer getId() {
        return id;
    }

    private Integer authorId;
    public Integer getAuthorId() {
        return authorId;
    }

    private String content;
    public String getContent() {
        return content;
    }

    private Timestamp datetime;     //внутри UTC
    public Timestamp getDatetime() {
        return datetime;
    }

    private String comment;
    public String getComment() {
        return comment;
    }

    public boolean isMarginRight;   //todo заменить на приват

    /**
     * Конструктор сообщения
     * @param id сообщения
     * @param authorId id автора
     * @param content содержимое сообщения
     * @param datetime время отправки
     * @param comment комментарий к сообщению
     */
    public Message(
            Integer id,             //todo сделать версию конструктора с получением id из БД
            Integer authorId,
            String content,
            Timestamp datetime,     //todo сделать версию конструктора с приемом времени
            String comment,
            JFrame window
    ){
        //todo хоть все проверки и уже сделаны "до", но вероятно стоит дописать их и здесь,
        // ведь мало ли как в будущем изменится код. Лишняя проверка - не лишняя
        this.anoWindow = (AnoWindow)window;
        this.log = this.anoWindow.log;
        log.info("Message(..) Начало");
        this.id = id;
        this.authorId = authorId;
        this.content = content;
        this.datetime = datetime; //Timestamp.from(Instant.now());
        this.comment = comment;
        this.isMarginRight = !authorId.equals(anoWindow.getUser().getId());
        log.info("Message(..) Конец - сообщение создано");
    }

    /**
     * Конструктор отправки нового сообщения
     * @param authorId id автора
     * @param content содержимое сообщения
     * @param comment комментарий к сообщению
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с БД, которая необходима для работы метода
     */
    public Message(
            Integer authorId,
            String content,
            String comment,
            ChatListRow chatListRow,
            AnoWindow anoWindow
    ){
        this.authorId = authorId;
        this.content = content;
        this.datetime = Timestamp.from(Instant.now());
        this.comment = comment;
        this.isMarginRight = !authorId.equals(anoWindow.getUser().getId());
        // отправить сообщение в БД
        sendToDB(this, chatListRow, anoWindow);
    }

    private void sendToDB(Message message, ChatListRow chatListRow, AnoWindow anoWindow) {
        // 1. Отправить новое сообщение в БД => присвоится id
        // 2. => сработает прослушивание и скачаются данные сообщения, в том числе и id
        // Т.е. здесь специально качать из базы id не нужно
        anoWindow.getDb().sendNewMessage(message, chatListRow);
    }

    /**
     * Показать содержимое сообщения
     * @return содержимое сообщения
     */
    public String show() {
        log.info("show() - Начало и конец - далее return содержимого сообщения");
        return content;
    }
}
