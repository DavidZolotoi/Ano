package gb.study;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/*
 Касаемо Level - из документации: названия и значения
 OFF, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL
 MAX_VALUE, 1000, 900, 800, 700, 500, 400, 300, MIN_VALUE
Как по Singleton будет содержать один статический логгер
*/
public class Log {
    private static Log log;

    /**
     * Создание объекта логгера, если его еще нет
     * @param logFilePath путь к файлу для логов
     * @return ссылка на объект логгера
     */
    protected static Log getLog(Path logFilePath) {
        if (Log.log == null)
            Log.log = new Log(logFilePath);
        return Log.log;
    }

    private final Logger logger;
    private final Path pathLogFileParent;

    /**
     * Конструктор для одноразового создания логгера.
     * Создает файл для записи логов, если его не существует.
     * Передает логгеру FileHandler с созданным файлом.
     * И настраивает формат записи логов (xml или просто текст)
     * @param logFileParentPath путь к родительскому каталогу для файла с логами.
     */
    private Log(Path logFileParentPath) {
        this.pathLogFileParent = logFileParentPath;
        this.logger = Logger.getLogger(Log.class.getName());
        String logFileName = "logs.log";
        Path logFilePath = pathLogFileParent.resolve(logFileName);
        File logFile = new File(logFilePath.toString());
        try {
            if(logFile.createNewFile())
                System.out.println("Файл для логов создан (отсутствовал)");
            else
                System.out.println("Файл для логов был создан");
        } catch (IOException e) {
            String mesLog = "Проблема при создании файла с логами `isLogFileExist = logFile.createNewFile();`. Результат ";
            System.out.println(mesLog);
            throw new RuntimeException(e);
        }
        FileHandler fileHandler;
        try {
            fileHandler = new FileHandler(logFilePath.toString(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.addHandler(fileHandler);
        SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);
    }

    /**
     * Просто информация о работе
     * @param messages текст информации, можно вводить кусками через запятую,
     *                которые в последствии заменятся на пробел.
     */
    protected void info(String... messages) {
        StringBuilder allMessages = new StringBuilder(System.lineSeparator());
        for (var oneMessage : messages)
            allMessages.append(oneMessage).append(" ");
        logger.info(allMessages.toString());
        alternativeWrite(allMessages.toString());
    }

    /**
     * Предупреждение о том, что важно или опасно
     * @param messages информация о предупреждении, можно вводить кусками через запятую,
     *                которые в последствии заменятся на пробел.
     */
    protected void warning(String... messages) {
        StringBuilder allMessages = new StringBuilder(System.lineSeparator());
        for (var oneMessage : messages)
            allMessages.append(oneMessage).append(" ");
        logger.warning(allMessages.toString());
        alternativeWrite(allMessages.toString());
    }

    /**
     * Проблема, из-за которой следует завершить работу приложения
     * @param messages информация о проблеме, можно вводить кусками через запятую,
     *                которые в последствии заменятся на пробел.
     */
    protected void problem(String... messages) {
        StringBuilder allMessages = new StringBuilder(System.lineSeparator());
        for (var oneMessage : messages)
            allMessages.append(oneMessage).append(" ");
        logger.severe(allMessages.toString());
        alternativeWrite(allMessages.toString());
        //todo код для завершения программы
    }

    /**
     * Альтернативный способ записи лога - дублирование (кроме файла)
     * @param message текст лога
     */
    private void alternativeWrite(String message) {

    }
}
