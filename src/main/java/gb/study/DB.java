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
                    "ORDER BY " + "mes_datetime" + " DESC LIMIT " + countMessagesDownloadAtStart + ") " +
                "AS last_message_not_ordered " +
                "ORDER BY " + "mes_datetime" + " ASC;";
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
                        "mes_author_id, " +
                        "mes_content, " +
                        "mes_datetime, " +
                        "mes_comment" +
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
    public void startListenerChat(ChatListRow chatListRow, AnoWindow anoWindow) {
        System.out.println("--- DB метод прослушки чата");
        String notifyName = chatListRow.getNameFromDB().get(ChatListRow.NAME.NOTIFY);
        String listenQuery = "LISTEN " + notifyName;
        try {
            stmtForListen.execute(listenQuery);
        } catch (SQLException e) {
            System.out.println("НЕ ВЫПОЛНЕНО: LISTEN " + notifyName);
        }

        PGConnection pgConn = (PGConnection)connForListen;
        while (true) {
            PGNotification[] notifications = new PGNotification[0];
            try {
                notifications = pgConn.getNotifications();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (notifications != null) {
                Integer idInterlocutor = anoWindow.getUser().calculateInterlocutorId(chatListRow);
                for (PGNotification notification : notifications) {
                    String[] parts = notification.getParameter().split("\\|");
                    Integer id = Integer.parseInt(parts[0]);
                    Integer authorId = Integer.parseInt(parts[1]);
                    String content = parts[2];
                    Timestamp datetime =  Timestamp.valueOf(parts[3]);
                    String comment = parts[4];
                    //Вставка и показ сообщений в чат и в окно
                    anoWindow.getUser().getChats().get(idInterlocutor).setNewMessage(
                            new Message(id, authorId, content, datetime, comment),
                            anoWindow
                    );
                    audioNotification();
                }
            }

            try {
                Thread.sleep(500);
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
                "SELECT id, login, password FROM " + DB.settings.get("table_name_for_user") + " " +
                        "WHERE login = '" + user.getLogin() + "'";
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
                        "login" + ", " +
                        "password" + ", " +
                        "first_name" + ", " +
                        "last_name" + ", " +
                        "mail" + ", " +
                        "phone" + ", " +
                        "comment" +
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

        //ПОЛУЧИТЬ id запросом в БД и вернуть его
//        String queryForReport = "select id" + " " +
//                "from " + DB.settings.get("table_name_for_user") + " " +
//                "where login = '" + this.login + "';";
//        return executeQueryReport(queryForReport);
    }

    /**
     * Метод, получающий id из таблицы chat_list в БД.
     * @param chatListRow запись о диалоге
     * @return id записи о диалоге
     */
    public ArrayList<ArrayList<Object>> selectIdForChatListRow(ChatListRow chatListRow) {
        String queryForSelectId =
                "SELECT id FROM " + DB.settings.get("table_name_for_chat_list") + " " +
                        "WHERE table_name = '" + chatListRow.getTableName() + "'";
        // может вернуться null - обработать выше по уровню
        return executeQueryReport(queryForSelectId);
    }

    /**
     * Метод, добавляющий запись о диалоге (новую строку в таблице chat_list)
     * @param chatListRow объект записи, которую необходимо добавить
     */
    public void insertNewChatListRow(ChatListRow chatListRow) {
        String queryForInsertChatList =
                "INSERT INTO " + DB.settings.get("table_name_for_chat_list") + " " +
                        "(userid_min, userid_max, table_name, comment) " +
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
     * Метод, создающий новую таблицу для диалога с наименованием в соответствии с записью о диалоге в таблице chat_list
     * @param chatListRow объект записи, содержащий наименование таблицы (tableName)
     */
    public void createNewTableForChat(ChatListRow chatListRow) {
        String queryForCreateNewTable =
                "CREATE TABLE IF NOT EXISTS " + chatListRow.getTableName() + " (" +
                        "id SERIAL PRIMARY KEY, " +
                        "mes_author_id integer NOT null, " +
                        "mes_content varchar(1000) null, " +
                        "mes_datetime timestamp NOT null, " +
                        "mes_comment varchar(256) null" +
                        ");"
                ;
        executeQueryVoid(queryForCreateNewTable);
    }

    /**
     * Метод, добавляющий внешний ключ к колонке mes_author_id из диалога со ссылкой на колонку id таблицы user_list
     * @param chatListRow объект записи о диалоге, содержащий наименование таблицы (tableName)
     */
    public void addForeignKeyForChat(ChatListRow chatListRow) {
        String queryForAddForeignKey =
                "ALTER TABLE " + chatListRow.getTableName() + " " +
                        "ADD CONSTRAINT fk_mes_author_id FOREIGN KEY (mes_author_id) " +
                        "REFERENCES " + DB.settings.get("table_name_for_user") + " (id);"
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
        String queryForCreateFunctionNotify =
                "CREATE OR REPLACE FUNCTION " + functionName + "\n" +
                " RETURNS trigger\n" +
                " LANGUAGE plpgsql\n" +
                "AS $function$\n" +
                "DECLARE\n" +
                "BEGIN\n" +
                "  PERFORM pg_notify(" +
                          "'" + notifyName + "', " +
                          "NEW.mes_author_id || '|' || mes_content || '|' || mes_datetime || '|' || mes_comment" +
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
                        "where userid_min = " + user.getId() + " or userid_max = " + user.getId() + ";";
        return executeQueryReport(queryForChatListRows);
    }

    /**
     * Метод, получающий из БД id пользователей (userid_min, userid_max)
     * из списка диалогов для пользователя
     * @param chatListRow список диалогов пользователя
     * @return id пользователей из списка диалогов для пользователя
     */
    public ArrayList<ArrayList<Object>> selectUserIdMinAndUserIdMax(ChatListRow chatListRow) {
        String queryForSelectUserIdMinAndUserIdMax =
                "select userid_min, userid_max" + " " +
                        "from " + DB.settings.get("table_name_for_chat_list") + " " +
                        "where id = " + chatListRow.getId() + ";";
        return executeQueryReport(queryForSelectUserIdMinAndUserIdMax);
    }

    /**
     * Метод, получающий из БД id и логины пользователей по их id
     * @param userIds id пользователей
     * @return id и логины пользователей
     */
    public ArrayList<ArrayList<Object>> selectIdsAndLoginsForIds(ArrayList<Integer> userIds) {
        String queryForSelectIdsAndLoginsForIdsPart1 =
                "SELECT id, login FROM " + DB.settings.get("table_name_for_user") + " " +
                "WHERE id = " + userIds.get(0);
        StringBuilder queryForSelectIdsAndLoginsForIdsPart2 = new StringBuilder("");
        if (userIds.size()>0){
            for (int i = 1; i < userIds.size(); i++) {
                queryForSelectIdsAndLoginsForIdsPart2.append(" or id = ").append(userIds.get(i));
            }
        }
        queryForSelectIdsAndLoginsForIdsPart2.append(";");

        String queryForSelectIdsAndLoginsForIds =
                queryForSelectIdsAndLoginsForIdsPart1 + queryForSelectIdsAndLoginsForIdsPart2;

        return executeQueryReport(queryForSelectIdsAndLoginsForIds);
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