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

        // 1. ЛЕВАЯ ПАНЕЛЬ
        leftPanel = new JPanel();
//        leftPanel.setSize(
//                new Dimension(
//                        (int)((anoWindow.getWidth()-4)*0.30),
//                        (int)((anoWindow.getHeight()-4)*0.30)
//                )
//        );
        // Панель поиска юзера для открытия чата с ним = поле ввода + кнопка
        searchLoginPanel = new JPanel();
        searchLoginTextArea = new JTextArea("введите логин собеседника");
        searchLoginTextArea.setLineWrap(true);
        searchLoginTextArea.setWrapStyleWord(true);
        searchLoginButton = new JButton("Открыть чат");
        // Панель со всеми логинами
        loginsPanel = new JPanel();
        // добавить скролл
        loginsScroll = new JScrollPane();
        loginsScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        loginsScroll.setViewportView(loginsPanel);



        // 2. ПРАВАЯ ПАНЕЛЬ
        rightPanel = new JPanel();
        // 3. ВЕРХНИЙ элемент - история переписки - состоит из панели с сообщениями, завернутой в скролл
        JPanel historyPanel = new JPanel();
        // история переписки - это JPanel из многих JTextArea, завернутая в JScrollPane
        messageHistoryPanel = new JPanel();
        // добавить скролл
        messageHistoryScroll = new JScrollPane(messageHistoryPanel);

        // 4. НИЖНЯЯ панель (панель с текстовым полем, кнопками)
        bottomPanel = new JPanel();
        messageForSendTextArea = new JTextArea();
        messageForSendTextArea.setLineWrap(true);
        messageForSendTextArea.setWrapStyleWord(true);
        messageAttachButton = new JButton("V");
        messageSendButton = new JButton("Отправить");
        // Обработчики
        searchLoginButton.addActionListener(searchLoginActionListener);
        messageSendButton.addActionListener(sendMessageActionListener);

        // РАЗМЕТКА
        log.info("РАЗМЕТКА панели настроек - добавление всего созданного");
        setLayout(new GridBagLayout());
        // ЛЕВАЯ панель
        add(leftPanel, newConstraints(11,9, 0,0, 3, 9, false, true));
            leftPanel.setLayout(new GridBagLayout());
            leftPanel.add(searchLoginPanel, newConstraints(3,9, 0,0, 3, 2));
                searchLoginPanel.setLayout(new GridBagLayout());
                searchLoginPanel.add(searchLoginTextArea,   newConstraints(3,2, 0,0, 3, 1));
                searchLoginPanel.add(searchLoginButton,     newConstraints(3,2, 0,1, 3, 1));
            leftPanel.add(loginsPanel,      newConstraints(3,9, 0,2, 3, 7, false, true));
            loginsPanel.setLayout(new GridBagLayout());

        // ПРАВАЯ панель
        add(rightPanel, newConstraints(11,9, 3,0, 8, 9, false, true));
            rightPanel.setLayout(new BorderLayout());
            rightPanel.add(historyPanel, BorderLayout.CENTER);
                historyPanel.setLayout(new GridLayout(2, 1)); // 2 строки - 1 для messageHistoryPanel, 1 для bottomPanel
                messageHistoryPanel.setLayout(new BoxLayout(messageHistoryPanel, BoxLayout.Y_AXIS));//new GridBagLayout());
                messageHistoryScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                historyPanel.add(messageHistoryScroll, BorderLayout.CENTER);
            rightPanel.add(bottomPanel, BorderLayout.SOUTH);//,             newConstraints(8,9, 0,7, 8, 2, true, false));
                bottomPanel.setLayout(new GridBagLayout());
                bottomPanel.add(messageForSendTextArea, newConstraints(8,2, 0,0, 5, 2, true, true));
                bottomPanel.add(messageAttachButton,    newConstraints(8,2, 5,0, 1, 2, true, true));
                bottomPanel.add(messageSendButton,      newConstraints(8,2, 6,0, 2, 2, true, true));
    }

    /**
     * Создает и возвращает GridBagConstraints с настроенными свойствами
     * @param wx вес компонента вдоль оси Х ~ ширина панели / ширина ячейки
     * @param wy вес компонента вдоль оси У ~ высота панели / высота ячейки
     * @param gx позиция по оси Х
     * @param gy позиция по оси У
     * @param gw количество занимаемых ячеек по оси Х
     * @param gh количество занимаемых ячеек по оси У
     * @return экземпляр объекта GridBagConstraints
     */
    public GridBagConstraints newConstraints(double wx, double wy, int gx, int gy, int gw, int gh) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.weightx = wx;
        gridBagConstraints.weighty = wy;
        gridBagConstraints.gridx = gx;
        gridBagConstraints.gridy = gy;
        gridBagConstraints.gridwidth = gw;
        gridBagConstraints.gridheight = gh;
        return gridBagConstraints;
    }
    /**
     * Создает и возвращает GridBagConstraints с настроенными свойствами
     * @param wx вес компонента вдоль оси Х ~ ширина панели / ширина ячейки
     * @param wy вес компонента вдоль оси У ~ высота панели / высота ячейки
     * @param gx позиция по оси Х
     * @param gy позиция по оси У
     * @param gw количество занимаемых ячеек по оси Х
     * @param gh количество занимаемых ячеек по оси У
     * @return экземпляр объекта GridBagConstraints
     */
    public GridBagConstraints newConstraints(double wx, double wy, int gx, int gy, int gw, int gh, boolean isFillHor, boolean isFillVert) {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.weightx = wx;
        gridBagConstraints.weighty = wy;
        gridBagConstraints.gridx = gx;
        gridBagConstraints.gridy = gy;
        gridBagConstraints.gridwidth = gw;
        gridBagConstraints.gridheight = gh;
        if (isFillHor) gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        if (isFillVert) gridBagConstraints.fill = GridBagConstraints.VERTICAL;
        return gridBagConstraints;
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
        Integer loginNum = loginsPanel.getComponentCount();
        loginsPanel.add(loginTextArea, newConstraints(3,7, 0, loginNum, 3, 1));
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
        Integer messageNum = messageHistoryPanel.getComponentCount();
        messageHistoryPanel.add(messageTextArea); //, newConstraints(8,7, 0, messageNum, 8, 1));
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
     * Происходит только отправка сообщения в БД, остальное делает прослушивание у обоих собеседников.
     */
    ActionListener sendMessageActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            log.info("sendMessageActionListener = new ActionListener() {..} Начало");
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
            log.info("sendMessageActionListener = new ActionListener() {..} Конец - обработчик отправки сообщения сработал");
        }
    };

    /**
     * Обработчик поиска пользователя для создания новой записи о диалоге.
     * Происходит только отправка записи в БД, остальное делает прослушивание у обоих собеседников.
     */
    ActionListener searchLoginActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            log.info("searchLoginActionListener = new ActionListener() {..} Начало");
            ArrayList<ArrayList<Object>> idsAndLoginsForLoginSearch =
                    anoWindow.getDb().selectIdsAndLoginsForLoginSearch(searchLoginTextArea.getText());
            if (idsAndLoginsForLoginSearch.isEmpty()){
                String messageInfo = "Пользователей, по такому поисковому запросу не найдено. Уточните запрос.";
                JOptionPane.showMessageDialog(null, messageInfo);
                log.warning(messageInfo, "Поисковое значение:", searchLoginTextArea.getText());
                return;
            }
            if (idsAndLoginsForLoginSearch.size() > 1){
                StringBuilder reportSearch = new StringBuilder();
                reportSearch.append("По такому поисковому запросу найдено много пользователей:").append(anoWindow.lSep);
                for (var loginRowObj : idsAndLoginsForLoginSearch) {
                    reportSearch.append(loginRowObj.get(1)).append(anoWindow.lSep);
                }
                log.warning(reportSearch.toString());
                JOptionPane.showMessageDialog(null, reportSearch.append("Уточните запрос."));
                return;
            }
            Integer user1 = anoWindow.getUser().getId();
            Integer user2 = ((Number) idsAndLoginsForLoginSearch.get(0).get(0)).intValue();
            String comment = "'создано в программе'";
            ChatListRow chatListRow = new ChatListRow( user1, user2, comment, anoWindow );
            log.info("searchLoginActionListener = new ActionListener() {..} Конец - обработчик кнопки открытия нового чата сработал");
        }
    };

}