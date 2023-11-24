package gb.study;

import javax.swing.*;
import java.awt.*;

public class AnoWindow extends JFrame {
    protected DB db;

    protected JTabbedPane tabbedPane;
    protected TabChatPanel tabChatPanel;
    protected JPanel tabSettingsPanel;

    public int windowWidth;
    public int windowHeigh;

    public AnoWindow() throws HeadlessException {
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
        // Создание панели для первой вкладки
        tabChatPanel = new TabChatPanel(this);
        // Создание панели для второй вкладки
        tabSettingsPanel = new JPanel();
        tabSettingsPanel.add(new javax.swing.JLabel());
        // Добавление вкладок на панель с вкладками
        tabbedPane.addTab("Чат", tabChatPanel);
        tabbedPane.addTab("Настройки", tabSettingsPanel);

        // Добавление панели с вкладками в главное окно
        add(tabbedPane, BorderLayout.CENTER);

        // Установка операции закрытия окна
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Установка начальной позиции окна по центру экрана
        setLocationRelativeTo(null);
        // Установка видимости окна
        setVisible(true);

        // Запуск прослушивания БД
        db.startListenerDB(this);
    }

}
