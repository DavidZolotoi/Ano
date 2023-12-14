package gb.study;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class User {
    private final AnoWindow anoWindow;   //БД в его свойстве
    private final Log log;

    private final Integer id;        //заполняется при добавлении в БД, выгрузкой из неё присвоенного id
    public Integer getId() {
        return id;
    }

    private final String login;   //тоже уникально, но неудобно для использования - например создание наименования чата
    public String getLogin() {
        return login;
    }

    private String password;
    public String getPassword() {
        return password;
    }

    private String firstName;
    public String getFirstName() {
        return firstName;
    }

    private String lastName;
    public String getLastName() {
        return lastName;
    }

    private String mail;
    public String getMail() {
        return mail;
    }

    private String phone;
    public String getPhone() {
        return phone;
    }

    private String comment;
    public String getComment() {
        return comment;
    }

    private LinkedHashMap<Integer, ChatListRow> disputerIdsAndChatListRows;
    public LinkedHashMap<Integer, ChatListRow> getDisputerIdsAndChatListRows() {
        return disputerIdsAndChatListRows;
    }

    private LinkedHashMap<String, ChatListRow> disputerLoginsAndChatListRows;
    public LinkedHashMap<String, ChatListRow> getDisputerLoginsAndChatListRows() {
        return disputerLoginsAndChatListRows;
    }

    private ChatListRow activeChatListRow;
    public ChatListRow getActiveChatListRow() {
        return activeChatListRow;
    }
    public void setActiveChatListRow(ChatListRow activeChatListRow) {
        this.activeChatListRow = activeChatListRow;
    }

    private LinkedHashMap<Integer, Chat> chats;
    public LinkedHashMap<Integer, Chat> getChats() {
        return chats;
    }

    /**
     * Статический метод проверки данных для входа.
     * Если данные для входа корректны, то создаст пользователя и
     * загрузит для пользователя из БД информацию о собеседниках для словарей пользователя,
     * но не информацию о сообщениях (загрузка сообщений по клику).
     * @param login пользователя
     * @param passValuePasswordField пользователя
     * @param anoWindow главное окно, содержит логгер и прочее
     * @return пользователя, если данные корректны
     * @throws IllegalArgumentException исключение, если данные для входа не корректны
     */
    protected static User checkLoginPasswordAndParseUserFromDB(
            String login, JPasswordField passValuePasswordField, AnoWindow anoWindow
    ) throws IllegalArgumentException {
        anoWindow.log.info("checkLoginPasswordAndParseUserFromDB(..) Начало");
        ArrayList<ArrayList<Object>> userFromDB = anoWindow.getDb().selectIdLoginPasswordForUser(login);
        Integer idFromDB = 0;
        String loginFromDB = null;
        String passFromDB = null;
        if (userFromDB!=null && !userFromDB.isEmpty()){
            idFromDB = ((Number) userFromDB.get(0).get(0)).intValue();
            loginFromDB = userFromDB.get(0).get(1).toString();
            passFromDB = userFromDB.get(0).get(2).toString();
            anoWindow.log.info("Выгрузка данных из БД не пустая");
        }
        if (
            !login.equals(loginFromDB) ||
            !Arrays.equals(passValuePasswordField.getPassword(), passFromDB.toCharArray())
        ){
            passValuePasswordField = null;
            passFromDB = null;
            loginFromDB = null;
            idFromDB = 0;
            anoWindow.log.info("Данные введены некорректно");
            JOptionPane.showMessageDialog(null, "Некорректные логин и/или пароль.");
            throw new IllegalArgumentException("Некорректные логин и/или пароль");
        }
        User user = new User(idFromDB, loginFromDB, anoWindow);
        passValuePasswordField = null;
        passFromDB = null;
        loginFromDB = null;
        idFromDB = 0;
        anoWindow.log.info("checkLoginPasswordAndParseUserFromDB(..) Конец - данные проверены и стерты, пользователь создан");
        return user;
    }

    /**
     * Конструктор пользователя, в который передаются данные, загруженные из БД.
     * Приватный, вызывается из метода проверки логина и пароля после проверки .
     * Загрузит из БД информацию о собеседниках для словарей, но не информацию о сообщениях (загрузка сообщений по клику).
     * @param id пользователя, который присвоен ему в БД и загружен из нее
     * @param login пользователя
     * @param window главное окно, содержит логгер и прочее
     */
    private User(
            Integer id, String login,
            JFrame window
    ) {
        this.anoWindow = (AnoWindow)window;
        this.log = this.anoWindow.log;
        log.info("User(..) Начало");
        this.id = id;
        this.login = login;
        //this.password = password;
        this.disputerIdsAndChatListRows = new LinkedHashMap<Integer, ChatListRow>();
        this.disputerLoginsAndChatListRows = new LinkedHashMap<String, ChatListRow>();
        this.chats = new LinkedHashMap<Integer, Chat>();
        disputersUpdate();
        log.info("User(..) Конец");
    }
    /**
     * Обновит словари с информацией о собеседниках: disputerIdsAndChatListRows, disputerLoginsAndChatListRows, chats
     */
    protected void disputersUpdate() {
        log.info("disputersUpdate() Начало");
        parseDisputerIdsAndChatListRows(getChatListRowsFromDB(this));

        ArrayList<Integer> disputerIds = new ArrayList<Integer>(disputerIdsAndChatListRows.keySet());
        parseDisputerLoginsAndChatListRows(getIdsAndLoginsFromDB(disputerIds));

        parseChats();
        log.info("disputersUpdate() Конец");
    }

    /**
     * Метод, получающий из БД список записей о диалогах для пользователя.
     * @param user пользователь, для которого необходимо получить записи
     * @return список записей о диалогах - таблица Object
     */
    private ArrayList<ArrayList<Object>> getChatListRowsFromDB(User user) {
        log.info("getIdFromDB() Начало");
        ArrayList<ArrayList<Object>> chatListRowsFromDB = anoWindow.getDb().selectAllChatListRowWhereId(user);
        log.info("getIdFromDB() Конец - собеседники есть? ", ((Boolean)(chatListRowsFromDB!=null)).toString());
        return chatListRowsFromDB;
    }
    /**
     * Заполняет словарь ''id собеседников -> записи о диалогах'', в которых состоит пользователь user,
     * обработав результаты запроса к таблице записей о диалогах из БД и приведя их к нужному типу
     */
    public void parseDisputerIdsAndChatListRows(ArrayList<ArrayList<Object>> chatListRowsFromDB){
        log.info("parseDisputerIdsAndChatListRows() Начало - парс словаря ''id собеседника -> запись о диалоге''");
        if (chatListRowsFromDB == null) return;
        for (var chatListWhereId : chatListRowsFromDB) {
            Integer idUser1 = null;
            Integer idUser2 = null;
            String comment = null;
            try {
                idUser1 = (chatListWhereId.get(1) != null) ? ((Number) chatListWhereId.get(1)).intValue() : null;
                idUser2 = (chatListWhereId.get(2) != null) ? ((Number) chatListWhereId.get(2)).intValue() : null;
                comment = (chatListWhereId.get(4) != null) ? chatListWhereId.get(4).toString() : null;
            }catch (IndexOutOfBoundsException e){
                log.problem("Ячейки строки chatList не распознаны при выгрузке из БД - массив пуст", anoWindow.lSep, e.getMessage());
                e.printStackTrace();
            }
            ChatListRow chatListRow = new ChatListRow(idUser1, idUser2, comment, anoWindow);
            Integer disputerId = calculateDisputerId(chatListRow);
            disputerIdsAndChatListRows.put(disputerId, chatListRow);
            log.info("В словарь ''id собеседника -> запись о диалоге'' добавлена запись о диалоге с собеседником",
                    "с id =", disputerId.toString(), " и tableName =", chatListRow.getTableName());
        }
        log.info("parseDisputerIdsAndChatListRows() Конец - парс словаря ''id собеседника -> запись о диалоге''");
    }

    /**
     * Метод, получающий из таблицы пользователей БД для данных id, две колонки: id и login
     * @param disputerIds список id, для которых нужно получить результат
     * @return список записей о диалогах - таблица Object
     */
    private ArrayList<ArrayList<Object>> getIdsAndLoginsFromDB(ArrayList<Integer> disputerIds) {
        log.info("getIdsAndLoginsFromDB(..) Начало");
        ArrayList<ArrayList<Object>> idsAndLoginsFromDB = anoWindow.getDb().selectIdsAndLoginsForIds(disputerIds);
        log.info("getIdsAndLoginsFromDB(..) Конец - собеседники есть? ", ((Boolean)(idsAndLoginsFromDB!=null)).toString());
        return idsAndLoginsFromDB;
    }
    /**
     * Получает словарь ''login собеседников -> запись о диалогах'', в которых состоит пользователь user,
     * обработав результаты запроса к таблице пользователей из БД и приведя их к нужному типу
     */
    private void parseDisputerLoginsAndChatListRows(ArrayList<ArrayList<Object>> disputerIdsAndLoginsFromDB) {
        log.info("parseDisputerLoginsAndChatListRows(..) Начало - парс словаря ''login собеседника -> запись о диалоге''");
        if(disputerIdsAndLoginsFromDB == null) return;
        for (var disputerIdAndLogin : disputerIdsAndLoginsFromDB) {
            String disputerLogin = (disputerIdAndLogin.get(1) != null) ? disputerIdAndLogin.get(1).toString() : null;
            Integer disputerId = (disputerIdAndLogin.get(0) != null) ? ((Number) disputerIdAndLogin.get(0)).intValue() : null;
            ChatListRow disputerChatListRow = disputerIdsAndChatListRows.get(disputerId);
            disputerLoginsAndChatListRows.put(
                    disputerLogin,
                    disputerChatListRow
            );
            log.info("В словарь ''login собеседника -> запись о диалоге'' добавлена запись о диалоге с собеседником",
                    "с disputerLogin = ", disputerLogin, " и disputerId = ", disputerId.toString());
        }
        log.info("parseDisputerLoginsAndChatListRows(..) Конец - парс словаря ''login собеседника -> запись о диалоге''");
    }

    /**
     * Создает словарь ''id собеседника -> чат для пользователя'', не обращаясь к БД,
     * на основе уже заполненного словаря disputerIdsAndChatListRows (id собеседника->запись о диалоге).
     * Сообщения из БД в хранилище не грузятся - ждут клика для загрузки.
     * @return словарь id собеседника->Чат для пользователя
     */
    private void parseChats() {
        log.info("parseChats() Начало");
        if (disputerIdsAndChatListRows == null) {
            log.warning("Чаты не созданы, потому что собеседников нет");
            return;
        }
        for (var disputerIdAndChatListRow : disputerIdsAndChatListRows.entrySet()) {
            Integer disputerId = disputerIdAndChatListRow.getKey();
            ChatListRow chatListRow = disputerIdAndChatListRow.getValue();
            Chat chat = new Chat(chatListRow.getTableName(), anoWindow);
            chats.put(disputerId, chat);
            log.info("В словарь ''id собеседника -> чат для пользователя'' добавлен чат",
                    "с disputerId = ", disputerId.toString(), " и chat.getTableName() = ", chat.getTableName());
        }
        log.info("parseChats() Конец - парс словаря ''id собеседника -> чат для пользователя''");
    }

    protected void addNewDisputerFromDBNotify(String[] chatListRowParts){
        // 0. Распознать chatListRow и проигнорировать, если не наш, ведь при создании chatListRow есть запрос в БД (id по tableName)
        Integer id = Integer.parseInt(chatListRowParts[0]);
        Integer userIdMin = Integer.parseInt(chatListRowParts[1]);
        Integer userIdMax = Integer.parseInt(chatListRowParts[2]);
        String tableName = chatListRowParts[3]; //серое, потому что не нужно, потому что имя получается по формуле
        String comment = chatListRowParts[4];
        if (anoWindow.getUser().getId() != userIdMin && anoWindow.getUser().getId() != userIdMax){
            return;
        }
        ChatListRow chatListRow = new ChatListRow(userIdMin, userIdMax, comment, anoWindow);
        // 1. Определить id собеседника и добавить в первый словарь - без запроса в БД
        Integer disputerId = calculateDisputerId(chatListRow);
        disputerIdsAndChatListRows.put(disputerId, chatListRow);
        // 2. Определить login собеседника и добавить во второй словарь - с запросом
        ArrayList<Integer> disputerIds = new ArrayList<>();
        disputerIds.add(disputerId);// коллекция для запроса в БД только из одного id, будет 1 строка ответа: id+login
        ArrayList<ArrayList<Object>> disputerIdsAndLogins = getIdsAndLoginsFromDB(disputerIds);
        String disputerLogin = (String) disputerIdsAndLogins.get(0).get(1);
        disputerLoginsAndChatListRows.put(disputerLogin, chatListRow);
        // 3. Добавить чат
        Chat disputerChat = new Chat(chatListRow.getTableName(), anoWindow);
        chats.put(disputerId, disputerChat);
        // 4. Добавить компонент на экран и повесить на него обработчик
        for (var disputerLoginAndChatListRow : disputerLoginsAndChatListRows.entrySet()){
            if(disputerLoginAndChatListRow.getKey().equals(disputerLogin)){
                anoWindow.tabChatPanel.addNewDisputerAndListener(disputerLoginAndChatListRow);
                // Запустить асинхронное прослушивание коллекции каналов из одного канала:
                var chatListRows = new ArrayList<ChatListRow>();
                chatListRows.add(chatListRow);
                CompletableFuture<Void> futureNewListenerNewMessage = listenerNewMessageAsync(chatListRows);
                break;
            }
        }
    }

    /**
     * Рассчитывает id собеседника для пользователя, имея запись о диалоге
     * @param chatListRow запись о диалоге
     * @return id собеседника
     */
    public Integer calculateDisputerId(ChatListRow chatListRow){
        return chatListRow.getUserIdMin() + chatListRow.getUserIdMax() - id;
    }

    /**
     * Для нажатого компонента chatListRowClick проверить была ли смена активного чатЛиста.
     * Вне зависимости от результата сравнения, обновляет активный чатЛист юзера
     * @param chatListRowClick компонент, получивший клик
     * @return результат сравнения: true, если поменялся
     */
    public Boolean isChangeActiveChatListRow(ChatListRow chatListRowClick) {
        log.info("isChangeActiveChatListRow(..) Начало");
        Boolean isChangeActiveChat = true;
        if (this.activeChatListRow == null) {
            log.warning("На этом месте this.activeChatListRow == null => вероятно первый клик по ChatListRow");
        }
        if (chatListRowClick.getId() == null){
            log.problem("Ситуация, которая возможна только в теории, на практике такого не должно быть.",
                    "ChatListRow, по которому кликнули не имеет id.");
        }
        if (this.activeChatListRow != null) {
            isChangeActiveChat = chatListRowClick.getId() != activeChatListRow.getId();
            log.warning("Поменялся ли активный чат?", isChangeActiveChat.toString());
        }
        this.activeChatListRow = chatListRowClick;
        log.info("isChangeActiveChatListRow(..) Конец - на данный момент активный чатЛист:",
                "№", this.activeChatListRow.getId().toString(), "-", this.activeChatListRow.getTableName());
        return isChangeActiveChat;
    }

    /**
     * Запустить все прослушивания (как новых чатов, так и сообщений)
     */
    public void startListening() {
        // 1. Запустить асинхронное прослушивание этих каналов:
        CompletableFuture<Void> futureListenerNewMessage =
                listenerNewMessageAsync(    new ArrayList<>(disputerLoginsAndChatListRows.values())    );
        // 2. Запуск асинхронное прослушивание нового чата с неизвестным
        CompletableFuture<Void> futureListenerNewDisputer =
                listenerNewDisputerAsync(    new ArrayList<>(disputerLoginsAndChatListRows.values())    );
    }
    /**
     * Асинхронный метод прослушивания уведомлений о новых сообщениях
     * @param chatListRows записи о диалогах, которые необходимо прослушивать
     * @return
     */
    private CompletableFuture<Void> listenerNewMessageAsync(ArrayList<ChatListRow> chatListRows) {
        return CompletableFuture.runAsync(() -> {
            System.out.println("Прослушивание уведомлений о новых сообщениях запущено.");
            anoWindow.getDb().startListenerNewMessage(chatListRows);
        });
    }
    /**
     * Асинхронный метод прослушивания уведомлений о добавлении новой записи о диалоге (чате)
     * @param chatListRows записи о диалогах, которые необходимо прослушивать
     * @return
     */
    private CompletableFuture<Void> listenerNewDisputerAsync(ArrayList<ChatListRow> chatListRows) {
        return CompletableFuture.runAsync(() -> {
            System.out.println("Прослушивание уведомлений о добавлении новой записи о диалоге запущено.");
            anoWindow.getDb().startListenerNewChatListRow();
        });
    }


    /**
     * Распознавание и приведение id, присвоенного для User автоматически в БД,
     * загруженного из БД
     * @return Распознанный и приведенный id
     */
    private Integer parseIdFromDB(ArrayList<ArrayList<Object>> userIdFromDB){
        log.info("parseIdFromBD(..) Начало");
        Object userIdObj = null;
        Integer userId = null;
        try {
            userIdObj = userIdFromDB.get(0).get(0);
            userId = ((Number) userIdObj).intValue();
        } catch (IndexOutOfBoundsException e){
            log.problem("id пользователя не распознан при выгрузке из БД - массив пуст" + anoWindow.lSep + e.getMessage());
            e.printStackTrace();
        }
        log.info("parseIdFromBD(..) Конец");
        return userId;
    }
    /**
     * Метод, получающий id из таблицы user в БД.
     * Выгрузит из БД отчет с присвоенным id.
     * Все полученные данные - таблица Object
     * @return id, присвоенный в БД, для user
     */
    private ArrayList<ArrayList<Object>> getIdFromDB(String login){
        //todo надо создать другой метод, который не запрашивает пароль
        return anoWindow.getDb().selectIdLoginPasswordForUser(login);
    }
}
