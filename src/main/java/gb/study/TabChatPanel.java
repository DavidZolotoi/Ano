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
        user = anoWindow.getUser(); //todo видимо надо отсюда удалить, потому юзер распознается позже

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
        // Добавление обработчика на кнопку поиска нового юзера
        searchLoginButton.addActionListener(searchLoginActionListener);
        // Добавление обработчика на кнопку отправки
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
     * (Добавляет чат и вешает на него обработчик)
     */
    protected void updateDisputerLoginsPanel(){
        user = anoWindow.getUser();
        // 0. Прогон по списку диалогов с этим юзером (disputer), с повешанными обработчиками
        for (var disputerLoginAndChatListRow : user.getDisputerLoginsAndChatListRows().entrySet()) {
            addNewDisputerAndListener(disputerLoginAndChatListRow);
        }
    }
    /**
     * Добавляет чат с новым логином и вешает на него обработчик
     * @param disputerLoginAndChatListRow информация о новом чате
     */
    protected void addNewDisputerAndListener(Map.Entry<String, ChatListRow> disputerLoginAndChatListRow) {
        // 1. добавить JTextArea с его disputerLogin и повесить на него обработчик
        String disputerLogin = disputerLoginAndChatListRow.getKey();
        JTextArea loginTextArea = addDisputerLoginTextArea(disputerLogin);
        // 2. повесить на него обработчик
        loginAddMouseListener(loginTextArea);
    }

    /**
     * Добавляет на панель логинов компонент с логином,
     * @param disputerLogin логин пользователя, который необходимо добавить
     * @return ссылку на добавленный компонент
     */
    protected JTextArea addDisputerLoginTextArea(String disputerLogin) {
        JTextArea loginTextArea = new JTextArea(disputerLogin);
        loginTextArea.setEditable(false);
        loginTextArea.setLineWrap(true);
        loginTextArea.setWrapStyleWord(true);
        anoWindow.tabChatPanel.getLoginsPanel().add(loginTextArea);
        return loginTextArea;
    }

    /**
     * Вешает обработчик на клик по компоненту с пользователем (чату)
     * @param loginTextArea ссылка на компонент с логином пользователя (чат)
     */
    private void loginAddMouseListener(JTextArea loginTextArea) {
        loginTextArea.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                JTextArea sourceLoginTextArea = (JTextArea) e.getSource();
                // Проверка - если активный чат после клика не поменялся, то выходим
                if (!user.isChangeActiveChatListRow(sourceLoginTextArea)) return;
                // Загрузка последних сообщений из БД в хранилище (конкретный чат из словаря) юзера
                // в словарь добавляются только сообщения, которые еще не скачаны
                String disputerLogin = loginTextArea.getText();
                ChatListRow disputerChatListRow = user.getDisputerLoginsAndChatListRows().get(disputerLogin);
                Integer disputerId = user.calculateDisputerId(disputerChatListRow);
                user.getChats().get(disputerId).downloadLastMessages(
                        disputerChatListRow,
                        Integer.parseInt(anoWindow.tabSettingsPanel.getCountMesForDownValueTextArea().getText()),
                        anoWindow
                );
            }
        });
    }

    /**
     * Добавление нового сообщения на свое место в историю сообщений.
     * Вызывается в методе чата после загрузки сообщений.
     * @param message сообщение, которое нужно добавить
     */
    protected void addAndShowNewMessage(Message message) {
            JTextArea messageTextArea = new JTextArea(message.show());
            messageTextArea.setEditable(false);
            messageTextArea.setLineWrap(true);
            messageTextArea.setWrapStyleWord(true);
            // отступы
            int marginLeftRightBig = (int)(anoWindow.getWidth()*0.25);
            int marginLeftRightSmall = (int)(anoWindow.getWidth()*0.01);
            int marginLeft = marginLeftRightBig;        // если сообщение от автора
            int marginRight = marginLeftRightSmall;
            if (message.isMarginRight) {                // если сообщение от собеседника
                marginLeft = marginLeftRightSmall;
                marginRight = marginLeftRightBig;
            }
            messageTextArea.setMargin(new Insets(0, marginLeft, 0, marginRight));
            // добавление сообщения в историю переписки
            messageHistoryPanel.add(messageTextArea);
            anoWindow.revalidate();  // Пересчитать компоновку
            anoWindow.repaint();     // Перерисовать
            //todo добавить прокрутку колесиком вниз истории сообщений
    }
    /**
     * Добавляет уже загруженные сообщения из словаря на свое место в историю сообщений.
     * Вызывает в цикле метод добавления одного сообщения
     * @param messages коллекция сообщений, которые необходимо добавить
     */
    protected void addAndShowMessagesFromList(ArrayList<Message> messages) {
        if (messages.isEmpty()) return;
        clearMessageHistoryPanel();
        for (var message : messages) {
            addAndShowNewMessage(message);
        }
        //loginsPanelNotice(String loginValue);
        messageHistoryPanel.revalidate();
        messageHistoryPanel.repaint();
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
     * Очистить историю сообщений
     */
    protected void clearMessageHistoryPanel(){
        Component[] components = messageHistoryPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JTextArea) {
                messageHistoryPanel.remove(component);
            }
        }
    }

    /**
     * Обработчик отправки письма в БД, сам запрос спрятан внутри метода
     */
    ActionListener sendMessageActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //отправить письмо в БД активного диалога, остальное должна сделать прослушка
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
     * Обработчик поиска пользователя
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
                        "Пользователей, такому поисковому запросу не найдено. Уточните запрос."
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
                        "",     //comment
                        anoWindow
                );
            }
        }
    };

}