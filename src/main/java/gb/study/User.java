package gb.study;

import java.util.ArrayList;
import java.util.Arrays;

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

    private ArrayList<ChatListRow> chatListRows;
    public ArrayList<ChatListRow> getChatListRows() {
        return chatListRows;
    }
    public void setChatListRows(ArrayList<ChatListRow> chatListRows) {
        this.chatListRows = chatListRows;
    }

    private ArrayList<Chat> chats;

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
        this.chatListRows = parseChatListRows(anoWindow);
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
     * Получает список диалогов Chat, в которых состоит пользователь user,
     * обработав результаты выгрузки из БД и приведя их к нужному типу
     * @param anoWindow главное окно со всеми его свойствами,
     *                  в том числе с БД, которая необходима для работы метода
     * @return список диалогов ChatListRow, выгруженный из БД и приведенный к нужному типу
     */
    public ArrayList<ChatListRow> parseChatListRows(AnoWindow anoWindow) {
        ArrayList<ChatListRow> chatListRows = new ArrayList<>();
        ArrayList<ArrayList<Object>> chatListWhereId = getChatListRowsFromDB(this, anoWindow);
        //приведение выгрузки из БД к нормальному типу
        for (var chatListCells : chatListWhereId) {
            chatListRows.add(
                    //конструктор проверит, если такой записи нет в БД, то создаст ее.
                    new ChatListRow(
                            (Integer)chatListCells.get(1),    //user1
                            (Integer)chatListCells.get(2),    //user2
                            (String)chatListCells.get(4),     //comment
                            anoWindow
                    )
            );
        }
        return chatListRows;
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

    //todo наверно стоит соединить с parseChatListRows(..) в один метод, чтоб не дублировать запрос в БД
    // с другой стороны - это по идее один раз при регистрации пользователя - ДУМАТЬ!
    // Но как ни крути не нравится дублирование запроса в БД
    private ArrayList<Chat> parseChats(AnoWindow anoWindow) {
        ArrayList<Chat> chats = new ArrayList<>();
        ArrayList<ArrayList<Object>> chatListWhereId = getChatListRowsFromDB(this, anoWindow);
        for (var chatListCells : chatListWhereId) {
            chats.add(new Chat((String)chatListCells.get(3), anoWindow));
        }
        return chats;
    }
}
