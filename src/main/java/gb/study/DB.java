package gb.study;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DB {
    //путь к файлу и словарь с данными из файла делаю статическим,
    // чтоб не перезаписывать эти данные при каждом подключении к БД
    protected static String settingsFilePath = "E:\\Csharp\\GB\\Ano\\Anoswing\\settings_past_the_git.json";
    protected static Map<String, String> settings;

    protected Connection connForSend;
    protected Statement stmtForSend;

    protected Connection connForListen;
    protected Statement stmtForListen;

    public DB() {
        //создать словарь, если его до сих пор нет,
        // добавить ключи, если их там до сих пор нет, прочитав из файла
        DB.settings = checkSettings(DB.settings, DB.settingsFilePath,
                "url", "user", "password", "table_name_for_user", "table_name_for_chat_list");

        //todo возможно стоит убрать из конструктора - надо подумать
        connForSend = getConnection(settings.get("url"), settings.get("user"), settings.get("password"));
        stmtForSend = getStatement(connForSend);

        connForListen = getConnection(settings.get("url"), settings.get("user"), settings.get("password"));
        stmtForListen = getStatement(connForListen);
    }

    /**
     * Проверить существование словаря настроек и ключей в нём.
     * Если словаря нет, то создать.
     * Если ключей в словаре нет, то добавить, прочитав их значения из файла.
     * @param mapForCheck словарь, который необходимо проверить и вернуть.
     * @param filePath путь к файлу, в котором хранятся ключи и значения для словаря
     * @param jsonKeys ключи, которые необходимо проверить или добавить
     * @return Проверенный и дополненный в случае необходимости словарь.
     */
    private Map<String, String> checkSettings(Map<String, String> mapForCheck, String filePath, String... jsonKeys) {
        if (mapForCheck == null) mapForCheck = new HashMap<>();
        for (var jsonKey:jsonKeys) {
            if (!mapForCheck.containsKey(jsonKey))
                mapForCheck.put(jsonKey, readJSONFile(filePath, jsonKey));
        }
        return mapForCheck;
    }
    /**
     * Метод, получающий значение по ключу из JSON-файла
     * @param filePath путь к файлу JSON,
     * @param jsonKey ключ, по которому необходимо найти значение
     * @return искомое значение
     */
    public String readJSONFile(String filePath, String jsonKey) {
        String jsonValue = null;
        try (FileReader reader = new FileReader(filePath))
        {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);
            jsonValue = jsonObject.getString(jsonKey);
        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден: " + filePath);
            e.printStackTrace();
        } catch (JSONException e) {
            System.out.println("Ошибка при чтении JSON. Ключ: " + jsonKey);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Проблема с вводом-выводом при чтении файла: " + filePath);
            e.printStackTrace();
        }
        return jsonValue;
    }

    /**
     * Метод, создающий Statement, используя готовое соединение
     * @param connection соединение
     * @return созданный Statement
     */
    private Statement getStatement(Connection connection) {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
        } catch (SQLException e) {
            System.out.println("НЕ ВЫПОЛНЕНО: Проблема с Statement");
        }
        return stmt;
    }

    /**
     * Метод, создающий подключение
     * @param url адрес БД
     * @param user логин БД
     * @param password пароль БД
     * @return соединение
     */
    protected Connection getConnection(String url, String user, String password){
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("НЕ ВЫПОЛНЕНО: Проблема с Class.forName(..)");
        }
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            System.out.println("НЕ ВЫПОЛНЕНО: Проблема с conn = DriverManager.getConnection(url, user, password)");
        }
        return conn;
    }


    /**
     * Метод, получающий из БД последние countMessagesDownloadAtStart сообщений из определенного чата
     * @param chatListRow запись о диалоге, содержащая название чата
     * @param countMessagesDownloadAtStart количество последних сообщений, которое необходимо получить из БД
     */
    public ArrayList<ArrayList<Object>> selectLastMessages(ChatListRow chatListRow, int countMessagesDownloadAtStart) {
        String downloadQuery =
                "SELECT * " +
                "FROM " +
                    "(SELECT * from " + chatListRow.getTableName() + " " +
                    "ORDER BY " + "zydatetime" + " DESC LIMIT " + countMessagesDownloadAtStart + ") " +
                "AS last_message_not_ordered " +
                "ORDER BY " + "zydatetime" + " ASC;";
        return executeQueryReport(downloadQuery);
    }

    /**
     * Отправляет сообщение в БД в таблицу, название которой, указанно в записи о диалоге
     * @param message сообщение
     * @param chatListRow запись о диалоге
     */
    public void sendNewMessage(Message message, ChatListRow chatListRow) {
        String queryForInsertNewMessage =
                "INSERT INTO " + chatListRow.getTableName() + " (" +
                        "zyauthorid, " +
                        "zycontent, " +
                        "zydatetime, " +
                        "zycomment" +
                        ") " +
                "VALUES (" +
                        message.getAuthorId() + ", " +
                        "'" + message.getContent() + "', " +
                        "'" + message.getDatetime() + "', " +
                        "'" + message.getComment() + "'" +
                        ");";
        try {
            stmtForSend.execute(queryForInsertNewMessage);
        } catch (SQLException e) {
            System.out.println(
                    "НЕ ВЫПОЛНЕНО: Проблема с stmtForSend.execute(insertQuery): \n" + e.getMessage() +
                            "\nВот код запроса:\n" + queryForInsertNewMessage
            );
        }
    }

    /**
     * Универсальный метод, выполняющий запрос в БД
     * устанавливает свое соединение с базой и выполняет запрос
     * @param query запрос, который необходимо выполнить
     */
    public void executeQueryVoid(String query) {
        Statement stmtForQuery = getStatement(
                getConnection(settings.get("url"), settings.get("user"), settings.get("password"))
        );
        try {
            stmtForQuery.execute(query);
        } catch (SQLException e) {
            System.out.println("НЕ ВЫПОЛНЕНО: Проблема с stmtForQuery.execute(insertQuery): \n" + e.getMessage());
        }
    }

    /**
     * Универсальный метод, выдающий отчет из базы данных ио запросу - таблица - коллекция коллекций
     * @param query запрос SQL
     * @return отчет из базы данных
     */
    public ArrayList<ArrayList<Object>> executeQueryReport(String query){
        ArrayList<ArrayList<Object>> resultReport = new ArrayList<>();
        try (
                Statement stmtForQuery = getStatement(
                        getConnection(settings.get("url"), settings.get("user"), settings.get("password"))
                );
                ResultSet newRow = stmtForQuery.executeQuery(query)
        ) {
            ResultSetMetaData metaData = newRow.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (newRow.next()) {
                ArrayList<Object> newRowReport = new ArrayList<>();
                for (int newColumn = 1; newColumn <= columnCount; newColumn++) {
                    String columnName = metaData.getColumnName(newColumn);
                    Object cellValue = newRow.getObject(newColumn);
                    newRowReport.add(cellValue);
                    System.out.println(columnName + ": " + cellValue);
                }
                resultReport.add(newRowReport);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultReport;
    }

    /**
     * Прослушивание диалога
     * @param anoWindow ссылка на окно,
     *                  в котором есть метод, вставляющий новые сообщения в свое место на окне
     */
    public void startListenerNewMessage(ArrayList<ChatListRow> chatListRows, AnoWindow anoWindow) {
        System.out.println("--- DB метод прослушки новых сообщений");
        // Суммарный запрос для прослушки, состоящий из нескольких Listen notify...;...
        StringBuilder queriesForListenNotify = new StringBuilder();
        for (var chatListRow : chatListRows) {
            String notifyName = chatListRow.getNameFromDB().get(ChatListRow.NAME.NOTIFY);
            queriesForListenNotify.append("LISTEN ").append(notifyName).append("; ");
        }

        // Выполнение запросов прослушки
        try {
            stmtForListen.execute(queriesForListenNotify.toString());
        } catch (SQLException e) {
            System.out.println("НЕ ВЫПОЛНЕНО: для прослушивания сообщений `stmtForListen.execute(queriesForListenNotify.toString());`");
        }

        //Приём уведомлений от всех каналов при данном подключении
        PGConnection pgConnForListen = (PGConnection)connForListen;
        while (true) {
            PGNotification[] newMessageNotifications = new PGNotification[0];
            try {
                newMessageNotifications = pgConnForListen.getNotifications();
            } catch (SQLException e) {
                System.out.println("НЕ ВЫПОЛНЕНО: `newMessageNotifications = pgConnForListen.getNotifications();`");
                throw new RuntimeException(e);
            }
            if (newMessageNotifications != null) {
                for (PGNotification newMessageNotification : newMessageNotifications) {
                    String[] parts = newMessageNotification.getParameter().split("\\|");
                    String tableName = parts[0];
                    System.out.println("***Уведомление о новом сообщении в " + tableName);
                    //todo по ходу нужно только название таблицы??? Тогда и структуру уведомления можно сократить
                    Integer id = Integer.parseInt(parts[1]);
                    Integer authorId = Integer.parseInt(parts[2]);
                    String content = parts[3];
                    Timestamp datetime =  Timestamp.valueOf(parts[4]);
                    String comment = parts[5];
                    // Определение записи о диалоге собеседника по наименованию таблицы, полученной из БД:
                    ChatListRow chatListRow = new ChatListRow();
                    for (var chatListRowItem : anoWindow.getUser().getDisputerIdsAndChatListRows().values()) {
                        System.out.println("Проверка: " + chatListRowItem.getTableName() + "и" + tableName);
                        if (chatListRowItem.getTableName().equals(tableName)) {
                            chatListRow = chatListRowItem;
                            System.out.println("***ЧатЛист № " + chatListRowItem.getId());
                            break;
                        }
                    }
                    Integer idDisputer = anoWindow.getUser().calculateDisputerId(chatListRow);
                    //1.2.1. Загрузка последних сообщений из БД в хранилище (конкретный чат из словаря) юзера
                    // в словарь добавляются только сообщения, которые еще не скачаны
                    anoWindow.getUser().getChats().get(idDisputer).downloadLastMessages(
                            chatListRow,
                            Integer.parseInt(anoWindow.tabSettingsPanel.getCountMesForDownValueTextArea().getText()),
                            anoWindow
                    );
                    //1.2.2. Добавить сообщения на экран
                    // todo может быть стоит ограничить по количеству или вынести в асинхронный метод (если много)
                    anoWindow.tabChatPanel.addAndShowMessagesFromList(
                            new ArrayList<>(anoWindow.getUser().getChats().get(idDisputer).getMessages().values())
                    );
                    audioNotification();
                }
            }

            try {
                Thread.sleep(1005);
            } catch (InterruptedException e) {
                // обработка ошибок
            }
        }
    }

    public void startListenerNewChatListRow(ArrayList<ChatListRow> chatListRows, AnoWindow anoWindow) {
        System.out.println("DB метод прослушки о новых записях  о диалогах");
        String notifyName = "ncl";
        String queryForListenNotify = "LISTEN " + notifyName + "; ";

        // Выполнение запроса прослушки
        try {
            stmtForListen.execute(queryForListenNotify.toString());
        } catch (SQLException e) {
            System.out.println("НЕ ВЫПОЛНЕНО: для прослушивания новых логинов `stmtForListen.execute(queryForListenNotify.toString());`");
        }

        //Приём уведомлений о всех новых диалогах при данном подключении
        PGConnection pgConnForListen = (PGConnection)connForListen;
        while (true) {
            PGNotification[] newChatListRowNotifications = new PGNotification[0];
            try {
                newChatListRowNotifications = pgConnForListen.getNotifications();
            } catch (SQLException e) {
                System.out.println("НЕ ВЫПОЛНЕНО: `newChatListRowNotifications = pgConnForListen.getNotifications();`");
                throw new RuntimeException(e);
            }
            if (newChatListRowNotifications != null) {
                for (PGNotification newChatListRowNotification : newChatListRowNotifications) {
                    String[] parts = newChatListRowNotification.getParameter().split("\\|");
                    Integer id = Integer.parseInt(parts[0]);
                    Integer userIdMin = Integer.parseInt(parts[1]);
                    Integer userIdMax = Integer.parseInt(parts[2]);
                    String tableName = parts[3];
                    String comment = parts[4];
                    if (anoWindow.getUser().getId() != userIdMin && anoWindow.getUser().getId() != userIdMax){
                        continue;   //пропустить если нас не касается
                    }
                    System.out.println("***Уведомление о новой записи о диалоге с название табл. " + tableName);
                    // todo добавить везде запись о диалоге собеседника, полученную из БД:
                    //anoWindow.getUser().disputersUpdate(anoWindow);
                    audioNotification();
                }
            }

            try {
                Thread.sleep(7003);
            } catch (InterruptedException e) {
                // обработка ошибок
            }
        }
    }
    
    /**
     * Воспроизведение звукового уведомления
     */
    public static void audioNotification() {
        String soundFilePath = "E:\\Csharp\\GB\\Ano\\Anoswing\\Ano\\src\\main\\resources\\sounds\\audioMes.wav";
        try {
            File soundFile = new File(soundFilePath);
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(soundFile));
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод, получающий id, login, password из таблицы user в БД.
     * @param user пользователь
     */
    public ArrayList<ArrayList<Object>> selectIdLoginPasswordForUser(User user) {
        String queryForGetIdLoginPassword =
                "SELECT usid, uslogin, uspassword FROM " + DB.settings.get("table_name_for_user") + " " +
                        "WHERE uslogin = '" + user.getLogin() + "'";
        return executeQueryReport(queryForGetIdLoginPassword);
    }

    /**
     * Метод, добавляющий нового пользователя (новую строку в таблице user)
     * @param user объект пользователя, которого необходимо добавить
     */
    public void insertNewUserAndConfigure(User user) {
        String queryForInsertNewUser =
                "INSERT INTO " + DB.settings.get("table_name_for_user") +
                        "(" +
                        "uslogin" + ", " +
                        "uspassword" + ", " +
                        "usfirstname" + ", " +
                        "uslastname" + ", " +
                        "usmail" + ", " +
                        "usphone" + ", " +
                        "uscomment" +
                        ")" +
                        "VALUES" +
                        "(" +
                        "'" + user.getLogin() + "'," +
                        "'" + user.getPassword() + "'," +
                        "'" + user.getFirstName() + "'," +
                        "'" + user.getLastName() + "'," +
                        "'" + user.getMail() + "'," +
                        "'" + user.getPhone() + "'," +
                        "'" + user.getComment() + "'," +
                        ");";
        executeQueryVoid(queryForInsertNewUser);
    }

    /**
     * Метод, получающий id из таблицы chatlist в БД.
     * @param chatListRow запись о диалоге
     * @return id записи о диалоге
     */
    public ArrayList<ArrayList<Object>> selectIdForChatListRow(ChatListRow chatListRow) {
        String queryForSelectId =
                "SELECT clid FROM " + DB.settings.get("table_name_for_chat_list") + " " +
                        "WHERE cltablename = '" + chatListRow.getTableName() + "'";
        // может вернуться null - обработать выше по уровню
        return executeQueryReport(queryForSelectId);
    }

    /**
     * Метод, добавляющий запись о диалоге (новую строку в таблице chatlist)
     * @param chatListRow объект записи, которую необходимо добавить
     */
    public void insertNewChatListRow(ChatListRow chatListRow) {
        String queryForInsertChatList =
                "INSERT INTO " + DB.settings.get("table_name_for_chat_list") + " " +
                        "(cluseridmin, cluseridmax, cltablename, clcomment) " +
                        "values (" +
                        chatListRow.getUserIdMin() + "," +
                        chatListRow.getUserIdMax() + "," +
                        "'" + chatListRow.getTableName() + "'," +
                        chatListRow.getComment() +
                        ");"
                ;
        executeQueryVoid(queryForInsertChatList);
    }

    /**
     * Метод, создающий новую таблицу для диалога с наименованием в соответствии с записью о диалоге в таблице chatlist
     * @param chatListRow объект записи, содержащий наименование таблицы (tableName)
     */
    public void createNewTableForChat(ChatListRow chatListRow) {
        String queryForCreateNewTable =
                "CREATE TABLE IF NOT EXISTS " + chatListRow.getTableName() + " (" +
                        "zyid SERIAL PRIMARY KEY, " +
                        "zyauthorid integer NOT null, " +
                        "zycontent varchar(1000) null, " +
                        "zydatetime timestamp NOT null, " +
                        "zycomment varchar(256) null" +
                        ");"
                ;
        executeQueryVoid(queryForCreateNewTable);
    }

    /**
     * Метод, добавляющий внешний ключ к колонке zyauthorid из диалога со ссылкой на колонку usid таблицы user
     * @param chatListRow объект записи о диалоге, содержащий наименование таблицы (tableName)
     */
    public void addForeignKeyForChat(ChatListRow chatListRow) {
        String queryForAddForeignKey =
                "ALTER TABLE " + chatListRow.getTableName() + " " +
                        "ADD CONSTRAINT fkzyauthorid FOREIGN KEY (zyauthorid) " +
                        "REFERENCES " + DB.settings.get("table_name_for_user") + " (usid);"
                ;
        executeQueryVoid(queryForAddForeignKey);
    }

    /**
     * Метод, добавляющий функцию уведомлений для новой таблицы диалога.
     * Также обновляет имена в словаре chatListRow
     * для создаваемой функции уведомлений и создаваемой её уведомлений для прослушивания.
     * @param chatListRow объект записи о диалоге, содержащий наименование таблицы (tableName)
     */
    public void createFunctionNotifyForNewMessage(ChatListRow chatListRow) {
        String functionName = chatListRow.getNameFromDB().get(ChatListRow.NAME.FUNCTION);
        String notifyName =  chatListRow.getNameFromDB().get(ChatListRow.NAME.NOTIFY);
        String tableName = chatListRow.getTableName(); //в конструкторе chatListRow присваивается nameFromDB.get(NAME.TABLE);
        String queryForCreateFunctionNotify =
            "CREATE OR REPLACE FUNCTION " + functionName + "\n" +
            " RETURNS trigger\n" +
            " LANGUAGE plpgsql\n" +
            "AS $function$\n" +
            "DECLARE\n" +
            "BEGIN\n" +
            "  PERFORM pg_notify(" +
                  "'" + notifyName + "', " +
                  "'" + tableName + "' || '|' || NEW.zyid || '|' || NEW.zyauthorid || '|' || NEW.zycontent || '|' || NEW.zydatetime || '|' || NEW.zycomment" +
                  ");\n" +
            "  RETURN NEW;\n" +
            "END;\n" +
            "$function$\n" +
            ";"
        ;
        executeQueryVoid(queryForCreateFunctionNotify);
    }

    /**
     * Метод, создающий триггер для новой таблицы диалога,
     * который будет вызывать функцию уведомлений, при добавлении новой записи о диалоге в таблицу.
     * Также обновляет имя триггера в словаре chatListRow.
     * @param chatListRow объект записи о диалоге, содержащий наименование таблицы (tableName)
     */
    public void createTriggerForExecuteProcedure(ChatListRow chatListRow) {
        String tableName = chatListRow.getNameFromDB().get(ChatListRow.NAME.TABLE);
        String triggerName = chatListRow.getNameFromDB().get(ChatListRow.NAME.TRIGGER);
        String functionName = chatListRow.getNameFromDB().get(ChatListRow.NAME.FUNCTION);
        String queryForCreateTrigger =
                "CREATE TRIGGER " + triggerName + " " +
                        "AFTER INSERT" + " " +
                        "ON " + tableName + " " +
                        "FOR EACH ROW" + " " +
                        "EXECUTE PROCEDURE " + functionName + ";"
                ;
        executeQueryVoid(queryForCreateTrigger);
    }

    /**
     * Метод, получающий из БД список записей о диалогах для пользователя
     * @param user пользователь, для которого необходимо получить записи
     * @return список записей о диалогах
     */
    public ArrayList<ArrayList<Object>> selectAllChatListRowWhereId(User user) {
        String queryForChatListRows =
                "select *" + " " +
                        "from " + DB.settings.get("table_name_for_chat_list") + " " +
                        "where cluseridmin = " + user.getId() + " or cluseridmax = " + user.getId() + ";";
        return executeQueryReport(queryForChatListRows);
    }

//    /**
//     * Метод, получающий из БД id пользователей (useridmin, useridmax)
//     * из списка диалогов для пользователя
//     * @param chatListRow список диалогов пользователя
//     * @return id пользователей из списка диалогов для пользователя
//     */
//    public ArrayList<ArrayList<Object>> selectUserIdMinAndUserIdMax(ChatListRow chatListRow) {
//        String queryForSelectUserIdMinAndUserIdMax =
//                "select userid_min, userid_max" + " " +
//                        "from " + DB.settings.get("table_name_for_chat_list") + " " +
//                        "where id = " + chatListRow.getId() + ";";
//        return executeQueryReport(queryForSelectUserIdMinAndUserIdMax);
//    }

    /**
     * Метод, получающий из БД id и логины пользователей по их id
     * @param userIds id пользователей
     * @return id и логины пользователей
     */
    public ArrayList<ArrayList<Object>> selectIdsAndLoginsForIds(ArrayList<Integer> userIds) {
        String queryForSelectIdsAndLoginsForIdsPart1 =
                "SELECT usid, uslogin FROM " + DB.settings.get("table_name_for_user") + " " +
                "WHERE usid = " + userIds.get(0);
        StringBuilder queryForSelectIdsAndLoginsForIdsPart2 = new StringBuilder("");
        if (userIds.size()>0){
            for (int i = 1; i < userIds.size(); i++) {
                queryForSelectIdsAndLoginsForIdsPart2.append(" or usid = ").append(userIds.get(i));
            }
        }
        queryForSelectIdsAndLoginsForIdsPart2.append(";");

        String queryForSelectIdsAndLoginsForIds =
                queryForSelectIdsAndLoginsForIdsPart1 + queryForSelectIdsAndLoginsForIdsPart2;

        return executeQueryReport(queryForSelectIdsAndLoginsForIds);
    }

    /**
     * Метод, получающий из БД id и логины пользователей, в логинах которых есть поисковое значение valueForSearch
     * @param valueForSearch поисковое значение для поиска id и логинов
     * @return id и логины пользователей
     */
    public ArrayList<ArrayList<Object>> selectIdsAndLoginsForLoginSearch(String valueForSearch) {
        String queryForselectIdsAndLoginsForLoginSearch =
                "select usid, uslogin\n" +
                        "from " + readJSONFile(DB.settingsFilePath, "table_name_for_user") + " \n" +
                        "where uslogin ILIKE '%" + valueForSearch + "%';";

        return executeQueryReport(queryForselectIdsAndLoginsForLoginSearch);
    }
}


// СПОСОБ ЗАГРУЗКИ ИЗ БД ПО НАЗВАНИЮ КОЛОНКИ
//        try (ResultSet newRowFromSQL = stmtForListen.executeQuery(downloadQuery)) {
//            while (newRowFromSQL.next()) {
//                String id = newRowFromSQL.getString("id");
//                String mes = newRowFromSQL.getString("mes");
//                lastMessages.add(new Message(id, mes));
//            }
//        } catch (SQLException e) {
//            System.out.println("НЕ ВЫПОЛНЕНО: Проблема с загрузкой последних сообщений");
//        }