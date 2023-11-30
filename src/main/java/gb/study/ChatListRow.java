package gb.study;

import java.util.ArrayList;

public class ChatListRow {
    private Integer id;    //заполняется при добавлении в БД, выгрузкой из неё присвоенного id
    private Integer userIdMin;
    private Integer userIdMax;
    private String tableName;
    private String comment;

    protected Integer getId() {
        return id;
    }

    protected Integer getUserIdMin() {
        return userIdMin;
    }

    protected Integer getUserIdMax() {
        return userIdMax;
    }

    protected String getTableName() {
        return tableName;
    }

    protected String getComment() {
        return comment;
    }

    public ChatListRow() {
    }

    public ChatListRow(Integer user1, Integer user2, String comment, AnoWindow anoWindow) {
        this.userIdMin = Integer.min(user1, user2);
        this.userIdMax = Integer.max(user1, user2);
        this.tableName = "public.zz" + this.userIdMin + "yy" + this.userIdMax;
        this.comment = comment;
        // получить из БД присвоенный id, добавив данные в БД, если их там нет
        this.id = getParseIdFromBD(anoWindow);
    }
    /**
     * Метод, получающий из БД id, присвоенный для chatListRow,
     * обработав результаты выгрузки из БД и приведя их к нужному типу
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с БД, которая необходима для работы метода
     * @return id, выгруженный из БД и приведенный к нужному типу
     */
    private Integer getParseIdFromBD(AnoWindow anoWindow){
        // выгрузка из БД - это всегда таблица объектов
        ArrayList<ArrayList<Object>> chatListId = getIdFromDB(anoWindow);
        return (Integer) chatListId.get(0).get(0);
    }
    /**
     * Метод, получающий из БД id, присвоенный для chatListRow.
     * Если необходимой записи о диалоге и соответствующей таблицы не существует, то:
     * - создаст и загрузит В БД новую запись (появится id), а также настроит всё необходимое для нового диалога
     * - выгрузит из БД отчет с присвоенным id
     * Все полученные данные - таблица Object
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с БД, которая необходима для работы метода
     * @return id, присвоенный в БД, для chatListRow
     */
    private ArrayList<ArrayList<Object>> getIdFromDB(AnoWindow anoWindow){
        ArrayList<ArrayList<Object>> reportWithId = anoWindow.getDb().selectIdForChatListRow(this);
        //Если такого диалога в БД нет, то создать и настроить
        if(reportWithId.isEmpty()) {
            createNewTableForChatAndConfigure(anoWindow);
            // снова запросить id в таблице ChatListRow
            reportWithId = anoWindow.getDb().selectIdForChatListRow(this);
        }
        return reportWithId;
    }
    /**
     * Метод, который создаст новый диалог и загрузит его в БД, также настроит всё необходимое:
     * - добавит такую запись в chat_list (появится id диалога о записи);
     * - добавит в БД таблицу диалога, соответствующую записи из chat_list;
     * - добавит внешние ключи (к user);
     * - добавит функцию для создания уведомлений для прослушивания;
     * - добавит триггер к таблице, вызывающий функцию уведомлений при вставке новой записи в таблицы.
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с БД, которая необходима для работы метода
     */
    public void createNewTableForChatAndConfigure(AnoWindow anoWindow){
        // 1) добавить такую запись о диалоге в таблицу ChatListRow
        anoWindow.getDb().insertNewChatListRow(this);
        // 2) добавить такую таблицу для диалога в БД
        anoWindow.getDb().createNewTableForChat(this);
        // 3) добавление внешних ключей к таблице диалога
        anoWindow.getDb().addForeignKeyForChat(this);
        // 4) добавление отдельной функции для создания уведомлений для прослушивания
        anoWindow.getDb().createFunctionNotifyForNewMessage(this);
        // 5) добавление к таблице триггера, вызывающей функцию уведомлений
        anoWindow.getDb().createTriggerForExecuteProcedure(this);
    }


}
