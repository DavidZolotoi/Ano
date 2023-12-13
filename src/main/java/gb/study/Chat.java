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
        log.info("Chat(..) Конец - чат с хранилищем messages создан, но сообщения пока без клика не загружены: " + tableName);
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
        // 1. Загрузить и добавить сообщения в словарь
        for (var message : anoWindow.getDb().selectLastMessages(chatListRow, messageCount)) {
            Integer newMessageId = (Integer) message.get(0);
            if (messages.containsKey(newMessageId)){
                continue;   // сохранять только то, чего нет в словаре
            }               // Проверка не лишняя, потому что создание сообщения ниже может содержать запрос в БД
            Message newMessage = new Message(
                    (Integer) message.get(0),
                    (Integer) message.get(1),
                    (String) message.get(2),
                    (Timestamp) message.get(3),
                    (String) message.get(4)
            );
            messages.put(newMessage.getId(), newMessage);
        }
        // 2. Добавить сообщения на экран
        // todo может быть стоит ограничить по количеству или вынести в асинхронный метод (если много)
        if (chatListRow.getId() == anoWindow.getUser().getActiveChatListRow().getId()){
            anoWindow.tabChatPanel.addAndShowMessagesFromList(new ArrayList<>(messages.values()));
        }
    }
}
