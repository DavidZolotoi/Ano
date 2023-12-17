package gb.study;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Map;
;

public class TabChatPanel extends JPanel {
    // ОБЩЕЕ окно
    private AnoWindow anoWindow;   //БД в его свойстве
    private final Log log;
    private User user;
    
    // ЛЕВАЯ панель
    public JPanel leftPanel;    //todo переделать в private
    private JPanel searchLoginPanel;
    private JTextArea searchLoginTextArea;
    private JButton searchLoginButton;
    private JPanel loginsPanel;
    private JScrollPane loginsScroll;
    public JPanel getLoginsPanel() {
        return loginsPanel;
    }

    // ПРАВАЯ панель
    private JPanel rightPanel;
    // верхняя панель ПРАВОЙ панели
    private JScrollPane messageHistoryScroll;
    private JPanel messageHistoryPanel;
    //  todo нужна ли тут коллекция/словарь сообщений или элементов JTextArea? вроде и без нее норм
    // нижняя панель ПРАВОЙ панели
    private JPanel bottomPanel;
    private JTextArea messageForSendTextArea;
    private JButton messageAttachButton;
    private JButton messageSendButton;
    //todo нет ни одного геттера и сеттера - подумать нужны ли?

    public TabChatPanel(JFrame window) {
        super();
        this.anoWindow = (AnoWindow)window;
        this.log = this.anoWindow.log;
        //user = anoWindow.getUser(); //todo видимо надо отсюда удалить, потому юзер распознается позже

        // 0. САМО ОКНО
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        // 1. ЛЕВАЯ ПАНЕЛЬ
        leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setSize(
                new Dimension(
                        (int)((anoWindow.getWidth()-4)*0.30),
                        (int)((anoWindow.getHeight()-4)*0.30)
                )
        );
        // Панель поиска юзера для открытия чата с ним = поле ввода + кнопка
        searchLoginPanel = new JPanel();
        searchLoginPanel.setLayout(new BoxLayout(searchLoginPanel, BoxLayout.Y_AXIS));
        searchLoginPanel.setSize(
                new Dimension(
                        (int)((anoWindow.getWidth()-8)*0.30),
                        0
                )
        );
        searchLoginTextArea = new JTextArea("введите логин собеседника");
        searchLoginTextArea.setLineWrap(true);
        searchLoginTextArea.setWrapStyleWord(true);
        searchLoginTextArea.setSize(
                new Dimension(
                        (int)((anoWindow.getWidth()-10)*0.30),
                        0
                )
        );
        searchLoginButton = new JButton("Открыть чат");
        // Панель со всеми логинами
        loginsPanel = new JPanel();
        loginsPanel.setLayout(new BoxLayout(loginsPanel, BoxLayout.Y_AXIS));
        // добавить скролл
        loginsScroll = new JScrollPane();
        loginsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        loginsScroll.setViewportView(loginsPanel);



        // 2. ПРАВАЯ ПАНЕЛЬ
        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setSize(
                new Dimension(
                        (int)((anoWindow.getWidth()-4)*0.70),
                        (int)((anoWindow.getHeight()-4)*0.70)
                )
        );

        // 3. ВЕРХНИЙ элемент - история переписки - это JPanel из многих JTextArea, завернутая в JScrollPane
        messageHistoryPanel = new JPanel();
        messageHistoryPanel.setLayout(new BoxLayout(messageHistoryPanel, BoxLayout.Y_AXIS));
        // добавить скролл
        messageHistoryScroll = new JScrollPane();
        messageHistoryScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        messageHistoryScroll.setViewportView(messageHistoryPanel);

        // 4. НИЖНЯЯ панель (панель с текстовым полем, кнопками)
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.setSize(
                new Dimension(
                        (int)(rightPanel.getWidth()-2),
                        (int)(rightPanel.getHeight()-2)
                )
        );

        // Содержимое нижней панели: ввода текста + 2 кнопки
        messageForSendTextArea = new JTextArea();
        messageForSendTextArea.setSize(
                new Dimension(
                        (int)((bottomPanel.getWidth()-2)*0.70),
                        (int)((bottomPanel.getHeight()-2)*0.70)
                )
        );
        // Настройки для элементов
        messageForSendTextArea.setLineWrap(true);
        messageForSendTextArea.setWrapStyleWord(true);
        messageAttachButton = new JButton("V");
        messageAttachButton.setSize(
                new Dimension(
                        (int)((bottomPanel.getWidth()-2)*0.10),
                        (int)((bottomPanel.getHeight()-2)*0.10)
                )
        );
        messageSendButton = new JButton("Отправить");
        messageSendButton.setSize(
                new Dimension(
                        (int)((bottomPanel.getWidth()-2)*0.20),
                        (int)((bottomPanel.getHeight()-2)*0.20)
                )
        );

        // Обработчики
        searchLoginButton.addActionListener(searchLoginActionListener);
        messageSendButton.addActionListener(sendMessageActionListener);

        // РАЗМЕТКА
        // ЛЕВАЯ панель
        add(leftPanel);
            leftPanel.add(searchLoginPanel);
                searchLoginPanel.add(searchLoginTextArea);
                searchLoginPanel.add(searchLoginButton);
            leftPanel.add(loginsPanel);

        // ПРАВАЯ панель
        add(rightPanel);
            // Добавление истории сообщений
            rightPanel.add(messageHistoryScroll);
            // Добавить на историю сообщений сами сообщения - метод, вызывающийся по клику на чат
            // Добавление нижней панели с инструментами для отправки письма
            rightPanel.add(bottomPanel);
                // Добавление поля для ввода в нижнюю панель
                bottomPanel.add(messageForSendTextArea);
                // Добавление кнопки для загрузки вложений в нижнюю панель
                bottomPanel.add(messageAttachButton);
                // Добавление кнопки для отправки письма в нижнюю панель
                bottomPanel.add(messageSendButton);
    }

    /**
     * Обновляет панель со всеми логинами в соответствии с хранилищами пользователя
     * (Добавляет чат с логином и вешает на него обработчик)
     */
    protected void updateDisputerLoginsPanel(){
        log.info("updateDisputerLoginsPanel() Начало");
        user = anoWindow.getUser();//только на этом моменте у anoWindow есть полноценный пользователь со своими словарями
        var disputerLoginsAndChatListRows = user.getDisputerLoginsAndChatListRows().entrySet();
        if (disputerLoginsAndChatListRows == null) {
            log.warning("updateDisputerLoginsPanel() Конец - словарь disputerLoginsAndChatListRows = null");
            return;
        }
        for (var disputerLoginAndChatListRow : disputerLoginsAndChatListRows) {
            addNewDisputerAndListener(disputerLoginAndChatListRow);
        }
        log.info("updateDisputerLoginsPanel() Конец - на панель собеседников добавлены все логины",
                "и на них повешаны обработчики");
    }
    /**
     * Добавляет диалог с новым логином и вешает на него обработчик
     * @param disputerLoginAndChatListRow информация о новом чате
     */
    protected void addNewDisputerAndListener(Map.Entry<String, ChatListRow> disputerLoginAndChatListRow) {
        log.info("addNewDisputerAndListener(..) Начало");
        if (disputerLoginAndChatListRow == null) {
            log.warning("addNewDisputerAndListener(..) Конец - словарь disputerLoginsAndChatListRows = null");
            return;
        }
        String disputerLogin = disputerLoginAndChatListRow.getKey();
        JTextArea loginTextArea = addDisputerLoginTextArea(disputerLogin);
        loginAddMouseListener(loginTextArea);
        log.info("addNewDisputerAndListener(..) Конец - добавлен ", disputerLogin, "и повешан обработчик - двумя разными методами");
    }
    /**
     * Добавляет на панель логинов компонент с логином,
     * @param disputerLogin логин пользователя, который необходимо добавить
     * @return ссылку на добавленный компонент
     */
    protected JTextArea addDisputerLoginTextArea(String disputerLogin) {
        log.info("addDisputerLoginTextArea(..) Начало");
        if (disputerLogin == null) {
            log.warning("addDisputerLoginTextArea(..) Конец - disputerLogin для добавления = null");
            return null;
        }
        JTextArea loginTextArea = new JTextArea(disputerLogin);
        loginTextArea.setEditable(false);
        loginTextArea.setLineWrap(true);
        loginTextArea.setWrapStyleWord(true);
        anoWindow.tabChatPanel.getLoginsPanel().add(loginTextArea);
        log.info("addDisputerLoginTextArea(..) Конец - логин добавлен на панель");
        return loginTextArea;
    }
    /**
     * Вешает обработчик на клик по компоненту с пользователем (диалогу)
     * @param loginTextArea ссылка на компонент с логином пользователя (диалог)
     */
    private void loginAddMouseListener(JTextArea loginTextArea) {
        log.info("loginAddMouseListener(..) Начало");
        if (loginTextArea == null) {
            log.warning("loginAddMouseListener(..) Конец - loginTextArea для добавления = null");
            return;
        }
        loginTextArea.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                log.info("loginTextArea.addMouseListener(..) Начало");
                String disputerLogin = loginTextArea.getText();
                if (
                    user.getDisputerLoginsAndChatListRows() == null ||
                    !user.getDisputerLoginsAndChatListRows().containsKey(disputerLogin)
                ){
                    log.problem("Ситуация, которая возможна только в теории, на практике такого не должно быть.",
                            "Отсутствует словарь ''login->chatlist'' или логин (по которому кликнули) в этом словаре пользователя.");
                }
                ChatListRow chatListRowClick = user.getDisputerLoginsAndChatListRows().get(disputerLogin);
                Integer disputerId = user.calculateDisputerId(chatListRowClick);
                if (user.isActiveChatListRow(chatListRowClick)) return;
                user.setActiveChatListRow(chatListRowClick);
                if ( user.getChats() == null || !user.getChats().containsKey(disputerId) ){
                    log.problem("Ситуация, которая возможна только в теории, на практике такого не должно быть.",
                            "Отсутствует словарь ''id->chat'' или id (по которому кликнули) в этом словаре пользователя.");
                }
                user.getChats().get(disputerId).parseLastMessages(
                        chatListRowClick,
                        anoWindow.tabSettingsPanel.parseCountMessagesForDownload(),
                        anoWindow
                );
                addAndShowMessagesFromList(new ArrayList<>(user.getChats().get(disputerId).getMessages().values()));
                log.info("loginTextArea.addMouseListener(..) Конец - описание обработчика-метода в аргументе");
            }
        });
        log.info("loginAddMouseListener(..) Конец - описания обработчика на логин");
    }

    /**
     * Добавление нового сообщения в историю сообщений.
     * @param message сообщение, которое нужно добавить
     */
    protected void addAndShowNewMessage(Message message) {
        log.info("addAndShowNewMessage(..) Начало");
        JTextArea messageTextArea = new JTextArea(message.show());
        messageTextArea.setEditable(false);
        messageTextArea.setLineWrap(true);
        messageTextArea.setWrapStyleWord(true);

        int marginLeftRightBig = (int)(anoWindow.getWidth()*0.25);
        int marginLeftRightSmall = (int)(anoWindow.getWidth()*0.01);
        int marginLeft = marginLeftRightBig;        // если сообщение от автора
        int marginRight = marginLeftRightSmall;     // иначе
        if (message.isMarginRight) {
            marginLeft = marginLeftRightSmall;
            marginRight = marginLeftRightBig;
        }
        messageTextArea.setMargin(new Insets(0, marginLeft, 0, marginRight));

        messageHistoryPanel.add(messageTextArea);
        anoWindow.revalidate();
        anoWindow.repaint();
        //todo добавить прокрутку колесиком вниз истории сообщений
        log.info("addAndShowNewMessage(..) Конец - сообщение добавлено на панель");
    }
    /**
     * Добавляет уже загруженные сообщения из словаря на свое место в историю сообщений.
     * Вызывает в цикле метод добавления одного сообщения
     * @param messages коллекция сообщений, которые необходимо добавить
     */
    protected void addAndShowMessagesFromList(ArrayList<Message> messages) {
        log.info("addAndShowMessagesFromList(..) Начало");
        if (messages.isEmpty()) return;
        clearMessageHistoryPanel();
        for (var message : messages) {
            addAndShowNewMessage(message);
        }
        //loginsPanelNotice(String loginValue);
        messageHistoryPanel.revalidate();
        messageHistoryPanel.repaint();
        log.info("addAndShowMessagesFromList(..) Конец - сообщения добавлены и перерисованы");
    }
    //todo если делать пометку звездочкой, то и снимать ее надо при прочтении
    protected void loginsPanelNotice(String loginValue){
        for (var loginTextArea : loginsPanel.getComponents()) {
            if(((JTextArea) loginTextArea).getText().replace("*", "").equals(loginValue)){
                ((JTextArea) loginTextArea).setText(loginValue + "*");
            }
        }

    }

    /**
     * Очистить с панели истории сообщений все элементы JTextArea
     */
    protected void clearMessageHistoryPanel(){
        log.info("clearMessageHistoryPanel() Начало");
        Component[] components = messageHistoryPanel.getComponents();
        if (components == null) {
            log.warning("Панель сообщений была пуста (не имеет компонентов)");
            return;
        }
        for (Component component : components) {
            if (component instanceof JTextArea) {
                messageHistoryPanel.remove(component);
            }
        }
        log.info("clearMessageHistoryPanel() Конец - панель истории сообщений очищена");
    }

    /**
     * Обработчик отправки письма в БД, сам запрос спрятан внутри метода.
     * Происходит только отправка в БД, остальное делает прослушивание.
     */
    ActionListener sendMessageActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Integer authorId = anoWindow.getUser().getId();
            String mesContent = messageForSendTextArea.getText();
            String mesComment = "Комментарий к сообщению ";
            ChatListRow activeChatListRow = anoWindow.getUser().getActiveChatListRow();
            new Message(
                authorId,
                mesContent,
                mesComment,
                activeChatListRow,
                anoWindow
            );
            messageForSendTextArea.setText("");
            messageHistoryPanel.revalidate();
            messageHistoryPanel.repaint();
            messageForSendTextArea.requestFocus();
        }
    };

    /**
     * Обработчик поиска пользователя для создания диалога
     */
    ActionListener searchLoginActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //todo при регистрации пользователя, надо в базу записывать все логины с маленькими буквами!!!
            ArrayList<ArrayList<Object>> idsAndLoginsForLoginSearch =
                    anoWindow.getDb().selectIdsAndLoginsForLoginSearch(searchLoginTextArea.getText());
            if (idsAndLoginsForLoginSearch.isEmpty()){
                JOptionPane.showMessageDialog(
                        null,
                        "Пользователей, по такому поисковому запросу не найдено. Уточните запрос."
                );
                return;
            }
            if (idsAndLoginsForLoginSearch.size() > 1){
                StringBuilder reportSearch = new StringBuilder();
                reportSearch
                        .append("По такому поисковому запросу найдено много пользователей:")
                        .append(System.lineSeparator());
                for (var loginRowObj : idsAndLoginsForLoginSearch) {
                    reportSearch.append(loginRowObj.get(1)).append(System.lineSeparator());
                }
                JOptionPane.showMessageDialog(null, reportSearch.append("Уточните запрос."));
                return;
            }
            //Проверку в теории можно убрать, но что-то не хочется :-)
            if (idsAndLoginsForLoginSearch.size() == 1){
                //  todo подумать над тем, чтоб убрать эту логику в user или растворить в user.disputersUpdate()
                // зарегистрировать с ним новую запись о диалоге
                // => улетит уведомление
                // => прилетит уведомление => обработается
                //конструктор проверит, если такой записи нет в БД, то создаст ее (min/max тоже зашито в конструктор).
                ChatListRow chatListRow = new ChatListRow(
                        anoWindow.getUser().getId(),                            //user1
                        (Integer) idsAndLoginsForLoginSearch.get(0).get(0),     //user2
                        "'создано в программе'",     //comment
                        anoWindow
                );
            }
        }
    };

}