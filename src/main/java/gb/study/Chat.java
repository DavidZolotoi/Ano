package gb.study;

import java.sql.Timestamp;
import java.util.LinkedHashMap;

public class Chat {
    private String tableName;
    private LinkedHashMap<Integer, Message> messages;
    public LinkedHashMap<Integer, Message> getMessages() {
        return messages;
    }
    private Integer idLastMessage;

    public Chat(String tableName, AnoWindow anoWindow) {
        this.tableName = tableName;
        this.messages = new LinkedHashMap<>();
        this.idLastMessage = 0;
    }

    /**
     * Загрузить в чат и на окно последние messageCount сообщений для чата,
     * соответствующему записи о диалоге chatListRow.
     * @param chatListRow запись о диалоге
     * @param messageCount количество загружаемых сообщений
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с db и tabChatPanel, которая необходима для работы метода
     */
    protected void downloadLastMessages(ChatListRow chatListRow, Integer messageCount, AnoWindow anoWindow){
        for (var message : anoWindow.getDb().selectLastMessages(chatListRow, messageCount)) {
            Integer newMessageId = (Integer) message.get(0);
            if (newMessageId <= this.idLastMessage)
                continue;   // не перерисовывать сообщение, которое уже нарисовано
            Message newMessage = new Message(
                    (Integer) message.get(0),
                    (Integer) message.get(1),
                    (String) message.get(2),
                    (Timestamp) message.get(3),
                    (String) message.get(4)
            );
            this.idLastMessage = newMessageId;
            setNewMessage(newMessage, anoWindow);
        }
    }

    /**
     * Добавляет сообщение в чат и запускает метод добавления сообщения на окно
     * @param message сообщение
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с tabChatPanel, которая необходима для работы метода
     */
    protected void setNewMessage(Message message, AnoWindow anoWindow){
        messages.put(message.getId(), message);
        //Добавить JTextArea с загруженным сообщением
        anoWindow.tabChatPanel.addAndShowNewMessage(message);
    }
}