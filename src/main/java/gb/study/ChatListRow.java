package gb.study;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ChatListRow {
    private AnoWindow anoWindow;   //БД в его свойстве
    private Log log;

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

    public ChatListRow() {}

    /**
     * Создает объект записи о диалоге ChatListRow, выгрузив его характеристики из БД.
     * Если в БД такой строки нет, то создает её.
     * @param idUser1 id одного пользователя (неважно какого: большего или меньшего)
     * @param idUser2 id другого пользователя (неважно какого: большего или меньшего)
     * @param comment комментарий к записи о диалоге
     * @param window главное окно, содержит логгер и прочее
     */
    public ChatListRow(Integer idUser1, Integer idUser2, String comment, JFrame window) {
        this.anoWindow = (AnoWindow)window;
        this.log = this.anoWindow.log;
        log.info("ChatListRow(..) Начало");
        this.userIdMin = Integer.min(idUser1, idUser2);
        this.userIdMax = Integer.max(idUser1, idUser2);
        this.nameFromDB = createMapNamesForChatListRow(this.userIdMin, this.userIdMax);
        this.tableName = this.nameFromDB.get(NAME.TABLE);
        this.comment = comment;
        // получить из БД присвоенный id, добавив данные в БД, если их там нет
        this.id = parseIdFromBD(getIdFromDB());
        log.info("ChatListRow(..) Конец - запись о диалоге создана");
    }
    /**
     * Создает словарь имён - некая договоренность о том, как должны называться элементы для конкретной записи о диалоге
     * @param usIdMin меньший по значению id собеседника
     * @param usIdMax больший по значению id собеседника
     * @return готовый словарь имён (схемы, таблицы, триггера, функции, уведомления)
     */
    private LinkedHashMap<NAME, String> createMapNamesForChatListRow(Integer usIdMin, Integer usIdMax) {
        log.info("createMapNames(..) Начало");
        LinkedHashMap<NAME, String> names = new LinkedHashMap<>();
        names.put(NAME.SCHEMA, "public");
        StringBuilder tableNameWithoutScheme = new StringBuilder("zz").append(usIdMin).append("yy").append(usIdMax);
        names.put(NAME.TABLE, new StringBuilder(names.get(NAME.SCHEMA)).append(".").append(tableNameWithoutScheme).toString());
        names.put(NAME.TRIGGER, new StringBuilder("t").append(tableNameWithoutScheme).toString());
        names.put(NAME.FUNCTION, new StringBuilder("public.f").append(tableNameWithoutScheme).append("()").toString());
        names.put(NAME.NOTIFY, new StringBuilder("n").append(tableNameWithoutScheme).toString());
        log.info("createMapNames(..) Конец - словарь имен создан");
        return names;
    }

    /**
     * Получает из БД id для записи о диалоге.
     * @return id для записи о диалоге - таблица Object
     */
    private ArrayList<ArrayList<Object>> getIdFromDB(){
        log.info("getIdFromDB() Начало");
        ArrayList<ArrayList<Object>> idForChatListRow = anoWindow.getDb().selectIdForChatListRow(this);
        log.info("getIdFromDB() Конец - присвоен ли id для записи о диалоге " + this.tableName + "? " + (idForChatListRow!=null));
        return idForChatListRow;
    }
    /**
     * Метод, получающий из БД id, присвоенный для chatListRow,
     * обработав результаты выгрузки из БД и приведя их к нужному типу.
     * Если такой записи о диалоге нет, то создаст ее, настроит и снова запросит и распознает id.
     * @return id, выгруженный из БД и приведенный к нужному типу
     */
    private Integer parseIdFromBD(ArrayList<ArrayList<Object>> chatListId){
        log.info("parseIdFromBD(..) Начало");
        if (chatListId == null || chatListId.isEmpty()){
            log.warning("id ChatListRow не распознан при выгрузке из БД - массив пуст" +
                    " => будет создана новая строка в БД и по новой распознана."
            );
            createNewTableForChatAndConfigure();
            chatListId = getIdFromDB();
        }
        Object chatListRowIdObj = chatListId.get(0).get(0);
        Integer chatListRowId = ((Number) chatListRowIdObj).intValue();
        log.info("parseIdFromBD(..) Конец: id записи: " + chatListRowId);
        return chatListRowId;
    }
    /**
     * Метод, который создаст новый диалог и загрузит его в БД, также настроит всё необходимое:
     * - добавит такую запись в chatlist (появится id диалога о записи);
     * - добавит в БД таблицу диалога, соответствующую записи из chatlist;
     * - добавит внешний ключ к id автора сообщения;
     * - добавит функцию для создания уведомлений о новых сообщениях;
     * - добавит триггер к таблице, вызывающий функцию создания уведомлений.
     */
    public void createNewTableForChatAndConfigure(){
        log.info("createNewTableForChatAndConfigure() Начало");
        anoWindow.getDb().insertNewChatListRow(this);
        anoWindow.getDb().createNewTableForChat(this);
        anoWindow.getDb().addForeignKeyForChat(this);
        anoWindow.getDb().createFunctionNotifyForNewMessage(this);
        anoWindow.getDb().createTriggerForExecuteProcedure(this);
        log.info("createNewTableForChatAndConfigure() Конец - в БД добавлена новая запись о диалоге, создан чат с внешним ключом, функцией и триггером.");
    }


}
