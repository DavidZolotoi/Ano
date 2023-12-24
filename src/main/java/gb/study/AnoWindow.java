package gb.study;

import javax.swing.*;
import java.awt.*;

public class AnoWindow extends JFrame {
    protected java.io.File jsonFile;
    protected Log log;
    protected String lSep = System.lineSeparator();

    private DB db;
    protected DB getDb() {
        return db;
    }
    public void setDb(DB db) {
        this.db = db;
    }

    private User user;
    protected User getUser() {
        return user;
    }
    protected void setUser(User user) {
        this.user = user;
    }

    // ОБЩЕЕ
    private JPanel anoPanel;
    private JTabbedPane tabbedPane;
    protected JPanel tabSettings;
    protected JPanel tabChat;
    // tabSettings
    protected JPanel tsLeftPanel;
    protected JPanel tsRightPanel;
    protected JTextArea dbStatusTextArea;
    protected JButton dbDefaultButton;
    protected JButton dbJsonButton;
    protected JTextArea loginValueTextArea;
    protected JTextArea loginStatusTextArea;
    protected JPasswordField passValuePasswordField;
    protected JButton loginButton;
    protected JTextArea countMesForDownValueTextArea;
    //tabChat
    protected JPanel tcLeftPanel;
    protected JPanel tcRightPanel;
    protected JPanel searchLoginPanel;
    protected JTextArea searchLoginTextArea;
    protected JButton searchLoginButton;
    protected JPanel logins;
    protected JScrollPane loginsScrollPane;
    protected JPanel loginsPanel;
    protected JPanel messageHistory;
    protected JScrollPane messageHistoryScrollPane;
    protected JPanel messageHistoryPanel;
    protected JPanel bottomPanel;
    protected JTextArea messageForSendTextArea;
    protected JButton messageAttachButton;
    protected JButton messageSendButton;

    protected TabSettingsPanel tabSettingsPanel;
    protected TabChatPanel tabChatPanel;

    public Integer windowWidth;
    public Integer windowHeigh;

    public AnoWindow(Log log) {
        log.info("AnoWindow(Log log) Начало");
        this.log = log;

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        Integer screenWidth = screenSize.width;
        Integer screenHeight = screenSize.height;
        this.windowWidth = (int)(screenWidth * 0.50);
        this.windowHeigh = (int)(screenHeight * 0.75);

        this.log.info(
                "Размеры экрана:", screenWidth.toString(), "x", screenHeight.toString(),
                ".", lSep, "Установка размеров окна:", this.windowWidth.toString(), "x", this.windowHeigh.toString(),
                ".", lSep, "Установка заголовка."
        );
        setSize(this.windowWidth, this.windowHeigh);
        setTitle("Ano - лучший в мире чат!");

        this.log.info("Добавление основной панели на окно");
        //this.tabSettingsPanel = new TabSettingsPanel(this);
        //tabSettings.add(tabSettingsPanel);
        //this.tabChatPanel = new TabChatPanel(this);
        //tabChat.add(tabChatPanel);
        setContentPane(anoPanel);

        this.log.info("Установка операции закрытия окна, начальной позиции окна (по центру экрана) и видимости окна");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        this.log.info("AnoWindow(Log log) Конец");
    }

}
