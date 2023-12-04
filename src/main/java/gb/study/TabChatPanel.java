package gb.study;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
;

public class TabChatPanel extends JPanel {
    // ОБЩЕЕ окно
    private AnoWindow anoWindow;   //БД в его свойстве

    // ЛЕВАЯ панель
    public JPanel leftPanel;    //todo переделать в private

    // ПРАВАЯ панель
    private JPanel rightPanel;
    // верхняя панель ПРАВОЙ панели
    private JScrollPane messageHistoryScroll;
    private JPanel messageHistoryPanel;
    private String messageAuthor;               //из настроек
    private int countMessagesDownloadAtStart;   //из настроек
    //todo нужна ли тут коллекция/словарь сообщений или элементов JTextArea? вроде и без нее норм
    // нижняя панель ПРАВОЙ панели
    private JPanel bottomPanel;
    private JTextArea messageForSendTextArea;
    private JButton messageAttachButton;
    private JButton messageSendButton;
    //todo нет ни одного геттера и сеттера - подумать нужны ли?

    public TabChatPanel(JFrame window) {
        super();
        this.anoWindow = (AnoWindow)window;

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
        // Добавление обработчика на кнопку отправки
        messageSendButton.addActionListener(sendMessageActionListener);


        // ДОБАВЛЕНИЕ ВСЕГО СОЗДАННОГО НА ОКНО
        // ЛЕВАЯ панель
        add(leftPanel);
        //todo переделать testTextArea в поле для поиска собеседника
        JTextArea testTextArea = new JTextArea("тестовое слово и не одно а много всяких разных слов, нужно больше");
        testTextArea.setEditable(false);
        testTextArea.setLineWrap(true);
        testTextArea.setWrapStyleWord(true);
        //todo сделать метод, загружающий актуальные диалоги в левую панель
        leftPanel.add(testTextArea);
        // ПРАВАЯ панель
        add(rightPanel);
        // Добавление истории сообщений
        rightPanel.add(messageHistoryScroll);
        // Загрузить последние N сообщений и вставить их на панель сообщений messageHistoryPanel
        //todo загрузка сообщений должна происходить после выбора чата (клика)
        //downloadAndPasteLastMessagesToHistoryPanel(); //todo надо проскроллить
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
     * Вставка нового сообщения на свое место в историю сообщений.
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
     * Обработчик отправки письма в БД, сам запрос спрятан внутри метода
     */
    ActionListener sendMessageActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //отправить письмо в БД активного диалога, остальное должна сделать прослушка
            Integer authorId = anoWindow.getUser().getId();
            String mesContent = messageForSendTextArea.getText();
            String mesComment = "Комментарий к сообщению 6";
            ChatListRow activeChatListRow = anoWindow.getUser().getActiveChatListRow();
            Message messageForSend = new Message(
                    authorId,
                    mesContent,
                    mesComment,
                    activeChatListRow,
                    anoWindow
            );
            messageForSendTextArea.setText("");
            messageForSendTextArea.requestFocus();
        }
    };

}
