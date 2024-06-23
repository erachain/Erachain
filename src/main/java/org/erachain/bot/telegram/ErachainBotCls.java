package org.erachain.bot.telegram;

import com.google.gson.Gson;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.message.origin.*;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.FileUtils;
import org.erachain.utils.Pair;
import org.erachain.utils.SaveStrToFile;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.erachain.core.item.assets.AssetCls.FEE_KEY;
import static org.erachain.core.transaction.Transaction.VALIDATE_OK;

/**
 * Бот для Телеграм.
 * see https://github.com/pengrad/java-telegram-bot-api?tab=readme-ov-file#creating-your-bot
 * set Environment variable [bot_name]=[TOKEN]
 * В файле настроек [bot_name].json нужно задать основной Сид из которого будут создаваться ключи для групп, поле baseSeed. Если его не задать он создаётся сам. Но его нужно потом сохранить - чтобы не потерять связь со счетами чатов
 * Chat.userName - это ссылка на чат или имя бота. Если ссылка не задано то имя чата Chat.title
 */
abstract public class ErachainBotCls {

    private static final Logger log = LoggerFactory.getLogger(ErachainBotCls.class.getSimpleName());
    Controller cnt;
    Settings settingsInstance;
    Lang langList;
    TelegramBot bot;
    static Gson GSON = new Gson();

    private JSONObject settingsJSON;
    private JSONObject settingsChats;
    private byte[] baseSeed;

    private GetMeResponse meInfo;
    protected String settingsName;
    protected String settingsPath;
    protected String botTitle;
    protected String botUserName;
    protected String botUserNameF;
    protected String botUserNameMD;
    protected String botUserNameMD1;

    private long SEE_REPIOD;
    // максимальная длинна очереди транзакций для слежения на получение подтверждения
    private final int MAX_PENDING_COUNT = 1000;
    private final int PENDING_CONFIRMS = 3;

    private final static String PRIVATE_CHAT_ID = "privateChatId";
    private final static String PRIVATE_KEY_SEED = "privSeed";

    String commandsHelp;

    private PrivateKeyAccount myPrivacyKey;
    protected Long adminChatId;

    public boolean test;

    /**
     * Быстрый поис ИД чата по счету
     */
    public final static ConcurrentHashMap<String, String> knownAddress = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String, String> knownPubKey = new ConcurrentHashMap<>();

    public ErachainBotCls(Controller cnt) {

        settingsInstance = Settings.getInstance();
        settingsName = "bot_" + getShortName();
        settingsPath = "settings_" + settingsName + ".json";

        boolean saveSettings = false;
        String botToken;

        try {
            try {
                settingsJSON = FileUtils.readCommentedJSONObject(settingsPath);
                ((JSONObject) settingsJSON.get("chats")).forEach((key, value) -> {
                    JSONObject chat = (JSONObject) value;
                    knownPubKey.put((String) chat.get("pubKey"), (String) key);
                    knownAddress.put((String) chat.get("address"), (String) key);
                });

            } catch (Exception e) {
                settingsJSON = new JSONObject();
                settingsChats = new JSONObject();
                settingsJSON.put("chats", settingsChats);
                saveSettings = true;
            }

            String parsTokenName = settingsName + "_token";
            botToken = (String) settingsJSON.get(parsTokenName);
            botToken = botToken == null ? System.getProperty(parsTokenName, System.getenv(parsTokenName)) : botToken;
            if (botToken == null) {
                log.warn("Token for bot [" + parsTokenName + "] not found in parameters, skip start... Use -D" + parsTokenName + "=...");
                return;
            }

            log.info("Try start [" + settingsName + "], used " + settingsPath);

            this.cnt = cnt;

            if (settingsJSON.isEmpty() || !settingsJSON.containsKey("baseSeed")) {
                // Создадим независимый от нашего кошелька СИД для строгой математической зависимости счетов Каналов от него.
                // Его надо потом сохранить чтобы не потерять доступ ко всем счетам Чатов
                SecureRandom random = new SecureRandom();
                byte[] baseSeed = new byte[32];
                random.nextBytes(baseSeed);

                settingsJSON.put("baseSeed", Base58.encode(baseSeed));
                saveSettings = true;

            }

            baseSeed = Base58.decode((String) settingsJSON.get("baseSeed"));

            if (settingsJSON.isEmpty() || !settingsJSON.containsKey("chats")) {
                settingsChats = new JSONObject();
                settingsJSON.put("chats", settingsChats);
                saveSettings = true;
            } else {
                settingsChats = (JSONObject) settingsJSON.get("chats");
            }

        } finally {
            if (saveSettings)
                saveSettings();
        }

        bot = new TelegramBot(botToken);

        // {"result":{"id":7316184958,"is_bot":true,"first_name":"Blockchain Storage","username":"blockchain_storage_bot","can_join_groups":true,"can_read_all_group_messages":false,"supports_inline_queries":false,"can_connect_to_business":false},"ok":true,"error_code":0}
        try {
            meInfo = bot.execute(new GetMe());
        } catch (Exception e) {
            log.warn(e.getMessage());
            stop();
            return;
        }

        if (!meInfo.isOk()) {
            log.warn("GetMy: " + GSON.toJson(meInfo).toString() + " - canceled");
            stop();
            return;
        }

        // Подписка на обновления
        bot.setUpdatesListener(updates -> {
            // Обработка обновлений
            updates.forEach(update -> onUpdateReceived(update));
            // return id of last processed update or confirm them all
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
            // Создание Обработчика ошибок
        }, e -> {
            if (e.response() != null) {
                // Ошибка из Телеграма
                log.warn("TELEGRAM ERR: " + e.response().errorCode() + " - " + e.response().description());
            } else {
                // Как видно проблема сети
                e.printStackTrace();
            }
        });

        botUserName = meInfo.user().username();
        botTitle = meInfo.user().firstName();
        botUserNameF = "@" + botUserName;
        botUserNameMD1 = "`" + botUserNameF + "`";
        botUserNameMD = "@" + botUserName.replace("_", "\\_");
        log.warn(botUserName + " started!");
        log.warn("GetMy: " + GSON.toJson(meInfo).toString());

        // Для посылки сообщений админу - нужно задать adminChatId в настройках!
        adminChatId = (Long) settingsJSON.get("adminChatId");
        sendToAdminMessage("Started...");
        settingsChats.keySet().forEach(chatId -> {
            if (((JSONObject) settingsChats.get(chatId)).get("userName") != null)
                // пользователям не пишем
                return;
            sendMarkdown(Long.parseLong((String) chatId), "Я запустился... " + botUserNameMD1);
        });

        try {
            // resources/ErachainStorageBot/commandsHelp.md
            //File file = new File("resources/ErachainStorageBot/commandsHelp.md");
            String file = System.getProperty("user.dir") + "/resources/ErachainStorageBot/commandsHelp.md";
            commandsHelp = FileUtils.readFileAsString(file);
            commandsHelp = commandsHelp.replace("\n", " ");
        } catch (IOException e) {
            commandsHelp = "???";
            return;
        }

        langList = Lang.getInstance();

        // опрос блокчейн для проверки подтверждения транзакций
        SEE_REPIOD = cnt.blockChain.GENERATING_MIN_BLOCK_TIME_MS(cnt.getMyHeight());
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(SEE_REPIOD);
                } catch (InterruptedException e) {
                    return;
                }
                try {
                    updateTransactions();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
        thread.setName(botUserName + "_thread");
        thread.start();

    }

    GetUpdates getUpdates = new GetUpdates().limit(100).offset(0).timeout(1);

    protected StringBuilder scanUrl(String item, Object key) {
        StringBuilder url = new StringBuilder();
        url.append(settingsInstance.getBlockexplorerURL()).append("/index/blockexplorer.html?").append(item);
        if (key != null)
            url.append("=" + key);
        return url;
    }

    protected String scanUrlMarkdown(String text, String item, Object key) {
        StringBuilder url = new StringBuilder();
        url.append("[").append(text).append("](").append(scanUrl(item, key)).append(")");
        return url.toString();
    }

    abstract protected String getShortName();

    abstract protected boolean processMessage(Long makerTxId, Long replyChatId, Chat chatMain, Message message, String lang);

    abstract protected void makeEntitiesCommands(String text, List<MessageEntity> commands, MessageEntity entity);

    abstract protected boolean processCommand(Long makerTxId, Long replyChatId, Integer replyMessageId, Chat origChat, Integer origMessageId, Integer origMessageDate, Message message, String command, String text, JSONObject out, String lang);

    public void process() {
        GetUpdatesResponse updatesResponse = bot.execute(getUpdates);
        List<Update> updates = updatesResponse.updates();
        updates.forEach(update -> onUpdateReceived(update));
    }

    String getUserIdAndName(User user) {
        StringBuilder out = new StringBuilder();
        out.append(user.id()).append(":").append(user.username() == null ? user.firstName() + (user.lastName() == null ? "" : user.lastName()) : user.username());
        return out.toString();
    }

    // UpdateReact
    protected void onUpdateReceived(Update update) {

        long chatErrorId = -1;
        try {

            Chat chat;
            Chat origMessageChat;
            User from;
            User user;
            Long chatId;
            Long userId;
            String text;

            Integer updateId = update.updateId();
            String lang = "RU";

            Message message = update.message();
            String userName;
            if (message != null) {
                // сюда приходит если:
                // или прямой чат пользователя с ботом
                // или в чате группы/супергруппы (не канал) бот добавлен администратором

                chat = message.chat();
                chatId = chat.id();
                chatErrorId = chatId;

                if (Boolean.TRUE.equals(message.isAutomaticForward())) {
                    // это сообщение пришло из канала к которому привязан данный чат (супергруппа)
                    // это основной чат откуда пришло сообщение - с именем и правильным UserName
                    origMessageChat = message.senderChat();
                    if (origMessageChat.type().equals(Chat.Type.channel)) {
                        // при этом message.from() - будет id=777000 firstName= "Telegram"
                        // поэтому берем Имя основного чата - и счет из него будем делать
                        // userName = origMessageChat.username();
                        if (this.processMessage(chatId, chatId, origMessageChat, message, lang))
                            return;
                        return;
                    }

                    if (chat.type().equals(Chat.Type.supergroup) // публичная группа
                            || chat.type().equals(Chat.Type.group)) { // не публичная - только по приглашению
                        from = message.from();
                        // в супергруппах, которые прикрепляются как чат для каналов, в чат сообщения из канала пишет бот, поэтому все сообщения не от ботов игнорируем
                        if (!from.isBot()) {
                            return;
                        }

                        if (from.isBot()) {
                            if (message.newChatMembers() != null && message.newChatMembers().length > 0) {
                                // прилетает сюда при добавлении бота в супергруппу
                                return;
                            }
                            return;
                        }
                        return;

                    } else {
                        user = message.from();
                        userId = user.id();
                        userName = user.firstName();
                        text = message.text();
                        chatId = chat.id();
                        if (text != null) {
                            if (botAnswerPublic(text, chat, user))
                                return;
                        }

                    }

                    sendGreetingsMessage(chatId, lang);
                    return;

                } else {
                    // это сообщение пользователя или другого бота - но не из головного канала
                    text = message.text();
                    chatErrorId = chatId;
                    if (// даже Яндекс ничего сними сделать не может - это чисто встроенная фишка
                            message.forwardOrigin() == null &&
                                    (text == null || text.isEmpty())) {
                        // сюда приходит если было например изменение названия группы (супергруппы)
                        return;
                    }

                    if (chat.type().equals(Chat.Type.Private)) {
                        // в приватном чате не нужно писать Имя бота в сообщениях - поэтому сразу команды обрабатываем
                        user = message.from();

                        // запомним ИД приватного чата - чтобы напрямую пользователю потом писать
                        JSONObject settings = getChatSettings(user.id());
                        Long privateCharId = (Long) settings.get(PRIVATE_CHAT_ID);
                        if (privateCharId == null) {
                            settings.put(PRIVATE_CHAT_ID, user.id());
                            settings.put("userName", user.username());
                            saveSettings();
                        }

                        userName = user.firstName();
                        if (message.forwardOrigin() != null) {
                            Integer origMessageId; // ссылка на пересланное сообщение
                            Chat origChat;
                            Long origChatId; // откуда сообщение
                            Integer origMessageDate;
                            MessageOrigin forwardOrigin = message.forwardOrigin();
                            MessageIdResponse response; // from = chat - user Me // caption
                            if (message.caption() != null)
                                text = text == null ? message.caption() : text + "\n\n> " + message.caption();
                            // https://t.me/Onishchenko001/61166
                            if (forwardOrigin instanceof MessageOriginChannel) {
                                MessageOriginChannel messageOriginChannel = (MessageOriginChannel) message.forwardOrigin();
                                //origChatId = messageOriginChannel.chat().linkedChatId(); // а это что?
                                origChat = messageOriginChannel.chat();
                                origChatId = origChat.id();
                                origMessageId = messageOriginChannel.messageId();
                                origMessageDate = messageOriginChannel.date();

                            } else if (forwardOrigin instanceof MessageOriginChat) {
                                MessageOriginChat messageOriginChat = (MessageOriginChat) message.forwardOrigin();
                                origChat = messageOriginChat.senderChat(); // https://t.me/iuyiuyiuyiy/1075
                                origChatId = origChat.id();
                                origMessageId = null; // нет ИД на исходное сообщение из того чата (( message.messageId();
                                origMessageDate = messageOriginChat.date();

                            } else if (forwardOrigin instanceof MessageOriginUser) {
                                MessageOriginUser messageOriginUser = (MessageOriginUser) message.forwardOrigin();
                                origChat = null;
                                origChatId = null;
                                origMessageId = null;
                                origMessageDate = messageOriginUser.date();
                                from = messageOriginUser.senderUser();
                                text = getUserIdAndName(from) + ": " + message.text();

                            } else if (forwardOrigin instanceof MessageOriginHiddenUser) {
                                MessageOriginHiddenUser messageOriginHiddenUser = (MessageOriginHiddenUser) message.forwardOrigin();
                                origChat = null;
                                origChatId = null;
                                origMessageId = null;
                                origMessageDate = messageOriginHiddenUser.date();
                                text = messageOriginHiddenUser.senderUserName() + ": " + message.text();
                            } else {
                                // hidden name
                                log.warn("skip forwardOrigin: " + forwardOrigin.getClass().getSimpleName());
                                return;
                            }

                            if (false)
                                if ((test || settingsInstance.isTestNet()) && origChatId != null && origMessageId != null) {
                                    sendSimpleText(chatId, GSON.toJson(message));
                                    // всегда отвечает что оригинальное сообщение не доступно
                                    response = bot.execute(new CopyMessage(message.chat().id(), origChatId, origMessageId));
                                    if (response.errorCode() == 400) {
                                        // и больше данных нет error_code = 400 нет доступа к чтению для бота - но ИД и username для URL верные
                                        // Яндекс тоже пока не может такое обработать
                                        sendSimpleText(chatId, GSON.toJson(response));
                                    }
                                }

                            // в приватном чате одна ссылка только на сообщение
                            ///message.linkPreviewOptions(); // url на https://t.me/antifalivland/8946
                            if (text != null) {
                                this.processCommand(chatId, chatId, message.messageId(), origChat, origMessageId, origMessageDate, message, null, text, null, lang);
                                return;
                            }
                        } else if (message.linkPreviewOptions() != null) {
                            // https://t.me/Onishchenko001/61166?comment=1621294
                            //LinkPreviewOptions link = message.linkPreviewOptions();
                            this.processCommand(chatId, chatId, message.messageId(), null, null, null, message, null, null, null, lang);
                            return;
                        }
                        if (botAnswerPrivate(text.split(" "), chatId, chat, user))
                            return;

                        BaseResponse response = sendGreetingsPrivateMessage(chatId, user, lang);
                        return;
                    } else if (text.startsWith(botUserNameF)) {

                        // это сообщение боту из общего чата - видное всем
                        // @blockchain_storage_bot mode 3
                        String commandLine = text.length() == botUserNameF.length() ? "" : text.substring(botUserName.length() + 1).trim();
                        String[] command = commandLine.split(" ");
                        if (command.length == 1) {
                            // команда без паарметров - просто ответить как правило выдать Инфо или данные текущих настроек
                            if (botAnswerPublic(command[0], chat, message.from()))
                                return;

                        } else {
                            // как админ канала основного: User{id=136817688, is_bot=true, first_name='Channel', last_name='null', username='Channel_Bot', language_code='null', is_premium='null', added_to_attachment_menu='null', can_join_groups=null, can_read_all_group_messages=null, supports_inline_queries=null, can_connect_to_business=null}
                            // как анонимный администратор группы: User{id=1087968824, is_bot=true, first_name='Group', last_name='null', username='GroupAnonymousBot', language_code='null', is_premium='null', added_to_attachment_menu='null', can_join_groups=null, can_read_all_group_messages=null, supports_inline_queries=null, can_connect_to_business=null}
                            // как пользовательUser{id=19781---, is_bot=false, first_name='', last_name='null', username='', language_code='ru', is_premium='true', added_to_attachment_menu='null', can_join_groups=null, can_read_all_group_messages=null, supports_inline_queries=null, can_connect_to_business=null}
                            from = message.from();
                            if (from.isBot()) {
                                if (from.username().equals("Channel_Bot")) {
                                    // команды от админа канала
                                    botAnswerAdmin(command, chat, from);
                                    return;
                                } else if (from.username().equals("GroupAnonymousBot")) {
                                    // команды от админа чата
                                    // TODO проверить разрешение на управление от админа канала - а если это просто группа ьез канала?
                                    botAnswerAdmin(command, chat, from);
                                    return;
                                } else {
                                    // иной какой-то бот
                                }
                                return;
                            }
                        }

                        sendGreetingsMessage(chatId, lang);
                        return;
                    } else if (text.startsWith("/")) {
                        // это скорее всего команда боту быстрая
                        // если это ответ на другое - или есть общее обсуждение
                        if (message.replyToMessage() != null) {
                            Message reply = message.replyToMessage();
                            //from = reply.from();
                            from = message.from();
                            lang = from.languageCode();
                            if (from.isBot()) {
                                if ("GroupAnonymousBot".equals(from.username()) || "Channel_Bot".equals(from.username())) {
                                    // это админ канала - chatId не меняем
                                    if (this.processCommand(chatId, chatId, message.messageId(), reply.chat(), reply.messageId(), reply.date(), reply, ":" + text.substring(1), null, null, lang))
                                        return;
                                }
                            } else {
                                // чат для общения приватный и счет под него генерим
                                JSONObject settingsPrivateChat = getChatSettings(from.id());
                                Long privateChatId = (Long) settingsPrivateChat.get(PRIVATE_CHAT_ID);
                                userName = from.username(); // icreator
                                if (privateChatId == null) {
                                    sendMarkdown(chatId, userName + ", для использования, сперва настройте под себя " + botUserNameMD1);
                                    return;
                                } else {
                                    //chatId = from.id(); - если сообщение катать в приватный чат пользователя
                                    if (this.processCommand(
                                            // Ответ о записи в блокчейн в том же чате на нашу команду будет:
                                            chatId, chatId, message.messageId(),
                                            reply.chat(), reply.messageId(), reply.date(), reply, ":" + text.substring(1), null, null, lang))
                                        return;
                                }
                            }
                        } else {
                        }

                        if (message.replyToStory() != null) {
                            Story reply = message.replyToStory();
                        } else if (message.messageThreadId() != null) {
                            ;
                        }
                    }


                    if (chat.type().equals(Chat.Type.supergroup) // публичная группа
                            || chat.type().equals(Chat.Type.group)) { // не публичная - только по приглашению
                        MessageEntity[] entities = message.entities();
                        if (false) {
                            // ели использовать внутренний механизм выявления Команд по message.entities() - то русские команды туда не попадают ((
                            List<MessageEntity> commands = new ArrayList<>();
                            if (entities != null && entities.length > 0) {
                                for (int i = 0; i < entities.length; i++) {
                                    MessageEntity entity = entities[i];
                                    if (entity.type().equals(MessageEntity.Type.bot_command)) {
                                        makeEntitiesCommands(text, commands, entity);
                                    }
                                }
                            }
                            // тут обработчик на основе выявленных команд надо доделать
                        }

                        // проверку админства - по getChatAdministrators и getChatMember
                        // если это админы то от имени чата сохраняем - иначе оот имени пользователя
                        // ChatMemberOwner и ChatMemberAdministrator + can_manage_chat
                        from = message.from();
                        GetChatMember request = new GetChatMember(chatId, from.id());
                        GetChatMemberResponse response = bot.execute(request);
                        if (!response.isOk()) {
                            // что пошло не так
                            sendMarkdown(chatId, "```java " + response + "```");
                            return;
                        }
                        ChatMember chatMember = response.chatMember();
                        if (chatMember.status().equals(ChatMember.Status.creator)
                                || chatMember.status().equals(ChatMember.Status.administrator) && chatMember.canManageChat()) {
                            chatId = message.chat().id();
                        } else {
                            chatId = from.id();
                        }

                        if (this.processMessage(chatId, chat.id(), chat, message, lang))
                            return;
                    }
                }
            }

            MessageReactionUpdated reactions = update.messageReaction();

            message = update.channelPost();
            if (message != null) {
                // Сообщения из канала, где бот администратором добавлен
                chat = message.senderChat();
                chatId = chat.id();
                chatErrorId = chat.id();
                Integer message_id = message.messageId();
                Integer date = message.date();
                Chat.Type type = chat.type();
                String title = chat.title();
                if (chat.type().equals(Chat.Type.supergroup)) {
                    // тут chat.username() = NULL
                    userName = "" + chatId;
                } else {
                    userName = chat.username();
                }
                String author = message.authorSignature();
                text = message.text();
                MessageEntity[] entites = message.entities();
                LinkPreviewOptions link_preview_options = message.linkPreviewOptions();

                if (text.startsWith(botUserName)) {
                    String commandLine = text.substring(botUserName.length()).trim();
                    String[] command = commandLine.split(" ");
                    command[0] = command[0].toLowerCase();
                    if (command.length == 1) {
                        // это команды получить данные
                        if (command[0].equals("account")) {
                            sendChatAccountMessage(chat, lang);
                        }
                        return;
                    } else {
                        from = message.from();
                        if (from.isBot())
                            return;
                        // проверить админские права

                        if (command[0].equals("account")) {
                            // @blockchain_storage_bot account new
                            // задает SEED для чата
                            sendChatAccountMessage(chat, lang);
                        }

                    }

                    return;
                }

                if (this.processMessage(chatId, chatId, chat, message, lang))
                    return;

                //sendMessage(chatId, message);

                return;
            }

            CallbackQuery callbackQuery = update.callbackQuery();
            if (callbackQuery != null) {
                chatId = callbackQuery.from().id();
                //если нажата одна из кнопок бота
                String queryId = callbackQuery.id();
                userId = callbackQuery.from().id();
                user = callbackQuery.from();
                String receivedMessage = callbackQuery.data();
                botAnswerPublic(receivedMessage, null, user);
                return;
            }

            // приглашения удаления из групп
            ChatMemberUpdated myChatMember = update.myChatMember();
            if (myChatMember != null) {
                chat = myChatMember.chat(); // в каком чате действие произошло
                chatId = chat.id();
                chatErrorId = chat.id();
                from = myChatMember.from(); // кто это сделал

                //log.warn("Chat: " + GSON.toJson(chat).toString());
                log.warn("User from: " + GSON.toJson(from).toString());

                ChatMember newChatMember = myChatMember.newChatMember();
                user = newChatMember.user();
                ChatMember.Status oldStatus = myChatMember.oldChatMember().status();
                ChatMember.Status newStatus = newChatMember.status();
                if (oldStatus.equals(newStatus))
                    return;

                if (user.username().equals(botUserName)) {

                    if (newStatus.equals(ChatMember.Status.kicked) || newStatus.equals(ChatMember.Status.left)) {
                        // Удалили из группы
                        Integer untilDate = newChatMember.untilDate(); // 0 - просто удалили
                        log.error("kicked from " + chat.title() + " by User " + from.username());

                        // Конкретный пользователь известен только когда удаляют администратора! Иначе бот кикает и не понятно кто тебя кикнул
                        if (!from.isBot())
                            sendSimpleText(from.id(), from.username() + ", спасибо за использование наших услуг в чате \"" + chat.title() + "\". Надеемся на продолжение сотрудничества в будущем.");

                    } else if (newStatus.equals(ChatMember.Status.administrator)) {
                        // Тут известен конкретны пользователь
                        if (!from.isBot())
                            sendSimpleText(from.id(), from.username() + ", спасибо за допуск меня к работе в чате \"" + chat.title() + "\". Надеюсь быть полезным...");

                        sendGreetingsMessage(chatId, lang);

                        // В приветный чат вышлем данные о счете по данному Имени чата
                        sendChatAccountMessage(chat, lang);
                        sendBotAccountMessage(chat.id(), lang);
                        //sendAccountChatMessage(from.id(), chat, lang);

                    } else if (newStatus.equals(ChatMember.Status.member)) {
                        if (!oldStatus.equals(ChatMember.Status.administrator)) {
                            sendGreetingsMessage(chatId, lang);
                        }
                        // тут from.username() - это чат бот группы GroupAnonymousBot - бес толку ему что-то лично слать
                        // если нас несли - до этого нас не было в группе:
                        sendOnSetMemberStatus(chatId, newChatMember);

                        if (!from.isBot())
                            sendSimpleText(from.id(), from.username() + ", моя работа в чате \"" + chat.title() + "\" прекращена. Жду назначения администраторам снова...");

                    }

                    JSONObject chatSettings = getChatSettings(chatId);
                    chatSettings.put("status", newStatus.name());
                    saveSettings();

                } else {
                    // Это не про меня - хотя ему можно сообщение написать
                    if (user.isBot())
                        return;

                    sendGreetingsNewMember(chatId, user);
                }

            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            sendSimpleText(chatErrorId, e.toString());
        }
    }

    boolean isWrongAddress(Long chatId, String addressOrPublicKey) {
        Fun.Tuple2<Account, String> result = Account.tryMakeAccount(addressOrPublicKey);
        if (result.a == null) {
            sendSimpleText(chatId, addressOrPublicKey + ": " + result.b);
            return true;
        }
        // getPublicKeyByAddress
        if (result.a instanceof PublicKeyAccount)
            return false;

        if (cnt.getPublicKeyByAddress(result.a.getAddress()) == null) {
            sendSimpleText(chatId, addressOrPublicKey + " - публичный ключ данного счета неизвестен! Укажите вместо счета сам публичный ключ.");
            return true;
        }
        return false;
    }

    protected JSONArray getReceivers(JSONObject chatSettings) {
        if (!chatSettings.containsKey("receivers")) {
            chatSettings.put("receivers", new JSONArray());
        }
        return (JSONArray) chatSettings.get("receivers");
    }

    private void setReceivers(JSONObject chatSettings, JSONArray list) {
        chatSettings.put("receivers", list);
    }

    private void viewReceivers(Long chatId) {
        String info = "Список получателей";
        JSONArray list = getReceivers(getChatSettings(chatId));
        if (list.isEmpty()) {
            info += " - *пуст!*\nНикто не сможет расшифровать сохраняемые сообщения кроме самого бота.\nДля того чтобы не потерять доступ к сохраненным сообщениям в зашифрованном виде, укажите список счетов, которые будут получать их и смогут расшифровать при необходимости.";
        } else {
            info += ":\n";
            for (Object receiver : list) {
                info += "- " + scanUrlMarkdown(receiver.toString(), "address", receiver) + "\n";
            }
            info += "\n _Данные счета смогут расшифровать скрытые сообщения, которые высланы на них_";
        }

        info += "\n\nИспользуйте команду *для* с действием *=*, *+* или *-* и списком публичных ключей или счетов через пробел. Например:\n\n`для + СЧСЕТ1 СЧЕТ2`";
        sendMarkdown(chatId, info);

    }

    protected boolean editReceivers(String[] command, JSONObject settings, Long chatId, Chat chat, User user) {
        if (command.length == 1) {
            viewReceivers(chatId);
            return true;
        }
        // @blockchain_storage_bot receiver + 7QKXrHLRM1gd7bFjD2K5RYjYJfCPqJwoHf
        // @blockchain_storage_bot to = 7QKXrHLRM1gd7bFjD2K5RYjYJfCPqJwoHf
        // задает SEED для чата
        JSONArray list;
        if (command[1].equals("+")) {
            // Добавить счета к списку получателей
            list = getReceivers(settings);
            for (int i = 2; i < command.length; i++) {
                if (isWrongAddress(chatId, command[i])) {
                    return true;
                }
                if (list.contains(command[i]))
                    continue;
                list.add(command[i]);
            }

        } else if (command[1].equals("-")) {
            // Удалить счета к списку получателей
            list = getReceivers(settings);
            for (int i = 2; i < command.length; i++) {
                list.remove(command[i]);
            }

        } else if (command[1].equals("=")) {
            // Задать новый список счетов получателей
            list = new JSONArray();
            for (int i = 2; i < command.length; i++) {
                if (isWrongAddress(chat.id(), command[i])) {
                    return true;
                }
                list.add(command[i]);
            }
            setReceivers(settings, list);
        }

        saveSettings();

        viewReceivers(chat.id());

        return true;

    }

    protected List<Object[]> getSettingsCommandsList() {
        List<Object[]> out = new ArrayList<>();
        out.add(new Object[]{new String[]{"account", "счёт", "счет"}, "Работа со счетами летописца"});
        out.add(new Object[]{new String[]{"for", "receivers", "recipients", "получатели", "для"}, "Список получателей сообщений в блокчейн, которые так же смогут расшифровать сообщение если оно зашифровано (закрытое)"});
        out.add(new Object[]{new String[]{"bot", "бот"}, "Команды для самого бота. Например команда `бот счёт`"});
        out.add(new Object[]{new String[]{"chain", "цепь"}, "Общая информация о блокчейне: имя, подпись начального Блока, скорость блоков и т.д."});
        return out;
    }

    abstract protected List<Object[]> getJobCommandsList();

    private void answerChainInfo(Long chatId, String lang) {
        StringBuilder mess = new StringBuilder();
        mess.append("Свойства блокчейн сети").append("Имя: ").append(cnt.getApplicationName(true))
                .append("\nДата исходного блока: `").append(new Timestamp(cnt.blockChain.getGenesisBlock().getTimestamp()))
                .append("\nПодпись исходного блока: `").append(Base58.encode(cnt.blockChain.getGenesisBlock().getSignature()))
                .append("`\nСкорость сборки блоков (сек): *").append(BlockChain.GENERATING_MIN_BLOCK_TIME_MS(NTP.getTime()) / 1000)
                .append("*\nПросмотр цепочки данных: ").append(scanUrlMarkdown("сканер блокчейн", "blocks", null));

        sendMarkdown(chatId, mess.toString());

    }

    protected boolean botAnswerPublic(String receivedMessage, Chat chat, User user) {
        String lang = user.languageCode();
        String[] command = receivedMessage.toLowerCase().split(" ");
        switch (command[0]) {
            case "help":
            case "помоги":
            case "помощь":
                sendHelpMessage(chat.id(), lang);
                return true;
            case "account":
            case "счёт":
            case "счет":
                sendChatAccountMessage(chat, lang);
                return true;
            case "for":
            case "receivers":
            case "recipients":
            case "получатели":
            case "для":
                viewReceivers(chat.id());
                return true;
            case "bot":
            case "бот":
                return botAnswerSelf(command, chat.id(), user.languageCode());
            case "chain":
            case "цепь":
                answerChainInfo(chat.id(), user.languageCode());
                return true;
        }

        return false;

    }

    protected boolean botAnswerAdmin(String[] command, Chat chat, User user) {
        switch (command[0].toLowerCase()) {
            case "account":
            case "счёт":
            case "счет":
                // @blockchain_storage_bot account new
                // задает SEED для чата
                if (command[1].equals("new") || command[1].equals("new")) {
                    //makeAddress()
                    sendSimpleText(chat.id(), "coming soon...");

                    return true;
                }
                sendChatAccountMessage(chat, user.languageCode());
                return true;
            case "for":
            case "receivers":
            case "recipients":
            case "получатели":
            case "для":
                return editReceivers(command, getChatSettings(chat.id()), chat.id(), chat, user);
            case "bot":
            case "бот":
                return botAnswerSelf(command, chat.id(), user.languageCode());
            case "chain":
            case "цепь":
                answerChainInfo(chat.id(), user.languageCode());
                return true;
        }
        return false;
    }

    protected boolean botAnswerPrivate(String[] command, long chatId, Chat chatPrivate, User user) {
        switch (command[0].toLowerCase()) {
            case "help":
            case "помоги":
            case "помощь":
                sendHelpPrivateMessage(chatId, user, user.languageCode());
                return true;
            case "account":
            case "счёт":
            case "счет":
                sendUserAccountMessage(chatId, user);
                return true;
            case "for":
            case "receivers":
            case "recipients":
            case "получатели":
            case "для":
                return editReceivers(command, getChatSettings(chatId), chatId, chatPrivate, user);
            case "bot":
            case "бот":
                return botAnswerSelf(command, chatId, user.languageCode());
            case "chain":
            case "цепь":
                answerChainInfo(chatPrivate.id(), user.languageCode());
                return true;
        }
        return false;
    }

    protected boolean botAnswerSelf(String[] command, long chatId, String lang) {
        if (command.length == 1) {
            sendBotAccountMessage(chatId, lang);
            return true;
        }

        switch (command[1].toLowerCase()) {
            case "account":
            case "счёт":
            case "счет":
                sendBotAccountMessage(chatId, lang);
                return true;
            case "chain":
            case "цепь":
                answerChainInfo(chatId, lang);
                return true;
        }
        return false;
    }

    protected BaseResponse sendSimpleText(long chatId, String textToSend) {
        SendMessage message = new SendMessage(chatId, textToSend);
        return sendMessage(message);
    }

    protected BaseResponse sendMarkdown(long chatId, String textToSend) {
        SendMessage message = new SendMessage(chatId, textToSend);
        message.parseMode(ParseMode.Markdown);

        return sendMessage(message);

    }

    protected BaseResponse sendHtml(long chatId, String textToSend) {
        SendMessage message = new SendMessage(chatId, textToSend);
        message.parseMode(ParseMode.HTML);

        return sendMessage(message);

    }

    protected boolean LOG_ON = true;

    protected void sendMessage(Object chatId, Message message) {
        SendMessage sendMessage = new SendMessage(chatId, message.text());
        if (LOG_ON)
            log.warn(message.text());

        if (message.entities() != null) {
            sendMessage.entities(message.entities());
            if (LOG_ON)
                log.warn(GSON.toJson(message.entities()).toString());
        }
        if (message.linkPreviewOptions() != null) {
            sendMessage.linkPreviewOptions(message.linkPreviewOptions());
            if (LOG_ON)
                log.warn(GSON.toJson(message.linkPreviewOptions()).toString());
        }

        sendMessage(sendMessage);
    }

    protected BaseResponse sendMessage(SendMessage sendMessage) {
        try {
            BaseResponse sendResponse = bot.execute(sendMessage);
            if (sendResponse.isOk())
                return sendResponse;
            log.warn("Reply sent: " + GSON.toJson(sendResponse));
            return sendResponse;
        } catch (Exception e) {
            log.error("Send ERROR: " + e.getMessage());
            return null;
        }
    }

    // TODO sendChatAction - https://core.telegram.org/bots/api#sendchataction
    protected void sendTxResult(Long replyChatId, Integer replyMessageId, Transaction transaction, int validate, String lang) {

        SendMessage sendMessage;
        if (validate == VALIDATE_OK) {
            // Используем разметку Markdown
            sendMessage = new SendMessage(replyChatId, "Запущено сохранение в " + scanUrlMarkdown("блокчейн", "tx", Base58.encode(transaction.getSignature())) + "...");

        } else {
            JSONObject out = new JSONObject();
            transaction.updateMapByError2(out, validate, lang);
            String text = "```java\n" + out.toJSONString() + "```";
            sendMessage = new SendMessage(replyChatId, text);
        }

        sendMessage.parseMode(ParseMode.Markdown);

        if (replyMessageId != null) {
            // если чаты ответа и сообщения которое мы сохраняем совпадают,
            // то сделаем его как ответ на сохраняемое сообщение
            sendMessage.replyToMessageId(replyMessageId);
        }

        try {
            SendResponse response = bot.execute(sendMessage);
            if (response.isOk() && validate == VALIDATE_OK) {
                // запомним ее как неподтвержденную и непросчитанную - иначе height по другому блоку считает
                pendingTxs.put(Base58.encode(transaction.getSignature()), new Pair<>(response.message(), transaction.copy()));
                log.info("Reply sent");
            } else {
                log.error("Send ERROR: " + GSON.toJson(response).toString());
            }
        } catch (Exception e) {
            log.error("Send ERROR: " + e.getMessage());
        }
    }

    protected String getGreetingsHelp(boolean forChat, String lang) {
        StringBuilder mess = new StringBuilder();
        mess.append("Для полной помощи используйте команды `");
        if (forChat)
            mess.append(botUserNameF).append(" ");
        mess.append("help`, `");
        if (forChat)
            mess.append(botUserNameF).append(" ");
        mess.append("помоги` или `");
        if (forChat)
            mess.append(botUserNameF).append(" ");
        mess.append("помощь`\n");

        return mess.toString();
    }

    abstract protected String getHeadHelpMessage(String lang);

    abstract protected String helpLinksMessage(String lang);

    abstract protected BaseResponse sendGreetingsMessage(Long chatId, String lang);

    abstract protected BaseResponse sendHelpMessage(Long chatId, String lang);

    abstract protected BaseResponse sendGreetingsPrivateMessage(Long chatId, User user, String lang);

    abstract protected BaseResponse sendHelpPrivateMessage(Long chatId, User user, String lang);

    protected BaseResponse sendGreetingsNewMember(Long chatId, User user) {
        //sendUserAccountMessage(chatId, user);
        return sendSimpleText(chatId, user.username() + ", приветствую тебя...");
    }

    private String accountFeeBalance(Account pubKey, String lang) {
        BigDecimal balance = pubKey.getBalanceUSE(FEE_KEY);
        return String.format("Баланс на счёте: *%s* (хватит на сохранение примерно %d кБ)", balance.toPlainString(), Account.bytesPerBalance(balance) / 2000);
    }

    private String accountPerson(PublicKeyAccount pubKey, String lang) {
        Fun.Tuple2<Integer, PersonCls> result = pubKey.getPerson();
        return "`" + pubKey.getAddress() + "` (" + scanUrlMarkdown("ссылка", "address", pubKey.getAddress()) + ")"
                + "\n\n" + accountFeeBalance(pubKey, lang) + "\n----\nДля оплаты комиссий блокчейн пополняйте этот счет своевременно, иначе сохранение в блокчейн может не сработать.\n"
                + (result == null ? "\nЭто *неудостоверенный счёт*. С него можно отправлять *Открытые* сообщения только очень короткие."
                + " Для снятия этого ограничения, удостоверьте этот открытый ключ: `" + pubKey.getBase58()
                + "`.\nКак удостоверить счёт и свою [персону в блокчейн](https://wiki.erachain.org/ru/Persons)"
                : "Это удостоверенный счёт на " + scanUrlMarkdown(result.b.getName(), "person", result.b.getKey()));
    }

    protected BaseResponse sendBotAccountMessage(Long chatId, String lang) {
        PublicKeyAccount pubKey = getBotAddress(lang);
        return sendMarkdown(chatId, "Собственный счет бота, который используется для поощрений других пользователей - "
                + accountPerson(pubKey, lang));
    }

    protected BaseResponse sendChatAccountMessage(Chat chat, String lang) {
        PublicKeyAccount pubKey = makeAddress(chat.id(), lang);
        return sendMarkdown(chat.id(), "Счет для работы летописца чата *" + chat.title() + "* - "
                + accountPerson(pubKey, lang));
    }

    protected BaseResponse sendUserAccountMessage(Long chatId, User user) {
        String lang = user.languageCode();
        PublicKeyAccount pubKey = makeAddress(user.id(), lang);
        return sendMarkdown(chatId, "Счет для работы вашего, *" + user.firstName() + "*, личного летописца - "
                + accountPerson(pubKey, lang));
    }

    protected BaseResponse sendToAdminMessage(String sendMessage) {
        if (adminChatId == null)
            return null;
        return sendMarkdown(adminChatId, sendMessage);
    }

    /**
     * Это счет с которого бот сможет начислять награды на другие счет - для это не забывайте пополнять его
     *
     * @param lang
     * @return
     */
    protected PrivateKeyAccount getBotPrivateKey(String lang) {
        if (myPrivacyKey == null)
            myPrivacyKey = getPrivKey(0L, lang);
        return myPrivacyKey;
    }

    private PublicKeyAccount getBotAddress(String lang) {
        return getBotPrivateKey(lang);
    }

    private PublicKeyAccount makeAddress(Long chatId, String lang) {
        return getPrivKey(chatId, lang);
    }

    protected void saveSettings() {
        try {
            SaveStrToFile.saveJsonFine(settingsPath, settingsJSON);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    protected JSONObject getChatSettings(Long chatId) {
        String key = "" + chatId;
        if (!settingsChats.containsKey(key)) {
            settingsChats.put(key, new JSONObject());
            saveSettings();
        }
        return (JSONObject) settingsChats.get(key);

    }

    //
    private void sendOnSetMemberStatus(Long chatId, ChatMember newChatMember) {
        Boolean canEditMessages = newChatMember.canEditMessages();

        sendSimpleText(chatId, "!!!!\nВНИМАНИЕ! Без прав администратора я не смогу читать сообщения и делать свою работу... Пожалуйста наделите меня правами администратора в этом чате");

    }

    protected void giftOnMeet(JSONObject settings, Long chatId, PublicKeyAccount account, String lang) {
        if (!BlockChain.TEST_MODE && account.getBalanceUSE(FEE_KEY).compareTo(BigDecimal.ZERO) > 0)
            return;
        if (chatId.equals(adminChatId))
            return;
    }

    /**
     * Либо генерирует свое либо берет уже то что задано - можно положить свой туда
     *
     * @param chatId
     * @param lang
     * @return
     */
    protected PrivateKeyAccount getPrivKey(Long chatId, String lang) {

        JSONObject settings = getChatSettings(chatId);
        if (settings.containsKey(PRIVATE_KEY_SEED)) {
            return new PrivateKeyAccount(Base58.decode((String) settings.get(PRIVATE_KEY_SEED)));
        } else {
            // неизменное Имя канала
            PrivateKeyAccount privKey = new PrivateKeyAccount(Wallet.generateAccountSeed(baseSeed, chatId));
            settings.put(PRIVATE_KEY_SEED, Base58.encode(privKey.getSeed()));
            settings.put("pubKey", privKey.getBase58());
            settings.put("address", privKey.getAddress());
            settings.put("lang", lang);
            knownPubKey.put(privKey.getBase58(), chatId.toString());
            knownAddress.put(privKey.getAddress(), chatId.toString());

            if (chatId != 0) {
                // Это новый пользователь - надо ему выслать награду
                giftOnMeet(settings, chatId, privKey, lang);
            }

            saveSettings();
            return privKey;
        }


    }

    static public class Buttons {
        private static final InlineKeyboardButton START_BUTTON = new InlineKeyboardButton("Start");
        private static final InlineKeyboardButton HELP_BUTTON = new InlineKeyboardButton("Help");

        public static InlineKeyboardMarkup inlineMarkup() {
            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup(
                    new InlineKeyboardButton[]{
                            //new InlineKeyboardButton("url").url("www.google.com"),
                            START_BUTTON.callbackData("/start"),
                            //new InlineKeyboardButton("Switch!").switchInlineQuery("switch_inline_query")
                            HELP_BUTTON.callbackData("/help")
                    });

            return inlineKeyboard;
        }
    }

    protected ConcurrentHashMap<String, Pair<Message, Transaction>> pendingTxs = new ConcurrentHashMap<>();

    /**
     * Опрос блокчейн для проверки подтверждения транзакций
     */
    protected synchronized void updateTransactions() {

        DCSet dcSet = cnt.getDCSet();
        Iterator<Map.Entry<String, Pair<Message, Transaction>>> iterator = pendingTxs.entrySet().iterator();
        Map.Entry<String, Pair<Message, Transaction>> entry;
        while (iterator.hasNext()) {
            entry = iterator.next();

            Pair<Message, Transaction> pair = entry.getValue();
            Message messge = pair.getA();
            if (messge == null) {
                iterator.remove();
                continue;
            }

            if (pair.getB().getBlockHeight() <= 0) {
                // если транзакция еще не подтверждена
                // проверим ее саму
                Long seq = cnt.getTransactionSeqBySign(pair.getB().getSignature(), dcSet);
                if (seq == null)
                    continue;

                Transaction tx = cnt.getTransaction(seq, dcSet);
                if (tx == null) {
                    // ОШИБКА - она исчезла...
                    iterator.remove();
                    continue;
                }

                // считаем это достаточным числом подтверждений
                // принимаем как внесенную в блокчейн "навечно"
                log.info(botTitle + " done: " + tx.viewHeightSeq());
                entry.setValue(new Pair<>(pair.getA(), tx));
            }

            if (pair.getB().getConfirmations(dcSet) < PENDING_CONFIRMS)
                continue;

            // сюда приходит если транзакция подтверждена, но в чате сообщение не обновилось что подтверждено
            EditMessageText editMessage = new EditMessageText(messge.chat().id(), messge.messageId(),
                    "Сохранено в " + scanUrlMarkdown("блокчейн", "tx", pair.getB().viewHeightSeq()) + " !!!");
            editMessage.parseMode(ParseMode.Markdown);
            BaseResponse response = bot.execute(editMessage);
            if (response.isOk()) {
                iterator.remove();
            }

            // что-то пошло не так - оставим на будущее для ответа еще раз

        }

        long current = NTP.getTime();
        if (pendingTxs.size() > (MAX_PENDING_COUNT >> 3)) {
            // если очередь распухла, чистим все старые - чиста очень старых
            iterator = pendingTxs.entrySet().iterator();
            while (iterator.hasNext()) {
                Transaction tx = iterator.next().getValue().getB();
                if (tx.getConfirmations(dcSet) > 60
                        || current - tx.getTimestamp() > 6000000) {
                    iterator.remove();
                }
            }
        }

        // повторная чистка более жесткая всех кто старше
        int diff = pendingTxs.size() - MAX_PENDING_COUNT;
        if (diff > 0) {
            // если очередь распухла, чистим все старые
            iterator = pendingTxs.entrySet().iterator();
            while (iterator.hasNext() && diff-- > 0) {
                iterator.next();
                iterator.remove();
            }
        }

    }

    protected void processIncomeTx(String chatId, Transaction tx, JSONObject settings, String lang) {
        StringBuilder mess = new StringBuilder();
        mess.append("Поступление:\n\n");

        if (tx.getTitle() != null)
            mess.append(tx.getTitle()).append("\n");

        if (tx instanceof RSend) {
            RSend rSend = (RSend) tx;

            if (rSend.hasAmount()) {
                mess.append(Lang.T(rSend.viewActionType(), langList.getLangJson(lang))).append(": ").append(rSend.getAmount().toPlainString());
                if (rSend.getAssetKey() != FEE_KEY) {
                    mess.append("[").append(rSend.getAsset().getName()).append("]");
                }
            }

            if (rSend.isText()) {
                mess.append("\n------\n");
                if (rSend.isEncrypted()) {
                    mess.append(rSend.viewData());
                } else {
                    mess.append(rSend.viewData());
                    mess.append("\n");
                }
            }

            if (true)
                sendSimpleText(Long.parseLong(chatId), mess.toString());
            else
                sendMarkdown(Long.parseLong(chatId), "```Поступление:\n\n" + mess + "```");
        }

        if (BlockChain.TEST_MODE)
            sendMarkdown(Long.parseLong(chatId), "```Transaction:\n\n" + tx.toJson() + "```");

    }

    /**
     * Проверим все транзакции в блоке - если они пришли к нашим известным, то вызов
     *
     * @param txs
     */
    public void processBlock(List<Transaction> txs) {
        if (bot == null || settingsChats == null)
            return;

        HashSet<Account> listTo;
        String chatId;
        JSONObject chatSettings;
        for (Transaction tx : txs) {
            listTo = tx.getRecipientAccounts();
            if (listTo == null || listTo.isEmpty())
                continue;

            for (Account to : listTo) {
                chatId = knownAddress.get(to.getAddress());
                if (chatId == null)
                    return;

                chatSettings = (JSONObject) settingsChats.get(chatId);
                if (chatSettings == null)
                    return;

                try {
                    processIncomeTx(chatId, tx, settingsChats, (String) chatSettings.get("lang"));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

            }
        }

    }

    public void stop() {
        if (bot == null)
            return;

        sendToAdminMessage("Stopped...");

        settingsChats.keySet().forEach(chatId -> {
            if (((JSONObject) settingsChats.get(chatId)).get("userName") != null)
                // пользователям не пишем
                return;
            sendSimpleText(Long.parseLong((String) chatId), "Моя работа временно прекращена, надеюсь не на долго...");
        });

        bot.removeGetUpdatesListener();
        bot.shutdown();
    }
}
