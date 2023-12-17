package gb.study;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
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

    private final AnoWindow anoWindow;
    private final Log log;

    protected Connection connForListenNewChatList;
    protected Statement stmtForListenNewChatList;

    protected Connection connForListenNewMessage;
    protected Statement stmtForListenNewMessage;

    public DB(JFrame window) {
        this.anoWindow = (AnoWindow)window;
        this.log = this.anoWindow.log;
        log.info("DB(JFrame window) Начало - создание БД");
        DB.settings = updateSettings(DB.settings, DB.settingsFilePath,
                "url", "user", "password", "table_name_for_user", "table_name_for_chat_list");

        //todo возможно стоит убрать из конструктора - надо подумать
        connForListenNewChatList = createConnection(DB.settings.get("url"), DB.settings.get("user"), DB.settings.get("password"));
        stmtForListenNewChatList = createStatement(connForListenNewChatList);

        connForListenNewMessage = createConnection(DB.settings.get("url"), DB.settings.get("user"), DB.settings.get("password"));
        stmtForListenNewMessage = createStatement(connForListenNewMessage);

        log.info("DB(JFrame window) Конец - БД создана");
    }

    /**
     * Проверить существование статического словаря настроек и ключей в нём.
     * Если словаря нет, то создать.
     * Если ключей в словаре нет, то добавить, прочитав их значения из файла.
     * @param mapForUpdate словарь, который необходимо проверить и вернуть.
     * @param filePath путь к файлу, в котором хранятся ключи и значения для словаря
     * @param jsonKeys ключи, которые необходимо проверить или добавить
     * @return Проверенный и дополненный в случае необходимости словарь.
     */
    private Map<String, String> updateSettings(Map<String, String> mapForUpdate, String filePath, String... jsonKeys) {
        log.info("updateSettings(..) Начало - обновление словаря настроек");
        if (mapForUpdate == null) mapForUpdate = new HashMap<>();
        for (var jsonKey:jsonKeys) {
            if (!mapForUpdate.containsKey(jsonKey))
                mapForUpdate.put(jsonKey, readJSONFile(filePath, jsonKey));
        }
        log.info("updateSettings(..) Конец - обновление словаря настроек");
        return mapForUpdate;
    }
    /**
     * Метод, получающий значение по ключу из JSON-файла
     * @param filePath путь к файлу JSON,
     * @param jsonKey ключ, по которому необходимо найти значение
     * @return искомое значение
     */
    public String readJSONFile(String filePath, String jsonKey) {
        log.info("readJSONFile(..) Начало - получение значения по ключу JSON из файла");
        String jsonValue = null;
        try (FileReader reader = new FileReader(filePath))
        {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);
            jsonValue = jsonObject.getString(jsonKey);
        } catch (FileNotFoundException e) {
            log.problem("Файл не найден: ", filePath);
            e.printStackTrace();
        } catch (JSONException e) {
            log.problem("Ошибка при распознавании JSON. Ключ: ", jsonKey);
            e.printStackTrace();
        } catch (IOException e) {
            log.problem("Проблема с вводом-выводом при чтении файла: ", filePath);
            e.printStackTrace();
        }
        log.info("readJSONFile(..) Конец - получение значения по ключу JSON из файла");
        return jsonValue;
    }

    /**
     * Метод, создающий Statement, используя готовое соединение
     * @param connection соединение
     * @return созданный Statement
     */
    private Statement createStatement(Connection connection) {
        log.info("createStatement(..) Начало создания stmt");
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
        } catch (SQLException e) {
            log.problem("Проблема с созданием Statement");
        }
        log.info("createStatement(..) Конец создания stmt");
        return stmt;
    }

    /**
     * Метод, создающий подключение
     * @param url адрес БД
     * @param user логин БД
     * @param password пароль БД
     * @return соединение
     */
    protected Connection createConnection(String url, String user, String password){
        log.info("createConnection(..) Начало");
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            log.problem("Проблема - исключение при вызове Class.forName(\"org.postgresql.Driver\");");
        }
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            log.problem("Проблема - исключение при вызове conn = DriverManager.getConnection(url, user, password)");
        }
        log.info("createConnection(..) Конец");
        return conn;
    }


    /**
     * Метод, получающий из БД последние messagesCount сообщений из определенного чата
     * @param chatListRow запись о диалоге, содержащая название чата
     * @param messagesCount количество последних сообщений, которое необходимо получить из БД
     */
    public ArrayList<ArrayList<Object>> selectLastMessages(ChatListRow chatListRow, int messagesCount) {
        log.info("selectLastMessages(..) Начало");
        String downloadQuery =
                "SELECT * " +
                "FROM " +
                    "(SELECT * from " + chatListRow.getTableName() + " " +
                    "ORDER BY " + "zydatetime" + " DESC LIMIT " + messagesCount + ") " +
                "AS last_message_not_ordered " +
                "ORDER BY " + "zydatetime" + " ASC;";
        log.info("selectLastMessages(..) Конец - далее запрос в базу и return");
        return executeQueryReport(downloadQuery);
    }

    /**
     * Универсальный метод, выполняющий запрос в БД.
     * Устанавливает свое соединение с базой и выполняет запрос.
     * @param query запрос, который необходимо выполнить
     */
    public void executeQueryVoid(String query) {
        log.info("executeQueryVoid(..) Начало выполнения действия");
        Statement stmtForQuery = createStatement(
                createConnection(DB.settings.get("url"), DB.settings.get("user"), DB.settings.get("password"))
        );
        try {
            stmtForQuery.execute(query);
        } catch (SQLTimeoutException e) {
            log.problem("Истекло время ожидания при общении с БД." + e.getMessage());
        } catch (SQLException e) {
            log.problem("Ошибка доступа к БД." + e.getMessage());
        }
        log.info("executeQueryVoid(..) Конец выполнения действия");
    }

    /**
     * Универсальный метод, выдающий отчет из базы данных по запросу.
     * Устанавливает свое соединение с базой и выполняет запрос.
     * @param query запрос SQL
     * @return отчет из базы данных -> таблица = коллекция коллекций
     */
    public ArrayList<ArrayList<Object>> executeQueryReport(String query){
        log.info("executeQueryReport(..) Начало универсального метода запроса в БД с выводом");
        ArrayList<ArrayList<Object>> resultReport = new ArrayList<>();
        try (
                Statement stmtForQuery = createStatement(
                        createConnection(DB.settings.get("url"), DB.settings.get("user"), DB.settings.get("password"))
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
                }
                resultReport.add(newRowReport);
                log.info("Выгружена новая строка: ", newRowReport.toString());
            }
        } catch (SQLTimeoutException e) {
            log.problem("Истекло время ожидания при общении с БД.", e.getMessage());
        } catch (SQLException e) {
            log.problem("Ошибка доступа к БД.", e.getMessage());
        }
        log.info("executeQueryReport(..) Конец универсального метода запроса в БД с выводом");
        return resultReport;
    }

    /**
     * Метод, получающий id, login, password из таблицы user в БД по логину
     * @param login пользователя
     */
    public ArrayList<ArrayList<Object>> selectIdLoginPasswordForUser(String login) {
        log.warning("selectIdLoginPasswordForUser(..) Начало");
        String queryForGetIdLoginPassword =
                "SELECT usid, uslogin, uspassword FROM " + DB.settings.get("table_name_for_user") + " " +
                        "WHERE uslogin = '" + login + "'";
        log.warning("selectIdLoginPasswordForUser(..) Конец - далее запрос в базу и return");
        return executeQueryReport(queryForGetIdLoginPassword);
    }

    /**
     * Метод, получающий id из таблицы chatlist в БД по наименованию таблицы.
     * @param chatListRow запись о диалоге, содержащая наименование таблицы
     * @return id записи о диалоге
     */
    public ArrayList<ArrayList<Object>> selectIdForChatListRow(ChatListRow chatListRow) {
        log.info("selectIdForChatListRow() Начало");
        String queryForSelectId =
                "SELECT clid FROM " + DB.settings.get("table_name_for_chat_list") + " " +
                        "WHERE cltablename = '" + chatListRow.getTableName() + "'";
        log.info("selectIdForChatListRow() Конец, далее действие в return. Сам запрос:", queryForSelectId);
        return executeQueryReport(queryForSelectId);
    }

    /**
     * Метод, добавляющий запись о диалоге (новую строку в таблице chatlist)
     * @param chatListRow объект записи, которую необходимо добавить
     */
    public void insertNewChatListRow(ChatListRow chatListRow) {
        log.info("insertNewChatListRow(..) Начало выполнения действия");
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
        log.info("insertNewChatListRow(..) Конец - добавлена запись о диалоге: ", chatListRow.getTableName());
    }

    /**
     * Метод, создающий новую таблицу для диалога с наименованием в соответствии с записью о диалоге в таблице chatlist
     * @param chatListRow объект записи, содержащий наименование таблицы (tableName)
     */
    public void createNewTableForChat(ChatListRow chatListRow) {
        log.info("createNewTableForChat(..) Начало");
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
        log.info("createNewTableForChat(..) Конец - создана таблица: ", chatListRow.getTableName());
    }

    /**
     * Метод, добавляющий внешний ключ к колонке zyauthorid из диалога со ссылкой на колонку usid таблицы user
     * @param chatListRow объект записи о диалоге, содержащий наименование таблицы (tableName)
     */
    public void addForeignKeyForChat(ChatListRow chatListRow) {
        log.info("addForeignKeyForChat(..) Начало");
        String queryForAddForeignKey =
                "ALTER TABLE " + chatListRow.getTableName() + " " +
                        "ADD CONSTRAINT fkzyauthorid FOREIGN KEY (zyauthorid) " +
                        "REFERENCES " + DB.settings.get("table_name_for_user") + " (usid);"
                ;
        executeQueryVoid(queryForAddForeignKey);
        log.info("addForeignKeyForChat(..) Конец - внешний ключ для id автора установлен в таблице: ", chatListRow.getTableName());
    }

    /**
     * Метод, добавляющий функцию для создания уведомлений о новых сообщениях в новой таблицы диалога.
     * @param chatListRow объект записи о диалоге, содержащий словарь всех наименований
     */
    public void createFunctionNotifyForNewMessage(ChatListRow chatListRow) {
        log.info("createFunctionNotifyForNewMessage(..) Начало");
        String functionName = chatListRow.getNamesFromDB().get(ChatListRow.NAME.FUNCTION);
        String notifyName =  chatListRow.getNamesFromDB().get(ChatListRow.NAME.NOTIFY);
        String tableName = chatListRow.getTableName(); //это не из БД - в конструкторе chatListRow присваивается nameFromDB.get(NAME.TABLE);
        String notifyRow = "'" + tableName + "' || '|' || NEW.zyid";// + " || '|' || NEW.zyauthorid || '|' || NEW.zycontent || '|' || NEW.zydatetime || '|' || NEW.zycomment";
        String queryForCreateFunctionNotify =
                "CREATE OR REPLACE FUNCTION " + functionName + "\n" +
                        " RETURNS trigger\n" +
                        " LANGUAGE plpgsql\n" +
                        "AS $function$\n" +
                        "DECLARE\n" +
                        "BEGIN\n" +
                        "  PERFORM pg_notify(" + "'" + notifyName + "', " + notifyRow + ");\n" +
                        "  RETURN NEW;\n" +
                        "END;\n" +
                        "$function$\n" +
                        ";"
                ;
        executeQueryVoid(queryForCreateFunctionNotify);
        log.info("createFunctionNotifyForNewMessage(..) Конец - создана ф-я создания уведомлений",
                "о новых сообщения в таблице: ", chatListRow.getTableName());
    }

    /**
     * Метод, создающий триггер для новой таблицы диалога, срабатывающий на добавление новой записи о диалоге в таблицу,
     * который будет вызывать функцию уведомлений.
     * @param chatListRow объект записи о диалоге, содержащий словарь всех наименований
     */
    public void createTriggerForExecuteProcedure(ChatListRow chatListRow) {
        log.info("createTriggerForExecuteProcedure(..) Начало");
        String tableName = chatListRow.getNamesFromDB().get(ChatListRow.NAME.TABLE);
        String triggerName = chatListRow.getNamesFromDB().get(ChatListRow.NAME.TRIGGER);
        String functionName = chatListRow.getNamesFromDB().get(ChatListRow.NAME.FUNCTION);
        String queryForCreateTrigger =
                "CREATE TRIGGER " + triggerName + " " +
                        "AFTER INSERT" + " " +
                        "ON " + tableName + " " +
                        "FOR EACH ROW" + " " +
                        "EXECUTE PROCEDURE " + functionName + ";"
                ;
        executeQueryVoid(queryForCreateTrigger);
        log.info("createTriggerForExecuteProcedure(..) Конец - создан триггер для таблицы: ", chatListRow.getTableName());
    }

    /**
     * Метод, получающий из БД id и логины пользователей по их id
     * @param userIds id пользователей
     * @return id и логины пользователей
     */
    public ArrayList<ArrayList<Object>> selectIdsAndLoginsForIds(ArrayList<Integer> userIds) {
        log.info("selectIdsAndLoginsForIds(..) Начало");
        if(userIds.isEmpty()) return null;
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

        log.info("selectIdsAndLoginsForIds(..) Конец - далее действие в return");
        return executeQueryReport(queryForSelectIdsAndLoginsForIds);
    }

    /**
     * Отправляет сообщение в БД в таблицу, название которой, указанно в записи о диалоге
     * @param message сообщение
     * @param chatListRow запись о диалоге
     */
    public void sendNewMessage(Message message, ChatListRow chatListRow) {
        log.info("db.sendNewMessage(..) Начало");
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
            stmtForListenNewChatList.execute(queryForInsertNewMessage);
        } catch (SQLTimeoutException e) {
            log.problem("Истекло время ожидания при общении с БД." + e.getMessage());
        } catch (SQLException e) {
            log.problem("Ошибка доступа к БД." + e.getMessage());
        }
        log.info("db.sendNewMessage(..) Конец - сообщение отправлено в БД");
    }

    /**
     * Прослушивание уведомлений о новых сообщениях в диалогах
     * @param chatListRows коллекция всех записей о диалогах.
     *                     Из них получаются наименования уведомлений, попадающих в запрос.
     */
    public void startListenerNewMessage(ArrayList<ChatListRow> chatListRows) {
        log.info("db.startListenerNewMessage(..) - Начло. Записи о конце не будет,",
                "если есть что слушать (записи о диалоге с пользователем),",
                "потому в бесконечном цикле в асинхронном методе запускается прослушивание сообщений");
        if(chatListRows.isEmpty()) {
            log.warning("db.startListenerNewMessage(..) - Конец - слушать нечего, видимо нет ни одного заведенного диалога");
            return;
        }
        StringBuilder queriesForListenNotify = new StringBuilder();
        for (var chatListRow : chatListRows) {
            String notifyName = chatListRow.getNamesFromDB().get(ChatListRow.NAME.NOTIFY);
            queriesForListenNotify.append("LISTEN ").append(notifyName).append("; ");
        }
        try {
            stmtForListenNewMessage.execute(queriesForListenNotify.toString());
            log.info("Выполнен запрос в БД для прослушивания новых сообщений:", queriesForListenNotify.toString());
        } catch (SQLException e) {
            log.problem("Возникла проблема с выполнением запроса в БД для прослушивания уведомлений о новых сообщениях",
                    e.getMessage());
        }

        PGConnection pgConnForListen = (PGConnection) connForListenNewMessage;
        while (true) {
            PGNotification[] newMessageNotifications = new PGNotification[0];
            try {
                newMessageNotifications = pgConnForListen.getNotifications();
            } catch (SQLException e) {
                log.problem("Возникла проблема с полученным уведомлением о новом сообщении:",
                        "newMessageNotifications = pgConnForListen.getNotifications();", e.getMessage());
                throw new RuntimeException(e);
            }
            if (newMessageNotifications != null) {
                for (PGNotification newMessageNotification : newMessageNotifications) {
                    String[] parts = newMessageNotification.getParameter().split("\\|");
                    String tableName = parts[0];
                    log.info("Уведомление о новом сообщении - новой записи в tableName = parts[0] = ", tableName);
                    //todo по ходу нужно только название таблицы?
                    //Integer id = Integer.parseInt(parts[1]);
                    //Integer authorId = Integer.parseInt(parts[2]);
                    //String content = parts[3];
                    //Timestamp datetime =  Timestamp.valueOf(parts[4]);
                    //String comment = parts[5];
                    ChatListRow chatListRow = anoWindow.getUser().getChatListRowByTableName(tableName);
                    Integer idDisputer = anoWindow.getUser().calculateDisputerId(chatListRow);
                    if (!anoWindow.getUser().isActiveChatListRow(chatListRow)) return;
                    if ( anoWindow.getUser().getChats() == null || !anoWindow.getUser().getChats().containsKey(idDisputer) ){
                        log.problem("Ситуация, которая возможна только в теории, на практике такого не должно быть.",
                                "Отсутствует словарь ''id->chat'' или id (по от кого сообщение) в этом словаре пользователя.");
                    }
                    //todo создать другой метод для загрузки не последних сообщений, а только последнего по его id
                    anoWindow.getUser().getChats().get(idDisputer).parseLastMessages(
                            chatListRow,
                            anoWindow.tabSettingsPanel.parseCountMessagesForDownload(),
                            anoWindow
                    );
                    anoWindow.tabChatPanel.addAndShowMessagesFromList(
                            new ArrayList<>(anoWindow.getUser().getChats().get(idDisputer).getMessages().values())
                    );
                    audioNotification();
                }
            }

            try {
                Thread.sleep(1005);
            } catch (InterruptedException | IllegalArgumentException e) {
                log.problem("В методе прослушивания DB.startListenerNewMessage(..) проблема с засыпанием Thread.sleep(..)");
            }
        }

    }

    /**
     * Прослушивание уведомлений о новых записях о диалогах.
     * После получения проверяет отношение User к ChatListRow проверкой двух id собеседников.
     * Если ChatListRow относится к User, то запускает метод для настройки нового собеседника:
     * опознание, добавление во все словари, добавление на панель + обработчик нажатия, прослушивание сообщений.
     */
    public void startListenerNewChatListRow() {
        log.info("db.startListenerNewChatListRow() - Начало. Записи о конце не будет,",
                "потому в бесконечном цикле в асинхронном методе запускается прослушивание уведомление о добавлении новых записей о диалогах");
        String notifyName = "ncl";
        String queryForListenNotify = "LISTEN " + notifyName + "; ";

        connForListenNewChatList = createConnection(DB.settings.get("url"), DB.settings.get("user"), DB.settings.get("password"));
        stmtForListenNewChatList = createStatement(connForListenNewChatList);

        try {
            stmtForListenNewChatList.execute(queryForListenNotify.toString());
            log.info("Выполнен запрос в БД для прослушивания уведомлений о новых записях о диалогах:",
                    queryForListenNotify.toString());
        } catch (SQLException e) {
            log.problem("Возникла проблема с выполнением запроса в БД для прослушивания уведомлений о новых записях о диалогах",
                    e.getMessage());
        }

        PGConnection pgConnForListen = (PGConnection) connForListenNewChatList;
        while (true) {
            PGNotification[] newChatListRowNotifications = new PGNotification[0];
            try {
                newChatListRowNotifications = pgConnForListen.getNotifications();
            } catch (SQLException e) {
                log.problem("Возникла проблема с полученным уведомлением о новой записи о диалоге:",
                        "newChatListRowNotifications = pgConnForListen.getNotifications();", e.getMessage());
                throw new RuntimeException(e);
            }
            if (newChatListRowNotifications != null) {
                for (PGNotification newChatListRowNotification : newChatListRowNotifications) {
                    String[] notifyParts = newChatListRowNotification.getParameter().split("\\|");
                    Integer id=0, userIdMin=0, userIdMax=0; String tableName=null, comment=null;
                    try{
                        id = Integer.parseInt(notifyParts[0]);
                        userIdMin = Integer.parseInt(notifyParts[1]);
                        userIdMax = Integer.parseInt(notifyParts[2]);
                        tableName = notifyParts[3]; //серое, потому что не нужно, потому что имя получается по формуле
                        comment = notifyParts[4];
                    }catch (NumberFormatException e){
                        log.problem("Проблема при распознавании ячеек ChatListRow,",
                                "полученного в текстовом уведомлении о новой записи ChatListRow",
                                "(еще не факт, что от собеседника - пока непонятно от кого)");
                    }
                    if (anoWindow.getUser().getId() != userIdMin && anoWindow.getUser().getId() != userIdMax) return;
                    ChatListRow chatListRow = new ChatListRow(userIdMin, userIdMax, comment, anoWindow);
                    log.info("Уведомление от нового собеседника с tableName:", chatListRow.getTableName());
                    anoWindow.getUser().addNewDisputerFromDBNotify(chatListRow);
                    audioNotification(); //todo проверить автора, чтоб понять надо ли звучать
                }
            }

            try {
                Thread.sleep(7003);
            } catch (InterruptedException | IllegalArgumentException e) {
                log.problem("В методе прослушивания DB.startListenerNewChatListRow() проблема с засыпанием Thread.sleep(..)");
            }
        }
    }
    
    /**
     * Воспроизведение звукового уведомления
     */
    public void audioNotification() {
        this.log.info("audioNotification() Начало");
        //todo надо как-то прикрепить к программе звуковой файл
        String soundFilePath = "E:\\Csharp\\GB\\Ano\\Anoswing\\Ano\\src\\main\\resources\\sounds\\audioMes.wav";
        try {
            File soundFile = new File(soundFilePath);
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(soundFile));
            clip.start();
            this.log.info("audioNotification() Конец - звуковое уведомление");
        } catch (Exception e) {
            log.warning("Проблема с воспроизведением звукового уведомления", e.getMessage().toString());
            e.printStackTrace();
        }
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

    /**
     * Метод, получающий из БД id и логины пользователей, в логинах которых есть поисковое значение valueForSearch
     * @param valueForSearch поисковое значение для поиска id и логинов
     * @return id и логины пользователей
     */
    public ArrayList<ArrayList<Object>> selectIdsAndLoginsForLoginSearch(String valueForSearch) {
        log.info("selectIdsAndLoginsForLoginSearch(..) Начало");
        String queryForSelectIdsAndLoginsForLoginSearch =
                "select usid, uslogin\n" +
                        "from " + readJSONFile(DB.settingsFilePath, "table_name_for_user") + " \n" +
                        "where uslogin ILIKE '%" + valueForSearch + "%';";
        log.info("selectIdsAndLoginsForLoginSearch(..) Конец - следом return. Сам запрос:",
                queryForSelectIdsAndLoginsForLoginSearch);
        return executeQueryReport(queryForSelectIdsAndLoginsForLoginSearch);
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