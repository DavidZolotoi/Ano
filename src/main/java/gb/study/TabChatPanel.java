package gb.study;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;

public class TabChatPanel extends JPanel {
    // общее
    private AnoWindow anoWindow;   //БД в его свойстве
    private GridBagLayout gridBagLayout;
    private GridBagConstraints gbcForTabChat;
    // верхняя панель
    private JScrollPane messageHistoryScroll;
    private JPanel messageHistoryPanel;
    private int countMessagesDownloadAtStart;
    //todo нужна ли тут коллекция/словарь сообщений или элементов JTextArea? вроде и без нее норм

    // нижняя панель
    private JPanel bottomPanel;
    private GridBagConstraints gbcForBottomPanel;
    private JTextArea messageTextField;
    private JButton messageAttachButton;
    private JButton messageSendButton;
    //todo нет ни одного геттера и сеттера - подумать нужны ли?

    public TabChatPanel(JFrame window) {
        super(new GridBagLayout());
        this.anoWindow = (AnoWindow)window;
        // 1. ВЕРХНИЙ элемент - это JPanel из многих JTextArea, завернутая в JScrollPane
        messageHistoryPanel = new JPanel();
        messageHistoryPanel.setLayout(new BoxLayout(messageHistoryPanel, BoxLayout.Y_AXIS));

        //Загрузить последние N сообщений
        countMessagesDownloadAtStart = 20; //todo сделать загрузку этого числа из настроек
        for (Message message : anoWindow.db.getLastMessages(countMessagesDownloadAtStart)) {
            addAndShowNewMessage(message);
        }

        // Создаем JScrollPane
        messageHistoryScroll = new JScrollPane();
        // Устанавливаем политику прокрутки для вертикальной полосы
        messageHistoryScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        // Устанавливаем в него панель
        messageHistoryScroll.setViewportView(messageHistoryPanel);

        // 2. НИЖНЯЯ панель (панель с текстовым полем, кнопками)
        bottomPanel = new JPanel(new GridBagLayout());
        gbcForBottomPanel = new GridBagConstraints();
        // Содержимое нижней панели: ввода текста + 2 кнопки
        messageTextField = new JTextArea();
        messageAttachButton = new JButton("V");
        messageSendButton = new JButton("Отправить");

        // Настройки для элементов
        messageTextField.setLineWrap(true);
        messageTextField.setWrapStyleWord(true);

        // Добавление обработчика на кнопку отправки
        messageSendButton.addActionListener(actionListener);

        // Добавление первой колонки в нижнюю панель
        gbcForBottomPanel.gridx = 0;
        gbcForBottomPanel.gridy = 0;
        gbcForBottomPanel.fill = GridBagConstraints.BOTH;
        gbcForBottomPanel.weightx = 0.85;   // Ширина поля ввода
        gbcForBottomPanel.weighty = 1;
        bottomPanel.add(messageTextField, gbcForBottomPanel);
        // Добавление второй колонки в нижнюю панель
        gbcForBottomPanel.gridx = 1;
        gbcForBottomPanel.weightx = 0.02;   // Ширина кнопки для вложений
        bottomPanel.add(messageAttachButton, gbcForBottomPanel);
        // Добавление третьей колонки в нижнюю панель
        gbcForBottomPanel.gridx = 2;
        gbcForBottomPanel.weightx = 0.13;   // Ширина кнопки для отправки
        bottomPanel.add(messageSendButton, gbcForBottomPanel);

        // 3. Создание панели для первой вкладки
        this.gridBagLayout = new GridBagLayout();
        gbcForTabChat = new GridBagConstraints();

        // Добавление первой строки вкладки чата
        gbcForTabChat.gridx = 0;
        gbcForTabChat.gridy = 0;
        gbcForTabChat.fill = GridBagConstraints.BOTH;
        gbcForTabChat.weightx = 1.0;
        gbcForTabChat.weighty = 0.9; // Высота первой строки
        add(messageHistoryScroll, gbcForTabChat);

        // Добавление второй строки вкладки чата
        gbcForTabChat.gridy = 1;
        gbcForTabChat.weighty = 0.1; // Высота второй строки
        add(bottomPanel, gbcForTabChat);
    }

    // Создание обработчика событий
    ActionListener actionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            //todo переделать обработчик отправки письма: БД и всё такое...
            String messageAuthor = (new Random().nextBoolean())?"Customer":"Comp";
            String messageText = "" + messageTextField.getText();
            Message message = new Message(messageAuthor, messageText);
            addAndShowNewMessage(message);
            messageTextField.setText("");
        }
    };


    private void addAndShowNewMessage(Message message) {
        JTextArea messageTextArea = new JTextArea(message.show());
        messageTextArea.setEditable(false);
        messageTextArea.setLineWrap(true);
        messageTextArea.setWrapStyleWord(true);
        // отступы
        int marginLeft, marginRight;
        int marginLeftRightBig = (int)(anoWindow.getWidth()*0.25);
        int marginLeftRightSmall = (int)(anoWindow.getWidth()*0.01);
        if (message.isMarginRight) {
            marginLeft = marginLeftRightSmall;
            marginRight = marginLeftRightBig;
        }else{
            marginLeft = marginLeftRightBig;
            marginRight = marginLeftRightSmall;
        }
        messageTextArea.setMargin(new Insets(0, marginLeft, 0, marginRight));
        // добавление сообщения в историю переписки
        messageHistoryPanel.add(messageTextArea);
        anoWindow.revalidate();  // Пересчитать компоновку
        anoWindow.repaint();     // Перерисовать
    }

}
