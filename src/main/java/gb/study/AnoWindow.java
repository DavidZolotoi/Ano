package gb.study;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

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
    private JTextArea registerTextArea;
    private JTextArea registerLoginTextArea;
    private JTextArea passValue1TextArea;
    private JTextArea passValue2TextArea;
    private JButton registerButton;

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
        setContentPane(anoPanel);

        // Обработчики
        dbDefaultButton.addActionListener(dbDefaultActionListener);
        dbJsonButton.addActionListener(dbJsonActionListener);
        loginButton.addActionListener(logingActionListener);
        registerButton.addActionListener(registerActionListener);
        searchLoginButton.addActionListener(searchLoginActionListener);
        messageSendButton.addActionListener(sendMessageActionListener);

        this.log.info("Установка операции закрытия окна, начальной позиции окна (по центру экрана) и видимости окна");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);

        this.log.info("AnoWindow(Log log) Конец");
    }

    /*** ОБРАБОТЧИКИ ПАНЕЛИ НАСТРОЕК ***/
    /**
     * Обработчик кнопки подключения к базе данных по умолчанию
     */
    final ActionListener dbDefaultActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            log.info("Обработчик кнопки dbDefaultButton Начало");
            DB.settingsFilePath = "E:\\Csharp\\GB\\Ano\\Anoswing\\settings_past_the_git.json";
            db = new DB(AnoWindow.this);
            dbStatusTextArea.setText("Соединение по умолчанию установлено.");
            log.info("Обработчик кнопки dbDefaultButton Конец - База данных по умолчанию создана.");
        }
    };
    /**
     * Обработчик кнопки подключения к базе данных, к JSON-настройкам которых указал пользователь
     */
    final ActionListener dbJsonActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            log.info("Обработчик кнопки dbJsonButton Начало");
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                DB.settingsFilePath = fileChooser.getSelectedFile().getAbsolutePath();
                log.info("Указан файл JSON:", DB.settingsFilePath);
                db = new DB(AnoWindow.this);
                dbStatusTextArea.setText("Соединение установлено.");
                log.info("Обработчик кнопки dbJsonButton Конец - База данных по указанному файлу JSON создана.");
            }
            else {
                log.warning("Пользователь намеревался, но не выбрал файл для подключения к БД");
            }
        }
    };
    /**
     * Обработчик кнопки входа
     * В результате имеются: база данных, пользователь со своими словарями и прослушивания.
     */
    final ActionListener logingActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            log.info("Обработчик кнопки loginButton Начало");
            if(AnoWindow.this.db == null){
                //dbDefaultButton.actionPerformed(new ActionEvent(dbDefaultButton, 0, null));
                dbDefaultButton.doClick();
            }
            if(loginValueTextArea.getText().equals("") || loginValueTextArea.getText()==null){
                log.problem("Не введен логин пользователя");
                JOptionPane.showMessageDialog(null, "Не введен логин пользователя");
                return;
            }
            User user = null;
            try {
                user = User.checkLoginPasswordAndParseUserFromDB(
                        loginValueTextArea.getText(),
                        passValuePasswordField,
                        AnoWindow.this
                );
            } catch (IllegalArgumentException argExp) {
                log.problem(argExp.getMessage());
            }
            AnoWindow.this.user = user;
            loginStatusTextArea.setText("Авторизирован");
            log.info("Пользователь проверен и создан, словари загружены, а сообщения - нет.");
            updateDisputerLoginsPanel();
            user.startListening();
            log.info("Обработчик кнопки logingButton Конец");
        }
    };
    /**
     * Обработчик кнопки регистрации нового пользователя
     */
    final ActionListener registerActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (registerLoginTextArea.getText().isEmpty() ||
                    passValue1TextArea.getText().isEmpty() ||
                    passValue2TextArea.getText().isEmpty()
            ){
                String messageInfo = "Заполните все 3 поля для регистрации: Логин, Пароль и снова Пароль. И попробуйте еще.";
                JOptionPane.showMessageDialog(null, messageInfo);
                log.warning(messageInfo);
                log.info("Обработчик кнопки registerButton Конец - прервано на проверке");
                return;
            }
            if (!passValue1TextArea.getText().equals(passValue2TextArea.getText())){
                String messageInfo = "Пароли не совпадают. Пожалуйста исправьте и попробуйте еще.";
                JOptionPane.showMessageDialog(null, messageInfo);
                log.warning(messageInfo);
                log.info("Обработчик кнопки registerButton Конец - прервано на проверке");
                return;
            }
            if(AnoWindow.this.db == null){
                //dbDefaultButton.actionPerformed(new ActionEvent(dbDefaultButton, 0, null));
                dbDefaultButton.doClick();
            }
            String login = registerLoginTextArea.getText();
            ArrayList<ArrayList<Object>> idsAndLoginsForLoginSearch = db.selectIdsAndLoginsForLoginSearch(login);
            if (idsAndLoginsForLoginSearch.isEmpty()){
                String passw = passValue1TextArea.getText();
                User user = new User(login, passw, AnoWindow.this);
                passw = "";
                user = null;
                log.warning("Создан новый пользователь: ", login);
                login = "";
                log.info("Обработчик кнопки registerButton Конец - пользователь создан и зарегистрирован");
                return;
            }
            if (idsAndLoginsForLoginSearch.size() >= 1){
                String messageInfo = "Пользователь, с таким логином уже существует. Придумайте другой логин.";
                JOptionPane.showMessageDialog(null, messageInfo);
                log.warning(messageInfo, "Вбиваемый для регистрации логин:", login);
                log.info("Обработчик кнопки registerButton Конец - прервано на проверке");
            }
        }
    };

    /**
     * Распознает число, введенное в поле количества последних загружаемых сообщений
     * @return значение (если возникли исключения, то значение по умолчанию)
     */
    public Integer parseCountMessagesForDownload(){
        log.info("parseCountMesForDownload() Начало");
        Integer countMesForDownload = 20;
        try {
            countMesForDownload = Integer.parseInt(this.countMesForDownValueTextArea.getText());
        }catch (NullPointerException | NumberFormatException e){
            log.warning("В поле с количеством загружаемых сообщений некорректное значение.",
                    e.getMessage(),
                    "Будет установление значение по умолчанию.");
        }
        log.info("parseCountMesForDownload() Конец - countMesForDownload =", countMesForDownload.toString());
        return countMesForDownload;
    }

    /*** ОБРАБОТЧИКИ ПАНЕЛИ ЧАТА ***/

    /**
     * Обработчик отправки письма в БД, сам запрос спрятан внутри метода.
     * Происходит только отправка сообщения в БД, остальное делает прослушивание у обоих собеседников.
     */
    ActionListener sendMessageActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            log.info("sendMessageActionListener = new ActionListener() {..} Начало");
            Integer authorId = AnoWindow.this.user.getId();
            String mesContent = messageForSendTextArea.getText();
            String mesComment = "Комментарий к сообщению ";
            ChatListRow activeChatListRow = AnoWindow.this.user.getActiveChatListRow();
            new Message(
                    authorId,
                    mesContent,
                    mesComment,
                    activeChatListRow,
                    AnoWindow.this
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
                    AnoWindow.this.db.selectIdsAndLoginsForLoginSearch(searchLoginTextArea.getText());
            if (idsAndLoginsForLoginSearch.isEmpty()){
                String messageInfo = "Пользователей, по такому поисковому запросу не найдено. Уточните запрос.";
                JOptionPane.showMessageDialog(null, messageInfo);
                log.warning(messageInfo, "Поисковое значение:", searchLoginTextArea.getText());
                return;
            }
            if (idsAndLoginsForLoginSearch.size() > 1){
                StringBuilder reportSearch = new StringBuilder();
                reportSearch.append("По такому поисковому запросу найдено много пользователей:").append(AnoWindow.this.lSep);
                for (var loginRowObj : idsAndLoginsForLoginSearch) {
                    reportSearch.append(loginRowObj.get(1)).append(AnoWindow.this.lSep);
                }
                log.warning(reportSearch.toString());
                JOptionPane.showMessageDialog(null, reportSearch.append("Уточните запрос."));
                return;
            }
            Integer user1 = AnoWindow.this.user.getId();
            Integer user2 = ((Number) idsAndLoginsForLoginSearch.get(0).get(0)).intValue();
            String comment = "'создано в программе'";
            ChatListRow chatListRow = new ChatListRow( user1, user2, comment, AnoWindow.this );
            log.info("searchLoginActionListener = new ActionListener() {..} Конец - обработчик кнопки открытия нового чата сработал");
        }
    };

    /*** Методы для работы с панелью чата ***/

    /**
     * Обновляет панель со всеми логинами в соответствии с хранилищами пользователя
     * (Добавляет чат с логином и вешает на него обработчик)
     */
    protected void updateDisputerLoginsPanel(){
        log.info("updateDisputerLoginsPanel() Начало");
        //user = this.getUser();//только на этом моменте у anoWindow есть полноценный пользователь со своими словарями
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
        loginsPanel.revalidate();
        loginsPanel.repaint();
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
                        parseCountMessagesForDownload(),
                        AnoWindow.this
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

        int marginLeftRightBig = (int)(this.getWidth()*0.25);
        int marginLeftRightSmall = (int)(this.getWidth()*0.01);
        int marginLeft = marginLeftRightBig;        // если сообщение от автора
        int marginRight = marginLeftRightSmall;     // иначе
        if (message.isMarginRight) {
            marginLeft = marginLeftRightSmall;
            marginRight = marginLeftRightBig;
        }
        messageTextArea.setMargin(new Insets(0, marginLeft, 0, marginRight));
        Integer messageNum = messageHistoryPanel.getComponentCount();
        //todo добавить еще какие-нибудь настройки, чтоб растянуть сообщение в ширину родителя
        messageHistoryPanel.add(messageTextArea, newConstraints(10,1, 0, messageNum, 1, 1, true, true));
        this.revalidate();
        this.repaint();
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
}
