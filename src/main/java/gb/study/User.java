package gb.study;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class User {
    private Integer id;        //заполняется при добавлении в БД, выгрузкой из неё присвоенного id
    public Integer getId() {
        return id;
    }

    private String login;   //тоже уникально, но неудобно для использования - например создание наименования чата
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


    private LinkedHashMap<Integer, Chat> chats;
    public LinkedHashMap<Integer, Chat> getChats() {
        return chats;
    }

    public User(
            String login, String password,
            String firstName, String lastName,
            String mail, String phone, String comment,
            AnoWindow anoWindow
    ) {
        System.out.println("--**-- Конструктор User");
        this.login = login;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mail = mail;
        this.phone = phone;
        this.comment = comment;
        // получить из БД присвоенный id, добавив данные в БД, если их там нет
        this.id = parseIdFromBD(anoWindow);
        disputersUpdate(anoWindow);
        // в результате объекты чатов созданы, но сообщения не загружены (будут загружены при открытии диалога)
    }

    /**
     * Метод, получающий из БД id, присвоенный для User,
     * обработав результаты выгрузки из БД и приведя их к нужному типу
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с БД, которая необходима для работы метода
     * @return id, выгруженный из БД и приведенный к нужному типу
     */
    private Integer parseIdFromBD(AnoWindow anoWindow){
        // выгрузка из БД - это всегда таблица объектов
        ArrayList<ArrayList<Object>> userId = getIdFromDB(anoWindow);
        return (Integer) userId.get(0).get(0);
    }
    /**
     * Метод, получающий id из таблицы user в БД.
     * Если необходимого пользователя не существует, то
     * создаст и загрузит в БД нового пользователя (появится id).
     * Если пользователь есть, то проверит правильность пароля.
     * Выгрузит из БД отчет с присвоенным id.
     * Все полученные данные - таблица Object
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с БД, которая необходима для работы метода
     * @return id, присвоенный в БД, для user
     */
    private ArrayList<ArrayList<Object>> getIdFromDB(AnoWindow anoWindow){
        //todo нужно вытащить логику выше по уровню, и добавить еще методов: инсерт, пароль и т.п.
        ArrayList<ArrayList<Object>> reportWithIdLogPass = anoWindow.getDb().selectIdLoginPasswordForUser(this);
        //Если такого пользователя в БД нет, то добавить и настроить
        if(reportWithIdLogPass.isEmpty()) {
            //todo добавить предупреждение, что пользователя нет мол добавить?
            anoWindow.getDb().insertNewUserAndConfigure(this);
            // теперь юзер 100% есть => снова запросить id в таблице User
            reportWithIdLogPass = anoWindow.getDb().selectIdLoginPasswordForUser(this);
        }
        //todo СДЕЛАТЬ ПРОВЕРКУ ЛОГИНА и ПАРОЛЯ
        checkLoginPassword();

        // предварительно очистив таблицу выгрузки от логина и пароля (на всякий);
        reportWithIdLogPass.set(0, new ArrayList<Object>(Arrays.asList(reportWithIdLogPass.get(0).get(0), null, null)));
        return reportWithIdLogPass;
    }
    //пока что заглушка
    private boolean checkLoginPassword() {return true;}

    /**
     * Обновит словари собеседников: disputerIdsAndChatListRows, disputerLoginsAndChatListRows, chats
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с БД, которая необходима для работы метода
     */
    protected void disputersUpdate(AnoWindow anoWindow) {
        System.out.println("--**-- Метод disputersUpdate");
        //todo НАДО ВСЁ ПРОВЕРИТЬ НА ПУСТЫЕ ВОЗВРАТЫ
        this.disputerIdsAndChatListRows = parseDisputerIdsAndChatListRows(anoWindow);
        this.disputerLoginsAndChatListRows = parseDisputerLoginsAndChatListRows(anoWindow);
        // ВНИМАНИЕ! В словаре chats: Integer - это id собеседника
        this.chats = parseChats(anoWindow);
        // в результате объекты чатов созданы, но сообщения не загружены (будут загружены при открытии диалога)
    }

    /**
     * Получает словарь id собеседников -> запись о диалогах, в которых состоит пользователь user,
     * обработав результаты запроса к таблице записей о диалогах из БД и приведя их к нужному типу
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с БД, которая необходима для работы метода
     * @return словарь id собеседников -> запись о диалогах
     */
    public LinkedHashMap<Integer, ChatListRow> parseDisputerIdsAndChatListRows(AnoWindow anoWindow) {
        var disputerIdsAndChatListRows = new LinkedHashMap<Integer, ChatListRow>();
        ArrayList<ArrayList<Object>> chatListWhereId = getChatListRowsFromDB(this, anoWindow);
        //приведение выгрузки из БД к нормальному типу
        for (var chatListCells : chatListWhereId) {
            //конструктор проверит, если такой записи нет в БД, то создаст ее.
            ChatListRow chatListRow = new ChatListRow(
                    (Integer)chatListCells.get(1),    //user1
                    (Integer)chatListCells.get(2),    //user2
                    (String)chatListCells.get(4),     //comment
                    anoWindow
            );
            Integer disputerId = calculateDisputerId(chatListRow);
            disputerIdsAndChatListRows.put(disputerId, chatListRow);
            System.out.println("Обнаружен чат с " + disputerId + " - " + chatListRow.getTableName());
        }
        return disputerIdsAndChatListRows;
    }
    /**
     * Метод, получающий из БД список записей о диалогах для пользователя.
     * Все полученные данные - таблица Object
     * @param user пользователь, для которого необходимо получить записи
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с БД, которая необходима для работы метода
     * @return список записей о диалогах
     */
    private ArrayList<ArrayList<Object>> getChatListRowsFromDB(User user, AnoWindow anoWindow) {
        return anoWindow.getDb().selectAllChatListRowWhereId(user);
    }

    /**
     * Получает словарь login собеседников -> запись о диалогах, в которых состоит пользователь user,
     * обработав результаты запроса к таблице пользователей из БД и приведя их к нужному типу
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с БД, которая необходима для работы метода
     * @return словарь login собеседников -> запись о диалогах
     */
    private LinkedHashMap<String, ChatListRow> parseDisputerLoginsAndChatListRows(AnoWindow anoWindow) {
        System.out.println("--**-- Метод parseDisputerLoginsAndChatListRows");
        var disputerLoginsAndChatListRows = new LinkedHashMap<String, ChatListRow>();
        var disputerIds = new ArrayList<Integer>(disputerIdsAndChatListRows.keySet());
        ArrayList<ArrayList<Object>> disputerIdsAndLogins = getIdsAndLoginsFromDB(disputerIds, anoWindow);
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
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с БД, которая необходима для работы метода
     * @return
     */
    private ArrayList<ArrayList<Object>> getIdsAndLoginsFromDB(ArrayList<Integer> disputerIds, AnoWindow anoWindow) {
        System.out.println("--**-- Метод getIdsAndLoginsFromDB");
        return anoWindow.getDb().selectIdsAndLoginsForIds(disputerIds);
    }

    /**
     * Создает словарь id собеседника->Чат для пользователя, не обращаясь к БД,
     * на основе уже заполненного словаря disputerIdsAndChatListRows (id собеседника->запись о диалоге)
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с БД, которая необходима для работы метода
     * @return словарь id собеседника->Чат для пользователя
     */
    private LinkedHashMap<Integer, Chat> parseChats(AnoWindow anoWindow) {
        var chats = new LinkedHashMap<Integer, Chat>();
        for (var disputerIdAndChatListRow : disputerIdsAndChatListRows.entrySet()) {
            Integer disputerId = disputerIdAndChatListRow.getKey();
            Chat disputerChat = new Chat(disputerIdAndChatListRow.getValue().getTableName(), anoWindow);
            chats.put(disputerId, disputerChat);
        }
        return chats;
    }

    protected void addNewDisputerFromDBNotify(String[] chatListRowParts, AnoWindow anoWindow){
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
        ArrayList<ArrayList<Object>> disputerIdsAndLogins = getIdsAndLoginsFromDB(disputerIds, anoWindow);
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
                CompletableFuture<Void> futureNewListenerNewMessage = listenerNewMessageAsync(
                        chatListRows,
                        anoWindow
                );
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
     * Запустить все прослушивания (как новых чатов, так и сообщений)
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с БД, которая необходима для работы метода
     */
    public void startListening(AnoWindow anoWindow) {
        // 1. Запустить асинхронное прослушивание этих каналов:
        CompletableFuture<Void> futureListenerNewMessage = listenerNewMessageAsync(
                new ArrayList<>(disputerLoginsAndChatListRows.values()),
                anoWindow
        );
        // 2. Запуск асинхронное прослушивание нового чата с неизвестным
        CompletableFuture<Void> futureListenerNewDisputer = listenerNewDisputerAsync(
                new ArrayList<>(disputerLoginsAndChatListRows.values()),
                anoWindow
        );
    }
    /**
     * Асинхронный метод прослушивания уведомлений о новых сообщениях
     * @param chatListRows записи о диалогах, которые необходимо прослушивать
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с БД, которая необходима для работы метода
     * @return
     */
    private CompletableFuture<Void> listenerNewMessageAsync(ArrayList<ChatListRow> chatListRows, AnoWindow anoWindow) {
        return CompletableFuture.runAsync(() -> {
            System.out.println("Прослушивание уведомлений о новых сообщениях запущено.");
            anoWindow.getDb().startListenerNewMessage(chatListRows, anoWindow);
        });
    }
    /**
     * Асинхронный метод прослушивания уведомлений о добавлении новой записи о диалоге (чате)
     * @param chatListRows записи о диалогах, которые необходимо прослушивать
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с БД, которая необходима для работы метода
     * @return
     */
    private CompletableFuture<Void> listenerNewDisputerAsync(ArrayList<ChatListRow> chatListRows, AnoWindow anoWindow) {
        return CompletableFuture.runAsync(() -> {
            System.out.println("Прослушивание уведомлений о добавлении новой записи о диалоге запущено.");
            anoWindow.getDb().startListenerNewChatListRow(chatListRows, anoWindow);
        });
    }
}
