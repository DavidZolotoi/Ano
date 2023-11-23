package gb.study;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DB {
    protected Connection connForListen;
    protected Connection connForSend;

    public DB() {
        connForListen = getConnection();
        connForSend = getConnection();
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

        // Создание Statement для скачиваний
        Statement stmt1 = null;
        try {
            stmt1 = connForListen.createStatement();
        } catch (SQLException e) {
            System.out.println("НЕ ВЫПОЛНЕНО: Проблема с stmt1");
        }

        // todo в будущем сортировку надо сделать по дате, а не по id
        String downloadQuery =
                "SELECT id, mes FROM " +
                        "(SELECT id, mes FROM messagestable ORDER BY id DESC LIMIT " + countMessagesDownloadAtStart + ") " +
                        "AS subquery ORDER BY id ASC";
        try (ResultSet newRowFromSQL = stmt1.executeQuery(downloadQuery)) {
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
}
