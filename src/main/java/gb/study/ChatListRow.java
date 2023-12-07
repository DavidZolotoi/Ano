package gb.study;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ChatListRow {
    private Integer id;    //заполняется при добавлении в БД, выгрузкой из неё присвоенного id
    protected Integer getId() {
        return id;
    }

    private Integer userIdMin;
    protected Integer getUserIdMin() {
        return userIdMin;
    }

    private Integer userIdMax;
    protected Integer getUserIdMax() {
        return userIdMax;
    }

    private String tableName;
    protected String getTableName() {
        return tableName;
    }

    private String comment;
    protected String getComment() {
        return comment;
    }

    enum NAME {SCHEMA, TABLE, TRIGGER, FUNCTION, NOTIFY}
    private LinkedHashMap<NAME, String> nameFromDB;
    public LinkedHashMap<NAME, String> getNameFromDB() {
        return nameFromDB;
    }

    public ChatListRow(Integer user1, Integer user2, String comment, AnoWindow anoWindow) {
        this.userIdMin = Integer.min(user1, user2);
        this.userIdMax = Integer.max(user1, user2);
        this.nameFromDB = createNames(this.userIdMin, this.userIdMax);
        this.tableName = this.nameFromDB.get(NAME.TABLE);
        this.comment = comment;
        // получить из БД присвоенный id, добавив данные в БД, если их там нет
        this.id = getParseIdFromBD(anoWindow);
    }
    /**
     * Создает словарь имён - некая договоренность о том, как должны называться элементы словаря
     * @param usIdMin меньший по значению id собеседника
     * @param usIdMax больший по значению id собеседника
     * @return готовый словарь имён (схемы, таблицы, триггера, функции, уведомления)
     */
    private LinkedHashMap<NAME, String> createNames(Integer usIdMin, Integer usIdMax) {
        LinkedHashMap<NAME, String> names = new LinkedHashMap<>();
        names.put(NAME.SCHEMA, "public");
        String tableNameWithoutScheme = "zz" + usIdMin + "yy" + usIdMax;
        names.put(NAME.TABLE, names.get(NAME.SCHEMA) + "." + tableNameWithoutScheme);
        names.put(NAME.TRIGGER, "t" + tableNameWithoutScheme);
        names.put(NAME.FUNCTION, "public.f" + tableNameWithoutScheme + "()");
        names.put(NAME.NOTIFY, "n" + tableNameWithoutScheme);
        return names;
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
        // 4) добавление отдельной функции для создания уведомлений для прослушивания + обновление словаря имен
        anoWindow.getDb().createFunctionNotifyForNewMessage(this);
        // 5) добавление к таблице триггера, вызывающей функцию уведомлений + использование и обновление сл.имен
        anoWindow.getDb().createTriggerForExecuteProcedure(this);
    }


}
