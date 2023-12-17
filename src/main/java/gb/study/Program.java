package gb.study;

import javax.swing.*;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Program {
    public static void main(String[] args) {
        Path jarParentPath = getJarParentPath();
        Log log = Log.getLog(jarParentPath);
        log.info(System.lineSeparator(), "..........................................",
                 System.lineSeparator(), "||||||||||| ПРОГРАММА ЗАПУЩЕНА |||||||||||",
                 System.lineSeparator(), "''''''''''''''''''''''''''''''''''''''''''");
        log.info("Путь к каталогу с jar-файлом и логами: ", jarParentPath.toString());

        new AnoWindow(log);
    }

    /**
     * Определяет путь к родительскому каталогу для jar.
     * Это нужно, чтоб рядом положить файл с логами.
     * @return путь к родительскому каталогу для jar.
     */
    private static Path getJarParentPath() {
        URL location = Program.class.getProtectionDomain().getCodeSource().getLocation();
        String jarPath;
        try {
            jarPath = new File(location.toURI()).getPath();
        } catch (URISyntaxException e) {
            //todo сделать логгер-заглушку, чтоб не использовать запись в файл
            JOptionPane.showMessageDialog(
                    null,
                    "Проблема с ведением логов - их запись невозможна."
            );
            throw new RuntimeException(e);
        }
        return Paths.get(jarPath).getParent();
    }
}


//todo Задачи текущие:
// 1. Добавить try..catch везде, где нужно
// 2. Добавить логи

//todo Задачи в долгий ящик:
// 1. Перепроверить модификаторы
// 2. При new Dimension(x, y) можно указать 0
// 3. В логах в DB вероятно лучше дублировать получившийся текст запроса в лог
// 4. Сделать запись логов в файл асинхронной,
// а если сильно заморочиться, то уменьшить количество записей в файл за счет накопления
// 5. Для работы на другом компьютере надо указать откуда брать данные для подключения к БД (где json)
// 6. Создать интерфейс с готовыми реализациями методов преобразования типов
// 7. Отмечать логин, когда появляются новые сообщения и снимать отметку при прочтении
// . Разметка

