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
    private JTextArea dbStatusTextArea;
    private JButton dbDefaultButton;
    private JButton dbJsonButton;
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
        dbStatusTextArea = new JTextArea("Требуется соединение с БД. Выберите способ подключения.");
        dbStatusTextArea.setBackground(null);
        dbStatusTextArea.setEditable(false);
        dbStatusTextArea.setLineWrap(true);
        dbStatusTextArea.setWrapStyleWord(true);
        dbDefaultButton = new JButton("По умолчанию");
        dbJsonButton = new JButton("Загрузить JSON");
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

        loginStatusTextArea = new JTextArea("Требуется авторизация");
        loginStatusTextArea.setBackground(null);
        loginStatusTextArea.setEditable(false);
        loginStatusTextArea.setLineWrap(true);
        loginStatusTextArea.setWrapStyleWord(true);
        logingButton = new JButton("Войти");

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
        setLayout(new GridBagLayout());
        add(leftPanel, newConstraints(4,0, 0,0, 2, 3));
        leftPanel.setLayout(new GridBagLayout());
            leftPanel.add(dbStatusTextArea,         newConstraints(2,0,0,0,1,2));
            leftPanel.add(dbDefaultButton,          newConstraints(2,0,1,0,1,1));
            leftPanel.add(dbJsonButton,             newConstraints(2,0,1,1,1,1));
            leftPanel.add(new JLabel(" "),      newConstraints(2,0,0,2,2,1));
            leftPanel.add(new JLabel(" "),      newConstraints(2,0,0,3,2,1));
            leftPanel.add(new JLabel(" "),      newConstraints(2,0,0,4,2,1));
            leftPanel.add(loginCommentTextArea,     newConstraints(2,0,0,5,1,1));
            leftPanel.add(loginValueTextArea,       newConstraints(2,0,1,5,1,1));
            leftPanel.add(passCommentTextArea,      newConstraints(2,0,0,6,1,1));
            leftPanel.add(passValuePasswordField,   newConstraints(2,0,1,6,1,1));
            leftPanel.add(loginStatusTextArea,      newConstraints(2,0,0,7,1,1));
            leftPanel.add(logingButton,             newConstraints(2,0,1,7,1,1));
        add(rightPanel, newConstraints(4,0, 2,0, 2, 3));
        rightPanel.setLayout(new GridBagLayout());
            rightPanel.add(countMesForDownTextArea,     newConstraints(2,0, 0,0, 1, 1));
            rightPanel.add(countMesForDownValueTextArea,newConstraints(2,0, 1,0, 1, 1));
        // Обработчики
        logingButton.addActionListener(logingActionListener);
        dbJsonButton.addActionListener(dbJsonActionListener);
        dbDefaultButton.addActionListener(dbDefaultActionListener);
        //todo регистрация anoWindow.getDb().insertNewUserAndConfigure(anoWindow); - добавляет нового пользователя в БД

        log.info("TabSettingsPanel(JFrame window) Конец");
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
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        return gridBagConstraints;
    }

    // Обработчики кнопки входа в систему и регистрации
    final ActionListener logingActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            log.info("Обработчик кнопки logingButton Начало");
            if(anoWindow.getDb() == null){
                dbDefaultActionListener.actionPerformed(new ActionEvent(dbDefaultButton, 0, null));
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
    final ActionListener dbJsonActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            log.info("Обработчик кнопки dbJsonButton Начало");
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                DB.settingsFilePath = fileChooser.getSelectedFile().getAbsolutePath();
                log.info("Указан файл JSON:", DB.settingsFilePath);
                anoWindow.setDb(new DB(anoWindow));
                dbStatusTextArea.setText("Соединение установлено.");
                log.info("Обработчик кнопки dbJsonButton Конец - База данных по указанному файлу JSON создана.");
            }
            else {
                log.warning("Пользователь намеревался, но не выбрал файл для подключения к БД");
            }
        }
    };
    final ActionListener dbDefaultActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            log.info("Обработчик кнопки dbDefaultButton Начало");
                DB.settingsFilePath = "E:\\Csharp\\GB\\Ano\\Anoswing\\settings_past_the_git.json";
                anoWindow.setDb(new DB(anoWindow));
                dbStatusTextArea.setText("Соединение по умолчанию установлено.");
                log.info("Обработчик кнопки dbDefaultButton Конец - База данных по умолчанию создана.");
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
}
