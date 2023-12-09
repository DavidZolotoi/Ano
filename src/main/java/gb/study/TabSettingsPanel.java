package gb.study;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TabSettingsPanel extends JPanel {
    // ОБЩЕЕ окно
    private AnoWindow anoWindow;   //БД в его свойстве
    private User user;
    // ЛЕВАЯ панель
    private JPanel leftPanel;
    private JTextArea loginCommentTextArea;
    private JTextArea loginValueTextArea;
    private JTextArea passCommentTextArea;
    private JPasswordField passValuePasswordField;
    private JButton logingButton;
    private JTextArea loginStatusTextArea;
    // ПРАВАЯ панель
    private JPanel rightPanel;
    private JTextArea countMesForDownTextArea;
    private JTextArea countMesForDownValueTextArea;
    public JTextArea getCountMesForDownValueTextArea() {
        return countMesForDownValueTextArea;
    }

    public TabSettingsPanel(JFrame window) {
        super(new GridLayout(1, 2));
        this.anoWindow = (AnoWindow)window;
        this.user = anoWindow.getUser();

        leftPanel = new JPanel();
        rightPanel = new JPanel();

        // Компоненты для leftPanel
        loginCommentTextArea = new JTextArea("Логин:* ");
        loginCommentTextArea.setEditable(false);
        loginCommentTextArea.setLineWrap(true);
        loginCommentTextArea.setWrapStyleWord(true);
        loginCommentTextArea.setBackground(new Color(0, 0, 0, 0));
        loginValueTextArea = new JTextArea();
        loginValueTextArea.setLineWrap(true);
        loginValueTextArea.setWrapStyleWord(true);

        passCommentTextArea = new JTextArea("Пароль:* ");
        passCommentTextArea.setEditable(false);
        passCommentTextArea.setLineWrap(true);
        passCommentTextArea.setWrapStyleWord(true);
        passCommentTextArea.setBackground(new Color(0, 0, 0, 0));
        passValuePasswordField = new JPasswordField();

        logingButton = new JButton("Войти");
        loginStatusTextArea = new JTextArea("Требуется авторизация");
        loginStatusTextArea.setEditable(false);
        loginStatusTextArea.setLineWrap(true);
        loginStatusTextArea.setWrapStyleWord(true);
        loginStatusTextArea.setBackground(new Color(0, 0, 0, 0));

        // Компоненты для rightPanel
        countMesForDownTextArea = new JTextArea("Количество загружаемых сообщений, при открытии диалога:");
        countMesForDownTextArea.setEditable(false);
        countMesForDownTextArea.setLineWrap(true);
        countMesForDownTextArea.setWrapStyleWord(true);
        countMesForDownTextArea.setBackground(new Color(0, 0, 0, 0));
        countMesForDownValueTextArea = new JTextArea();
        countMesForDownValueTextArea.setLineWrap(true);
        countMesForDownValueTextArea.setWrapStyleWord(true);

        // Обработчики
        logingButton.addActionListener(logingActionListener);

        // РАЗМЕТКА
        int rowCountLeftPanel = 10;
        int colCountLeftPanel = 2;
        leftPanel.setLayout(new GridLayout(rowCountLeftPanel, colCountLeftPanel));
        int rowCountRightPanel = rowCountLeftPanel;
        int colCountRightPanel = colCountLeftPanel;
        rightPanel.setLayout(new GridLayout(rowCountRightPanel, colCountRightPanel));
        // Здесь можно сделать метод добавления строк или кол-ва пустых.
        // Работает непросто. Java заранее считает кол-во добавляемых элементов,
        // если их кол-во не умещается в кол-во строк, то расставляет построчно слева-направо, сверху-вниз
        add(leftPanel);
            leftPanel.add(loginCommentTextArea);        leftPanel.add(loginValueTextArea);
            leftPanel.add(passCommentTextArea);         leftPanel.add(passValuePasswordField);
            leftPanel.add(logingButton);                 leftPanel.add(loginStatusTextArea);
            leftPanel.add(new JLabel());                leftPanel.add(new JLabel());
            leftPanel.add(new JLabel());                leftPanel.add(new JLabel());
            leftPanel.add(new JLabel());                leftPanel.add(new JLabel());
            leftPanel.add(new JLabel());                leftPanel.add(new JLabel());
            leftPanel.add(new JLabel());                leftPanel.add(new JLabel());
            leftPanel.add(new JLabel());                leftPanel.add(new JLabel());
            leftPanel.add(new JLabel());                leftPanel.add(new JLabel());
//            leftPanel.add(new JButton("Login"));leftPanel.add(new JTextArea("First Name:"));
//            leftPanel.add(new JTextArea("Last Name:"));leftPanel.add(new JTextArea("Mail:"));
//            leftPanel.add(new JTextArea("Phone:"));leftPanel.add(new JTextArea("Comment:"));
        add(rightPanel);
            rightPanel.add(countMesForDownTextArea);    rightPanel.add(countMesForDownValueTextArea);
            rightPanel.add(new JLabel());               rightPanel.add(new JLabel());
            rightPanel.add(new JLabel());               rightPanel.add(new JLabel());
            rightPanel.add(new JLabel());               rightPanel.add(new JLabel());
            rightPanel.add(new JLabel());               rightPanel.add(new JLabel());
            rightPanel.add(new JLabel());               rightPanel.add(new JLabel());
            rightPanel.add(new JLabel());               rightPanel.add(new JLabel());
            rightPanel.add(new JLabel());               rightPanel.add(new JLabel());
            rightPanel.add(new JLabel());               rightPanel.add(new JLabel());
            rightPanel.add(new JLabel());               rightPanel.add(new JLabel());

    }

    // Обработчик кнопки входа в систему
    final ActionListener logingActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Взаимодействие с БД - начало
            // 0. Идентификация юзера (логин) и подтверждение (пароль), а также получение его id из БД
            // При создании пользователя у него создадутся два словаря:
            // - словарь id->запись о диалоге (1 запрос к записям о диалогах)
            // - словарь логин->запись о диалоге (1 запрос к юзерам) - используем его
            user = new User(    //todo прочитать из вкладки настроек?
                            loginValueTextArea.getText(),
                            new String(passValuePasswordField.getPassword()),
                            "",
                            "",
                            "",
                            "",
                            "",
                            anoWindow
                    );
            anoWindow.setUser(user);
            // 1. Добавить все disputerTextArea и повесить на них обработчики
            anoWindow.tabChatPanel.updateDisputerLoginsPanel();
            // 2. Запустить все прослушивания
            user.startListening(anoWindow);
        }
    };
}
