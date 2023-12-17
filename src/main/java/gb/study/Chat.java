package gb.study;

import javax.swing.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Chat {
    private AnoWindow anoWindow;   //БД в его свойстве
    private Log log;

    private String tableName;
    protected String getTableName() {
        return tableName;
    }

    private LinkedHashMap<Integer, Message> messages;
    protected LinkedHashMap<Integer, Message> getMessages() {
        return messages;
    }

    /**
     * Создает чат с хранилищем messages, но для его заполнения используется отдельный метод
     * @param tableName Имя чата - соответствует наименованию таблицы в БД
     * @param window главное окно, содержит логгер и прочее
     */
    public Chat(String tableName, JFrame window) {
        this.anoWindow = (AnoWindow)window;
        this.log = this.anoWindow.log;
        log.info("Chat(..) Начало");
        this.tableName = tableName;
        this.messages = new LinkedHashMap<>();
        log.info("Chat(..) Конец - чат с хранилищем messages создан, но сообщения пока без клика не загружены: ", tableName);
    }

    /**
     * Метод, получающий из БД определенное количество последних сообщений
     * @param chatListRow запись о диалоге
     * @param messageCount количество загружаемых сообщений
     * @return список последних сообщений - таблица Object
     */
    private ArrayList<ArrayList<Object>> getLastMessagesFromDB(ChatListRow chatListRow, Integer messageCount) {
        log.info("getLastMessagesFromDB(..) Начало");
        ArrayList<ArrayList<Object>> lastMessagesFromDB = anoWindow.getDb().selectLastMessages(chatListRow, messageCount);
        log.info("getLastMessagesFromDB(..) Конец - сообщения есть?", ((Boolean)(lastMessagesFromDB!=null)).toString());
        return lastMessagesFromDB;
    }
    /**
     * Загрузить и распознать в чат и на окно последние messageCount сообщений для чата,
     * соответствующему записи о диалоге chatListRow.
     * @param chatListRow запись о диалоге
     * @param messageCount количество загружаемых сообщений
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с db и tabChatPanel, которая необходима для работы метода
     */
    protected void parseLastMessages(ChatListRow chatListRow, Integer messageCount, AnoWindow anoWindow) {
        log.info("parseLastMessages(..) Начало - парс словаря сообщений ''id -> Message''");
        ArrayList<ArrayList<Object>> lastMessagesFromDB = getLastMessagesFromDB(chatListRow, messageCount);
        if(lastMessagesFromDB == null) return;

        for (var messageRow : lastMessagesFromDB) {
            if (messageRow == null) continue;
            //todo вероятно здесь и в остальных местах обработки результатов нужно использовать интерфейс парсера-конвертера
            // но проверка на null вероятно должна здесь остаться
            Integer messageId = (messageRow.get(0) != null) ? ((Number) messageRow.get(0)).intValue() : null;
            if (messages.containsKey(messageId)){
                continue;   //если сообщение уже есть, то не надо создавать новое сообщение
            }
            Integer messageAuthorId = (messageRow.get(1) != null) ? ((Number) messageRow.get(1)).intValue() : null;
            String messageContent = (messageRow.get(2) != null) ? messageRow.get(2).toString() : null;
            Timestamp messageDatetime = (messageRow.get(3) != null) ? (Timestamp) messageRow.get(3) : null;
            String messageComment = (messageRow.get(4) != null) ? messageRow.get(4).toString() : null;
            Message newMessage = new Message(messageId, messageAuthorId, messageContent, messageDatetime, messageComment, anoWindow);
            messages.put(newMessage.getId(), newMessage);
            log.info("В словарь ''id -> Message'' добавлено сообщение",
                    "с id =", newMessage.getId().toString());
        }
        log.info("parseLastMessages(..) Конец - парс словаря сообщений ''id -> Message''");
    }
}
