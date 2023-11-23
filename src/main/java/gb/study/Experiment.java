package gb.study;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class Experiment {
    public static void main(String[] args) {
        System.out.println("Experiment");
        var dbInfo = readJSONFile("E:\\Csharp\\GB\\Ano\\Anoswing\\settings_past_the_git.json"); //todo переделать путь на кроссплатформ
    }

    public static Map<String, String> readJSONFile(String filePath) {
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

            // Вывод словаря todo удалить из кода
            System.out.println(dict);
        } catch (Exception e) {
            System.out.println("НЕ ВЫПОЛНЕНО: Проблема с прочтением JSON");
            e.printStackTrace();
        }
        return dict;
    }
}
