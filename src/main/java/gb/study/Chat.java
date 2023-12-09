package gb.study;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Chat {
    private String tableName;
    private LinkedHashMap<Integer, Message> messages;
    public LinkedHashMap<Integer, Message> getMessages() {
        return messages;
    }

    public Chat(String tableName, AnoWindow anoWindow) {
        this.tableName = tableName;
        this.messages = new LinkedHashMap<>();  //пока не грузим из БД - ждем клика
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
        // 1. Згрузить и добавить сообщения в словарь
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
