package gb.study;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TabSettingsPanel extends JPanel {
    // ОБЩЕЕ окно
    private final AnoWindow anoWindow;   //БД в его свойстве
    private final Log log;
    // ЛЕВАЯ панель
    private final JPanel leftPanel;
    private final JTextArea loginCommentTextArea;
    private final JTextArea loginValueTextArea;
    private final JPasswordField passValuePasswordField;
    private final JButton logingButton;
    private final JTextArea loginStatusTextArea;
    // ПРАВАЯ панель
    private final JPanel rightPanel;
    private final JTextArea countMesForDownTextArea;
    private final JTextArea countMesForDownValueTextArea;
    public JTextArea getCountMesForDownValueTextArea() {
        return countMesForDownValueTextArea;
    }

    public TabSettingsPanel(JFrame window) {
        super(new GridLayout(1, 2));
        this.anoWindow = (AnoWindow)window;
        this.log = this.anoWindow.log;
        log.info("TabSettingsPanel(JFrame window) Начало");
        leftPanel = new JPanel();
        rightPanel = new JPanel();

        log.info("Создание компонентов для leftPanel");
        loginCommentTextArea = new JTextArea("Логин:* ");
        loginCommentTextArea.setEditable(false);
        loginCommentTextArea.setLineWrap(true);
        loginCommentTextArea.setWrapStyleWord(true);
        loginCommentTextArea.setBackground(new Color(0, 0, 0, 0));
        loginValueTextArea = new JTextArea();
        loginValueTextArea.setLineWrap(true);
        loginValueTextArea.setWrapStyleWord(true);

        JTextArea passCommentTextArea = new JTextArea("Пароль:* ");
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

        log.info("Создание компонентов для rightPanel");
        countMesForDownTextArea = new JTextArea("Количество загружаемых сообщений, при открытии диалога:");
        countMesForDownTextArea.setEditable(false);
        countMesForDownTextArea.setLineWrap(true);
        countMesForDownTextArea.setWrapStyleWord(true);
        countMesForDownTextArea.setBackground(new Color(0, 0, 0, 0));
        countMesForDownValueTextArea = new JTextArea();
        countMesForDownValueTextArea.setLineWrap(true);
        countMesForDownValueTextArea.setWrapStyleWord(true);

        // РАЗМЕТКА
        log.info("РАЗМЕТКА панели настроек - добавление всего созданного");
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

        // Обработчики
        logingButton.addActionListener(logingActionListener);
        //todo Надо создать кнопку "регистрация"
        // anoWindow.getDb().insertNewUserAndConfigure(anoWindow); - добавляет нового пользователя в БД

        log.info("TabSettingsPanel(JFrame window) Конец");
    }

    // Обработчик кнопки входа в систему
    final ActionListener logingActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            log.info("Обработчик кнопки logingButton Начало");
            if(loginValueTextArea.getText()=="" || loginValueTextArea.getText()==null){
                log.problem("Не введен логин пользователя");
                JOptionPane.showMessageDialog(null, "Не введен логин пользователя");
                return;
            }
            User user = null;
            try {
                user = User.checkLoginPasswordAndParseUserFromDB(
                        loginValueTextArea.getText(),
                        passValuePasswordField,
                        anoWindow
                );
            } catch (IllegalArgumentException argExp) {
                log.problem(argExp.getMessage());
            }
            anoWindow.setUser(user);
            loginStatusTextArea.setText("*");
            log.info("Пользователь проверен и создан, словари загружены, а сообщения - нет.");
            anoWindow.tabChatPanel.updateDisputerLoginsPanel();
            user.startListening();
            log.info("Обработчик кнопки logingButton Конец");
        }
    };

    /**
     * Распознает число, введенное в поле количества последних загружаемых сообщений
     * @return значение (если возникли исключения, то значение по умолчанию)
     */
    protected Integer parseCountMessagesForDownload(){
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
}
