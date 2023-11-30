package gb.study;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class TabSettingsPanel extends JPanel {
    // ОБЩЕЕ окно
    private AnoWindow anoWindow;   //БД в его свойстве
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

    public TabSettingsPanel(JFrame window) {
        super(new GridLayout(1, 2));
        this.anoWindow = (AnoWindow)window;

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
            rightPanel.add(new JLabel("Тема:"));    rightPanel.add(new JTextArea());
            rightPanel.add(new JLabel());               rightPanel.add(new JLabel());
            rightPanel.add(new JLabel());               rightPanel.add(new JLabel());
            rightPanel.add(new JLabel());               rightPanel.add(new JLabel());
            rightPanel.add(new JLabel());               rightPanel.add(new JLabel());
            rightPanel.add(new JLabel());               rightPanel.add(new JLabel());
            rightPanel.add(new JLabel());               rightPanel.add(new JLabel());
            rightPanel.add(new JLabel());               rightPanel.add(new JLabel());
            rightPanel.add(new JLabel());               rightPanel.add(new JLabel());

    }

    ActionListener logingActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Взаимодействие с БД (может быть стоить опустить сюда по ближе ее создание)
            // 0. Идентификация юзера (логин) и подтверждение (пароль), а также получение его id из БД
            //todo прочитать из вкладки настроек
            anoWindow.setUser(
                    new User(
                            loginValueTextArea.getText(),//"Sergey",
                            new String(passValuePasswordField.getPassword()), // "1111",
                            "",
                            "",
                            "",
                            "",
                            "",
                            anoWindow
                    )
            );
            // 1. Загрузка списка диалогов с этим юзером
            // 1) рассчитать id собеседников
            ArrayList<Integer> idInterlocutor = new ArrayList<>();
            for (var chatListRow : anoWindow.getUser().getChatListRows()) {
                idInterlocutor.add(
                        chatListRow.getUserIdMin() + chatListRow.getUserIdMax() - anoWindow.getUser().getId()
                );
            }
            // 2) по id собеседников получить их логины
            var loginInterlocutorTableObj = anoWindow.getDb().selectLoginsForUserIds(idInterlocutor);
            ArrayList<String> loginInterlocutor = new ArrayList<>();
            for (ArrayList<Object> loginRow : loginInterlocutorTableObj) {
                loginInterlocutor.add((String) loginRow.get(0));
            }
            // 3) добавить JTextArea с его логином
            for (var login : loginInterlocutor) {
                System.out.println(loginInterlocutor);
                JTextArea loginTextArea = new JTextArea(login);
                loginTextArea.setEditable(false);
                loginTextArea.setLineWrap(true);
                loginTextArea.setWrapStyleWord(true);
                anoWindow.tabChatPanel.leftPanel.add(loginTextArea);
                // 4) повесить на него обработчик клика - загрузка последних N сообщений диалога
                loginTextArea.addMouseListener(new MouseAdapter() {
                    public void mouseReleased(MouseEvent e) {
                        anoWindow.tabChatPanel.downloadAndPasteLastMessagesToHistoryPanel();
                    }
                });
            }
            // 2. Запуск прослушивания БД
            anoWindow.getDb().startListenerChat(anoWindow);
        }
    };
}
