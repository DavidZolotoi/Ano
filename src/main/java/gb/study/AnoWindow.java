package gb.study;

import javax.swing.*;
import java.awt.*;

public class AnoWindow extends JFrame {
    private DB db;
    protected DB getDb() {
        return db;
    }
    private User user;
    protected User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    protected JTabbedPane tabbedPane;
    protected TabSettingsPanel tabSettingsPanel;
    protected TabChatPanel tabChatPanel;

    public int windowWidth;
    public int windowHeigh;

    public AnoWindow() {
        // БД
        db = new DB();

        // Получение размеров экрана и окна
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        windowWidth = (int)(screenWidth * 0.50);
        windowHeigh = (int)(screenHeight * 0.75);

        // Установка размеров окна
        setSize(windowWidth, windowHeigh);

        // Заголовок
        setTitle("Ano - лучший в мире чат!");

        // Панель с вкладками JTabbedPane
        tabbedPane = new JTabbedPane();
        // Создание панели для настроек
        tabSettingsPanel = new TabSettingsPanel(this);
        // Создание панели для чата
        tabChatPanel = new TabChatPanel(this);

        // Добавление вкладок на панель с вкладками
        tabbedPane.addTab("Настройки", tabSettingsPanel);
        tabbedPane.addTab("Чат", tabChatPanel);

        // Добавление панели с вкладками в главное окно
        add(tabbedPane);

        // Установка операции закрытия окна
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Установка начальной позиции окна по центру экрана
        setLocationRelativeTo(null);
        // Установка видимости окна
        setVisible(true);
    }
}
