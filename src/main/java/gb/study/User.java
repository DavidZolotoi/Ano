package gb.study;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

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

    private LinkedHashMap<Integer, ChatListRow> interlocutorIdsAndChatListRows;
    public LinkedHashMap<Integer, ChatListRow> getInterlocutorIdsAndChatListRows() {
        return interlocutorIdsAndChatListRows;
    }

    private LinkedHashMap<String, ChatListRow> interlocutorLoginsAndChatListRows;
    public LinkedHashMap<String, ChatListRow> getInterlocutorLoginsAndChatListRows() {
        return interlocutorLoginsAndChatListRows;
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

    public User(
            String login, String password,
            String firstName, String lastName,
            String mail, String phone, String comment,
            AnoWindow anoWindow
    ) {
        this.login = login;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mail = mail;
        this.phone = phone;
        this.comment = comment;
        // получить из БД присвоенный id, добавив данные в БД, если их там нет
        this.id = parseIdFromBD(anoWindow);
        this.interlocutorIdsAndChatListRows = parseInterlocutorIdsAndChatListRows(anoWindow);
        this.interlocutorLoginsAndChatListRows = parseInterlocutorLoginsAndChatListRows(anoWindow);
        // имея список чатов, todo создать коллекцию чатов (имена таблиц уже есть в chatListRows)
        this.chats = parseChats(anoWindow);
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
        //todo в теории можно вытащить логику выше по уровню, и добавить еще методов: инсерт, пароль и т.п.
        ArrayList<ArrayList<Object>> reportWithIdLogPass = anoWindow.getDb().selectIdLoginPasswordForUser(this);
        //Если такого пользователя в БД нет, то добавить и настроить
        if(reportWithIdLogPass.isEmpty()) {
            anoWindow.getDb().insertNewUserAndConfigure(this);
            // теперь юзер 100% есть => снова запросить id в таблице User
            reportWithIdLogPass = anoWindow.getDb().selectIdLoginPasswordForUser(this);
        }
        //todo СДЛАТЬ ПРОВЕРКУ ЛОГИНА и ПАРОЛЯ
        checkLoginPassword();

        // предварительно очистив таблицу выгрузки от логина и пароля (на всякий);
        reportWithIdLogPass.set(0, new ArrayList<Object>(Arrays.asList(reportWithIdLogPass.get(0).get(0), null, null)));
        return reportWithIdLogPass;
    }
    //пока что заглушка
    private boolean checkLoginPassword() {return true;}

    /**
     * Получает словарь id собеседников -> запись о диалогах, в которых состоит пользователь user,
     * обработав результаты запроса к таблице записей о диалогах из БД и приведя их к нужному типу
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с БД, которая необходима для работы метода
     * @return словарь id собеседников -> запись о диалогах
     */
    public LinkedHashMap<Integer, ChatListRow> parseInterlocutorIdsAndChatListRows(AnoWindow anoWindow) {
        LinkedHashMap<Integer, ChatListRow> interlocutorIdsAndChatListRows = new LinkedHashMap<>();
        ArrayList<ArrayList<Object>> chatListWhereId = getChatListRowsFromDB(this, anoWindow);
        //приведение выгрузки из БД к нормальному типу
        for (var chatListCells : chatListWhereId) {
            System.out.println("--- Создание chatListRow");
            //конструктор проверит, если такой записи нет в БД, то создаст ее.
            ChatListRow chatListRow = new ChatListRow(
                    (Integer)chatListCells.get(1),    //user1
                    (Integer)chatListCells.get(2),    //user2
                    (String)chatListCells.get(4),     //comment
                    anoWindow
            );
            Integer interlocutorId = calculateInterlocutorId(chatListRow);
            interlocutorIdsAndChatListRows.put(interlocutorId, chatListRow);
        }
        return interlocutorIdsAndChatListRows;
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
    private LinkedHashMap<String, ChatListRow> parseInterlocutorLoginsAndChatListRows(AnoWindow anoWindow) {
        LinkedHashMap<String, ChatListRow> interlocutorLoginsAndChatListRows = new LinkedHashMap<>();
        ArrayList interlocutorIds = new ArrayList<>(interlocutorIdsAndChatListRows.keySet());
        ArrayList<ArrayList<Object>> interlocutorIdsAndLogins = getIdsAndLoginsFromDB(interlocutorIds, anoWindow);
        for (ArrayList<Object> interlocutorIdAndLogin : interlocutorIdsAndLogins) {
            String interlocutorLogin = (String) interlocutorIdAndLogin.get(1);
            Integer interlocutorId = (Integer) interlocutorIdAndLogin.get(0);
            ChatListRow interlocutorChatListRow = interlocutorIdsAndChatListRows.get(interlocutorId);
            interlocutorLoginsAndChatListRows.put(
                    interlocutorLogin,
                    interlocutorChatListRow
            );
        }
        return interlocutorLoginsAndChatListRows;
    }
    /**
     * Метод, получающий из таблицы пользователей БД для данных id, две колонки: id и login
     * Все полученные данные - таблица Object
     * @param interlocutorIds список id, для которых нужно получить результат
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с БД, которая необходима для работы метода
     * @return
     */
    private ArrayList<ArrayList<Object>> getIdsAndLoginsFromDB(ArrayList interlocutorIds, AnoWindow anoWindow) {
        return anoWindow.getDb().selectIdsAndLoginsForIds(interlocutorIds);
    }

    /**
     * Создает словарь id собеседника->Чат для пользователя, не обращаясь к БД,
     * на основе уже заполненного словаря interlocutorIdsAndChatListRows (id собеседника->запись о диалоге)
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с БД, которая необходима для работы метода
     * @return словарь id собеседника->Чат для пользователя
     */
    private LinkedHashMap<Integer, Chat> parseChats(AnoWindow anoWindow) {
        LinkedHashMap<Integer, Chat> chats = new LinkedHashMap<>();
        for (var interlocutorIdAndChatListRow : interlocutorIdsAndChatListRows.entrySet()) {
            Integer interlocutorId = interlocutorIdAndChatListRow.getKey();
            Chat interlocutorChat = new Chat(interlocutorIdAndChatListRow.getValue().getTableName(), anoWindow);
            chats.put(interlocutorId, interlocutorChat);
        }
        return chats;
    }

    /**
     * Рассчитывает id собеседника для пользователя, имея запись о диалоге
     * @param chatListRow запись о диалоге
     * @return id собеседника
     */
    public Integer calculateInterlocutorId(ChatListRow chatListRow){
        return chatListRow.getUserIdMin() + chatListRow.getUserIdMax() - id;
    }
}
