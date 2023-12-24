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
    private final JTextArea loginValueTextArea;
    private final JPasswordField passValuePasswordField;
    private final JButton loginButton;
    private final JTextArea loginStatusTextArea;
    // ПРАВАЯ панель
    private final JPanel rightPanel;
    private final JTextArea countMesForDownValueTextArea;
    public JTextArea getCountMesForDownValueTextArea() {
        return countMesForDownValueTextArea;
    }

    public TabSettingsPanel(JFrame window) {
        super(new GridLayout(1, 2));
        this.anoWindow = (AnoWindow)window;
        this.log = this.anoWindow.log;
        log.info("TabSettingsPanel(JFrame window) Начало");
        leftPanel = anoWindow.tsLeftPanel;
        rightPanel = anoWindow.tsRightPanel;
        dbStatusTextArea = anoWindow.dbStatusTextArea;
        dbStatusTextArea.setBackground(new Color(0, 0, 0, 0));
        dbDefaultButton = anoWindow.dbDefaultButton;
        dbJsonButton = anoWindow.dbJsonButton;
        loginStatusTextArea = anoWindow.loginStatusTextArea;
        loginStatusTextArea.setBackground(new Color(0, 0, 0, 0));
        loginValueTextArea = anoWindow.loginValueTextArea;
        passValuePasswordField = anoWindow.passValuePasswordField;
        loginButton = anoWindow.loginButton;
        countMesForDownValueTextArea = anoWindow.countMesForDownValueTextArea;

        // Обработчики
        loginButton.addActionListener(logingActionListener);
        dbJsonButton.addActionListener(dbJsonActionListener);
        dbDefaultButton.addActionListener(dbDefaultActionListener);
        //todo регистрация anoWindow.getDb().insertNewUserAndConfigure(anoWindow); - добавляет нового пользователя в БД

        anoWindow.revalidate();
        anoWindow.repaint();

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
