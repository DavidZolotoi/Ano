package gb.study;

import java.sql.Timestamp;
import java.util.LinkedHashMap;

public class Chat {
    //todo добавить в User Chat
    private String tableName;
    private LinkedHashMap<Integer, Message> Message;

    public Chat(String tableName, AnoWindow anoWindow) {
        this.tableName = tableName;
        this.Message = new LinkedHashMap<>();
        //todo загрузку сообщений сделать отдельным методом,
        // чтоб не грузить базу если диалог не открыт
        // переселить в этот класс метод downloadAndPasteLastMessagesToHistoryPanel()
        // и переименовать, его задача - выдать словарь сообщений
    }
    //..методы добавления, удаления и т.п. - управление хранилищем

    protected void downloadLastMessages(User interlocutor, Integer messageCount, AnoWindow anoWindow){
        // для юзера anoWindow.getUser(),
        // выбрать из коллекции чатов собеседника Interlocutor
        // и загрузить count сообщений запросом в нужную таблицу
        for (Message message : anoWindow.getDb().getLastMessages(messageCount)) {
            //todo Message.put();
        }
    }
}
