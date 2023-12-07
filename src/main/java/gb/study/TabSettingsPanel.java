package gb.study;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

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

    final ActionListener logingActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Взаимодействие с БД - начало
            // 0. Идентификация юзера (логин) и подтверждение (пароль), а также получение его id из БД
            //todo прочитать из вкладки настроек
            anoWindow.setUser(
                    new User(
                            loginValueTextArea.getText(),
                            new String(passValuePasswordField.getPassword()),
                            "",
                            "",
                            "",
                            "",
                            "",
                            anoWindow
                    )
            );
            user = anoWindow.getUser();
            // 1. Загрузка списка диалогов с этим юзером, с повешанными обработчиками
            // При создании пользователя у него создадутся два словаря:
            // - словарь id->запись о диалоге (1 запрос к записям о диалогах)
            // - словарь логин->запись о диалоге (1 запрос к юзерам) - используем его
            for (var disputerLoginAndChatListRow :
                    user.getDisputerLoginsAndChatListRows().entrySet()) {
                String disputerLogin = disputerLoginAndChatListRow.getKey();
                ChatListRow disputerChatListRow = disputerLoginAndChatListRow.getValue();
                System.out.println(disputerLogin + " " + disputerChatListRow.getTableName());
                // 1.1. добавить JTextArea с его логином
                JTextArea loginTextArea = new JTextArea(disputerLogin);
                loginTextArea.setEditable(false);
                loginTextArea.setLineWrap(true);
                loginTextArea.setWrapStyleWord(true);
                anoWindow.tabChatPanel.leftPanel.add(loginTextArea);
                // 1.2. обработчик клика по чату JTextArea:
                loginTextArea.addMouseListener(new MouseAdapter() {
                    public void mouseReleased(MouseEvent e) {
                        JTextArea source = (JTextArea) e.getSource();
                        Integer idDisputer = anoWindow.getUser().calculateDisputerId(disputerChatListRow);
                        //1.2.-1. проверка - если активный чат после клика не поменялся, то выходим
                        if (!user.isChangeActiveChatListRow(source)) return;
                        //1.2.0. Очистить чат и загрузить содержимое словаря
                        anoWindow.tabChatPanel.clearMessageHistoryPanel();
                        anoWindow.tabChatPanel.addAndShowMessagesFromList(
                                new ArrayList<>(user.getChats().get(idDisputer).getMessages().values())
                        );
                        //1.2.1. Загрузка сообщений из БД в хранилище (конкретный чат из словаря) юзера и на окно
                        //todo можно попробовать переделать на метод внутри tabChatPanel
                        user.getChats().get(idDisputer).downloadLastMessages(
                                disputerChatListRow,
                                Integer.parseInt(countMesForDownValueTextArea.getText()),
                                anoWindow
                        );
                    }
                });
            }
            // 2. Запустить асинхронное прослушивание этих каналов:
            CompletableFuture<Void> future =
                    listenerNewMessageAsync(
                            new ArrayList<>(user.getDisputerLoginsAndChatListRows().values()),
                            anoWindow
                    );
            // 3. Запуск прослушивания сообщения от неизвестного???? Думать - видимо триггер к записям
        }

        /**
         * Асинхронный метод прослушивания чатов
         * @param chatListRows записи о диалогах, которые необходимо прослушивать
         * @param anoWindow главное окно со всеми его свойствами,
         *                  в том числе с БД, которая необходима для работы метода
         * @return
         */
        private CompletableFuture<Void> listenerNewMessageAsync(ArrayList<ChatListRow> chatListRows, AnoWindow anoWindow) {
            return CompletableFuture.runAsync(() -> {
                System.out.println("Прослушивание каналов запущено.");
                anoWindow.getDb().startListenerChat(chatListRows, anoWindow);
            });
        }
    };

}
