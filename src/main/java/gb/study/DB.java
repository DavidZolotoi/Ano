package gb.study;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DB {
    protected Connection connForListen;
    protected Statement stmtForListen;

    protected Connection connForSend;
    protected Statement stmtForSend;

    public DB() {
        connForListen = getConnection();
        stmtForListen = getStatement(connForListen);

        connForSend = getConnection();
        stmtForSend = getStatement(connForSend);
    }

    private Statement getStatement(Connection connection) {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
        } catch (SQLException e) {
            System.out.println("НЕ ВЫПОЛНЕНО: Проблема с Statement");
        }
        return stmt;
    }

    //Возврат подключения к базе
    protected Connection getConnection(){
        var dbInfo = readJSONFile("E:\\Csharp\\GB\\Ano\\Anoswing\\settings_past_the_git.json");
        String url = dbInfo.get("url");
        String user = dbInfo.get("user");
        String password = dbInfo.get("password");
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
     * Метод, конвертирующий данные из JSON в словарь
     * @param filePath путь к файлу JSON
     * @return словарь
     */
    public Map<String, String> readJSONFile(String filePath) {
        Map<String, String> dict = new HashMap<>();
        try {
            // Чтение файла JSON
            FileReader reader = new FileReader(filePath);
            JSONTokener tokener = new JSONTokener(reader);

            // Создание объекта JSONObject из содержимого файла
            JSONObject jsonObject = new JSONObject(tokener);

            // Получение значений по ключам
            String url = jsonObject.getString("url");
            String user = jsonObject.getString("user");
            String password = jsonObject.getString("password");

            // Заполнение словаря
            dict.put("url", url);
            dict.put("user", user);
            dict.put("password", password);
        } catch (Exception e) {
            System.out.println("НЕ ВЫПОЛНЕНО: Проблема с прочтением JSON");
            e.printStackTrace();
        }
        return dict;
    }

    public ArrayList<Message> getLastMessages(int countMessagesDownloadAtStart) {
        ArrayList<Message> lastMessages = new ArrayList<>(countMessagesDownloadAtStart);
        lastMessages.add(new Message("...", "..."));

        // todo в будущем сортировку надо сделать по дате, а не по id
        String downloadQuery =
                "SELECT id, mes FROM " +
                        "(SELECT id, mes FROM messagestable ORDER BY id DESC LIMIT " + countMessagesDownloadAtStart + ") " +
                        "AS subquery ORDER BY id ASC";
        try (ResultSet newRowFromSQL = stmtForListen.executeQuery(downloadQuery)) {
            while (newRowFromSQL.next()) {
                String id = newRowFromSQL.getString("id");
                String mes = newRowFromSQL.getString("mes");
                lastMessages.add(new Message(id, mes));
                //показ сообщений
                //if (!printMessage(id, mes)) System.out.println("НЕ ВЫПОЛНЕНО: Проблема с показом загруженного сообщения");
            }
        } catch (SQLException e) {
            System.out.println("НЕ ВЫПОЛНЕНО: Проблема с загрузкой последних сообщений");
        }

        return lastMessages;
    }

    public void sendNewMessage(Message message) {
        // отправляем запрос INSERT
        String insertQuery = "INSERT INTO messagestable (id, mes) VALUES ('" + message.author + "', '" + message.text + "')";
        //todo сдвинуть фокус на ввод
        try {
            stmtForSend.execute(insertQuery);
        } catch (SQLException e) {
            System.out.println("НЕ ВЫПОЛНЕНО: Проблема с stmt2.execute(insertQuery): \n" + e.getMessage());
        }
    }

    /**
     * Метод скорее для экспериментальных действий
     * устанавливает свое соединение с базой и выполняет запрос
     * @param query запрос, который необходимо выполнить
     */
    public void executeQuery(String query) {
        Statement stmtForQuery = getStatement(getConnection());
        try {
            stmtForQuery.execute(query);
        } catch (SQLException e) {
            System.out.println("НЕ ВЫПОЛНЕНО: Проблема с stmtForQuery.execute(insertQuery): \n" + e.getMessage());
        }
    }

    /**
     * Прослушивание
     * @param anoWindow ссылка на окно,
     *                  в котором есть метод, вставляющий новые сообщения в свое место на окне
     */
    public void startListenerDB(AnoWindow anoWindow) {
        String listenQuery = "LISTEN message_inserted";
        try {
            stmtForListen.execute(listenQuery);
        } catch (SQLException e) {
            System.out.println("НЕ ВЫПОЛНЕНО: LISTEN message_inserted");
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
                for (PGNotification notification : notifications) {
                    String[] parts = notification.getParameter().split("\\|");
                    String author = parts[0];
                    String mesText = parts[1];
                    //Вставка и показ сообщений
                    anoWindow.tabChatPanel.addAndShowNewMessage(new Message(author, mesText));
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
}
