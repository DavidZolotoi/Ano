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

    private final String password;
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
     * @param login пользователя
     * @param pass пользователя
     * @param anoWindow главное окно, содержит логгер и прочее
     * @return пользователя, если данные корректны
     * @throws IllegalArgumentException исключение, если данные для входа не корректны
     */
    protected static User checkLoginPasswordAndParseUserFromDB(String login, String pass, AnoWindow anoWindow) throws IllegalArgumentException {
        // По логину выгрузить id, логин и пароль
        ArrayList<ArrayList<Object>> userFromDB = anoWindow.getDb().selectIdLoginPasswordForUser(login);
        // Если есть выгрузка, то распознать ее
        Integer idFromDB = 0;
        String loginFromDB = null;
        String passFromDB = null;
        if (!userFromDB.isEmpty() && userFromDB != null){
            idFromDB = ((Number) userFromDB.get(0).get(0)).intValue();
            loginFromDB = userFromDB.get(0).get(1).toString();
            passFromDB = userFromDB.get(0).get(2).toString();
        }
        // Если логин и пароль не совпадают,
        if ( !(login.equals(loginFromDB) && pass.equals(passFromDB)) ){
            JOptionPane.showMessageDialog(null, "Некорректные логин и/или пароль.");
            throw new IllegalArgumentException("Некорректные логин и/или пароль");
        }
        // иначе создать объект User через конструктор и вернуть его
        return new User(idFromDB, loginFromDB, passFromDB, anoWindow);
    }

    /**
     * Конструктор пользователя, в который передаются данные, загруженные из БД.
     * Приватный, вызывается из метода проверки логина и пароля.
     * @param id пользователя, который присвоен ему в БД и загружен из нее
     * @param login пользователя
     * @param password пользователя
     * @param window главное окно, содержит логгер и прочее
     */
    private User(
            Integer id, String login, String password,
            JFrame window
    ) {
        this.anoWindow = (AnoWindow)window;
        this.log = this.anoWindow.log;
        log.info("User(..) Начало");
        this.id = id;
        this.login = login;
        this.password = password;
        this.disputerIdsAndChatListRows = new LinkedHashMap<Integer, ChatListRow>();
        this.disputerLoginsAndChatListRows = new LinkedHashMap<String, ChatListRow>();
        // ВНИМАНИЕ! В словаре chats: Integer - это id собеседника
        this.chats = new LinkedHashMap<Integer, Chat>();
        disputersUpdate();
        // в результате объекты чатов созданы, но сообщения не загружены (будут загружены при открытии диалога)
        log.info("User(..) Конец");
    }
    /**
     * Обновит словари собеседников: disputerIdsAndChatListRows, disputerLoginsAndChatListRows, chats
     */
    protected void disputersUpdate() {
        log.info("disputersUpdate() Начало");
        parseDisputerIdsAndChatListRows(getChatListRowsFromDB(this));
        parseDisputerLoginsAndChatListRows();
        // ВНИМАНИЕ! В словаре chats: Integer - это id собеседника
        parseChats();
        // в результате объекты чатов созданы, но сообщения не загружены (будут загружены при открытии диалога)
        log.info("disputersUpdate() Конец");
    }

    /**
     * Заполняет словарь ''id собеседников -> записи о диалогах'', в которых состоит пользователь user,
     * обработав результаты запроса к таблице записей о диалогах из БД и приведя их к нужному типу
     */
    public void parseDisputerIdsAndChatListRows(ArrayList<ArrayList<Object>> chatListRowsFromDB){
        log.info("parseDisputerIdsAndChatListRows() Начало - парс словаря ''id собеседника -> запись о диалоге''");
        for (var chatListWhereId : chatListRowsFromDB) {
            Integer idUser1 = null;
            Integer idUser2 = null;
            String comment = null;
            try {
                idUser1 = ((Number) chatListWhereId.get(1)).intValue();
                idUser2 = ((Number) chatListWhereId.get(2)).intValue();
                comment = chatListWhereId.get(4).toString();
            }catch (IndexOutOfBoundsException e){
                log.problem("Ячейки строки chatList не распознаны при выгрузке из БД - массив пуст" + anoWindow.lSep + e.getMessage());
                e.printStackTrace();
            }
            //конструктор проверит, если такой записи нет в БД, то создаст ее.
            //todo проверить, нужен ли такой подход, может проверка - лишняя?
            ChatListRow chatListRow = new ChatListRow(idUser1, idUser2, comment, anoWindow);
            Integer disputerId = calculateDisputerId(chatListRow);
            disputerIdsAndChatListRows.put(disputerId, chatListRow);
            log.info("В словарь ''id собеседника -> запись о диалоге'' добавлена запись о диалоге с собеседником с id=" + disputerId + " и tableName=" + chatListRow.getTableName());
        }
        log.info("parseDisputerIdsAndChatListRows() Начало - парс словаря ''id собеседника -> запись о диалоге''");
    }
    /**
     * Метод, получающий из БД список записей о диалогах для пользователя.
     * Все полученные данные - таблица Object
     * @param user пользователь, для которого необходимо получить записи
     * @return список записей о диалогах
     */
    private ArrayList<ArrayList<Object>> getChatListRowsFromDB(User user) {
        return anoWindow.getDb().selectAllChatListRowWhereId(user);
    }

    /**
     * Получает словарь login собеседников -> запись о диалогах, в которых состоит пользователь user,
     * обработав результаты запроса к таблице пользователей из БД и приведя их к нужному типу
     * @return словарь login собеседников -> запись о диалогах
     */
    private LinkedHashMap<String, ChatListRow> parseDisputerLoginsAndChatListRows() {
        System.out.println("--**-- Метод parseDisputerLoginsAndChatListRows");
        var disputerLoginsAndChatListRows = new LinkedHashMap<String, ChatListRow>();
        var disputerIds = new ArrayList<Integer>(disputerIdsAndChatListRows.keySet());
        ArrayList<ArrayList<Object>> disputerIdsAndLogins = getIdsAndLoginsFromDB(disputerIds);
        System.out.println("--**-- Вернулись в Метод parseDisputerLoginsAndChatListRows");
        for (var disputerIdAndLogin : disputerIdsAndLogins) {
            String disputerLogin = (String) disputerIdAndLogin.get(1);
            Integer disputerId = (Integer) disputerIdAndLogin.get(0);
            ChatListRow disputerChatListRow = disputerIdsAndChatListRows.get(disputerId);
            disputerLoginsAndChatListRows.put(
                    disputerLogin,
                    disputerChatListRow
            );
            System.out.println("--**-- disputerLogin = " + disputerLogin + " disputerId = " + disputerId);
        }
        System.out.println("--**-- Вышли из Метод parseDisputerLoginsAndChatListRows");
        return disputerLoginsAndChatListRows;
    }
    /**
     * Метод, получающий из таблицы пользователей БД для данных id, две колонки: id и login
     * Все полученные данные - таблица Object
     * @param disputerIds список id, для которых нужно получить результат
     * @return
     */
    private ArrayList<ArrayList<Object>> getIdsAndLoginsFromDB(ArrayList<Integer> disputerIds) {
        System.out.println("--**-- Метод getIdsAndLoginsFromDB");
        return anoWindow.getDb().selectIdsAndLoginsForIds(disputerIds);
    }

    /**
     * Создает словарь id собеседника->Чат для пользователя, не обращаясь к БД,
     * на основе уже заполненного словаря disputerIdsAndChatListRows (id собеседника->запись о диалоге)
     * @return словарь id собеседника->Чат для пользователя
     */
    private LinkedHashMap<Integer, Chat> parseChats() {
        var chats = new LinkedHashMap<Integer, Chat>();
        for (var disputerIdAndChatListRow : disputerIdsAndChatListRows.entrySet()) {
            Integer disputerId = disputerIdAndChatListRow.getKey();
            Chat disputerChat = new Chat(disputerIdAndChatListRow.getValue().getTableName(), anoWindow);
            chats.put(disputerId, disputerChat);
        }
        return chats;
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
     * Для нажатого компонента loginTextArea проверить была ли смена чатЛиста.
     * В любом случае вне зависимости от результата сравнения, обновляет активный чатЛист юзера
     * @param loginTextArea компонент, получивший клик
     * @return результат сравнения
     */
    public Boolean isChangeActiveChatListRow(JTextArea loginTextArea) {
        //todo надо переопределить метод equals у ChatListRow
        Boolean isChangeActiveChat = true;  //предполагаем, что всё впервые, т.е. this.activeChatListRow == null
        if (this.activeChatListRow != null)
            isChangeActiveChat = disputerLoginsAndChatListRows.get(loginTextArea.getText()).getId() != activeChatListRow.getId();
        // сравнение id от сhatListRow, полученного из login и id от activeChatListRow
        // если они не равны, значит была смена чата
        // при любом раскладе обновляем активный чатлист
        this.activeChatListRow = disputerLoginsAndChatListRows.get(loginTextArea.getText());
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
        return anoWindow.getDb().selectIdLoginPasswordForUser(login);
    }
}
