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
import lombok.Getter;
import org.erachain.bot.Rechargeable;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.wallet.Wallet;
import org.erachain.datachain.DCSet;
import org.erachain.lang.Lang;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.FileUtils;
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
abstract public class ErachainBotCls implements Rechargeable {

    private static final Logger log = LoggerFactory.getLogger(ErachainBotCls.class.getSimpleName());
    Controller cnt;
    Settings settingsInstance;
    Lang langList;
    TelegramBot bot;
    static Gson GSON = new Gson();

    private JSONObject settingsJSON;
    private JSONObject settingsAllChats;
    private byte[] baseSeed;

    private GetMeResponse meInfo;
    protected String settingsName;
    protected String settingsPath;
    protected String botTitle;
    protected String botUserName;
    @Getter
    protected String botUserNameF;
    protected String botUserNameFAndCommand;
    @Getter
    protected String botUserNameMD;
    protected String botUserNameMD1;

    private long SEE_REPIOD;
    // максимальная длинна очереди транзакций для слежения на получение подтверждения
    private final int MAX_PENDING_COUNT = 1000;
    private final int PENDING_CONFIRMS = 3;

    private final static String PRIVATE_KEY_SEED = "privSeed";

    String commandsHelp;

    private PrivateKeyAccount myPrivacyKey;

    @Getter
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
                settingsAllChats = new JSONObject();
                settingsJSON.put("chats", settingsAllChats);
                saveSettings = true;
            }

            String parsTokenName = settingsName + "_token";
            botToken = (String) settingsJSON.get(parsTokenName);
            botToken = botToken == null ? System.getProperty(parsTokenName, System.getenv(parsTokenName)) : botToken;
            if (botToken == null) {
                log.warn("Token for bot [{}] not found in parameters, skip start... Use -D{}=...", parsTokenName, parsTokenName);
                return;
            }

            log.info("Try start [{}], used {}", settingsName, settingsPath);

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
                settingsAllChats = new JSONObject();
                settingsJSON.put("chats", settingsAllChats);
                saveSettings = true;
            } else {
                settingsAllChats = (JSONObject) settingsJSON.get("chats");
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
            log.warn("GetMy: {} - canceled", GSON.toJson(meInfo));
            stop();
            return;
        }

        // Подписка на обновления
        bot.setUpdatesListener(updates -> {
            // Обработка обновлений
            updates.forEach(update -> onUpdateReceived(update));
            // return id of last processed update or confirm them all
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        }, e -> {
            if (e.response() != null) {
                // Ошибка из Телеграма
                log.warn("TELEGRAM ERROR: {} - {}", e.response().errorCode(), e.response().description());
            } else {
                // Как видно проблема сети
                e.printStackTrace();
            }
        });

        botUserName = meInfo.user().username();
        botTitle = meInfo.user().firstName();
        botUserNameF = "@" + botUserName;
        botUserNameFAndCommand = botUserNameF + " /";
        botUserNameMD1 = "`" + botUserNameF + "`";
        botUserNameMD = "@" + botUserName.replace("_", "\\_");
        log.warn(botUserName + " started!");
        log.warn("GetMy: {}", GSON.toJson(meInfo));

        // Для посылки сообщений админу - нужно задать adminChatId в настройках!
        adminChatId = (Long) settingsJSON.get("adminChatId");
        sendToAdminMessage("Started...");
        settingsAllChats.keySet().forEach(chatId -> {
            try {
                if (((JSONObject) settingsAllChats.get(chatId)).get("user") != null)
                    // пользователям не пишем
                    return;
                sendMarkdown(Long.parseLong((String) chatId), "Я запустился... " + botUserNameMD1);
            } catch (Exception e) {
                log.warn("Init chat {} - error {} ", chatId, e.toString());
            }
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
                } catch (Throwable e) {
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

    abstract protected String[] retrieveChatReplyCommand(String text);

    abstract protected String[] retrieveChatMessageCommand(String text);

    abstract protected boolean privateForwardReplyHasCommand(String text);

    abstract protected boolean processAutomaticForward(Long makerTxId, Chat replyChat, Chat chatMain, Message message, String lang);

    abstract protected boolean processMessage(Long makerTxId, Chat replyChat, Chat chatMain, Message message, String lang);

    abstract protected void makeEntitiesCommands(String text, List<MessageEntity> commands, MessageEntity entity);

    abstract protected boolean processForwardOrigin(Long makerTxId, Chat replyChat, Integer replyMessageId, Chat origChat, Integer origMessageId, Integer origMessageDate, Message message, String[] commands, String lang);

    abstract protected boolean processLinkPreviewOptions(Long makerTxId, Chat replyChat, Integer replyMessageId, Message message, String[] commands, String lang);

    abstract protected boolean processChatReplyCommand(Long makerTxId, Chat replyChat, Integer replyMessageId, Chat origChat, Integer origMessageId, Integer origMessageDate, Message message, String[] commands, String lang);

    abstract protected boolean processCommand(Long makerTxId, Chat replyChat, Integer replyMessageId, Chat origChat, Integer origMessageId, Integer origMessageDate, Message message, String[] commands, String lang);

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
            String lang = "ru";

            ////////// MESSAGE
            ////////////////////
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
                    this.processAutomaticForward(chatId, chat, origMessageChat, message, lang);
                    //if (origMessageChat.type().equals(Chat.Type.channel)) {
                    // при этом message.from() - будет id=777000 firstName= "Telegram"
                    // поэтому берем Имя основного чата - и счет из него будем делать
                    // userName = origMessageChat.username();
                    //   if (this.processMessage(chatId, chatId, origMessageChat, message, lang))
                    //       return;
                    //   return;
                    //}

                } else if (chat.type().equals(Chat.Type.Private)) {
                    // в приватном чате не нужно писать Имя бота в сообщениях - поэтому сразу команды обрабатываем
                    user = message.from();
                    // В комментарии к пересланному сообщению мы ожидаем команду и параметры
                    text = message.text();

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
                            log.warn("skip forwardOrigin: {}", forwardOrigin.getClass().getSimpleName());
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
                            JSONObject settings = getChatSettings(chatId, user);
                            String lastCommand = (String) settings.remove("lastCommand");
                            this.processForwardOrigin(chatId, chat, message.messageId(), origChat, origMessageId, origMessageDate, message, new String[]{lastCommand, text}, lang);
                        }
                    } else if (message.linkPreviewOptions() != null) {
                        // https://t.me/Onishchenko001/61166?comment=1621294
                        //LinkPreviewOptions link = message.linkPreviewOptions();
                        this.processLinkPreviewOptions(chatId, chat, message.messageId(), message, new String[]{text, null}, lang);
                    } else if (botAnswerPrivate(text.split(" "), chatId, chat, message.messageId(), user)) {
                    } else {
                        // Ответ на пересланное сообщение - прилетает в приватный чат первым как обычное сообщение
                        // следом прилетит пересланное сообщение - для него сохраним последнюю команду
                        JSONObject settings = getChatSettings(chatId, user);
                        if (privateForwardReplyHasCommand(message.text())) {
                            settings.put("lastCommand", message.text());

                        } else {
                            // это обычное сообщение в приват - может быть наказом или приказом
                            BaseResponse response = sendGreetingsPrivateMessage(chatId, user, lang);
                            // Проверим сразу счет - если что вышлем подарок
                            getPrivKey(chatId, user);
                        }
                    }


                } else //if (chat.type().equals(Chat.Type.supergroup) // публичная группа
                // || chat.type().equals(Chat.Type.group))
                { // не публичная - только по приглашению
                    // Это группа и сообщение не переданное из канала - значит нужно найти в нем саму команду
                    // - всё не кидать в блокчейн

                    // это сообщение пользователя или другого бота - но не из головного канала
                    text = message.caption();
                    if (text == null)
                        text = message.text();

                    chatErrorId = chatId;
                    if (// даже Яндекс ничего сними сделать не может - это чисто встроенная фишка
                            message.forwardOrigin() == null &&
                                    (text == null || text.isEmpty())) {
                        // сюда приходит если было например изменение названия группы (супергруппы)
                        // или кого-то забанили
                        return;
                    }

                    if (text == null) {
                        String warn = "Unknown action: " + GSON.toJson(message);
                        sendToAdminMessage(warn);
                        log.warn(warn);
                        return;
                    }

                    if (text.startsWith("/") || text.startsWith(botUserNameFAndCommand)) {
                        // это скорее всего команда боту быстрая
                        // если это ответ на другое - или есть общее обсуждение
                        if (message.replyToMessage() != null) {
                            // проверим - а есть ли в сообщении вообще команда или это обычное сообщение
                            String[] commands = retrieveChatReplyCommand(text);
                            if (commands == null)
                                // Это сообщение без команды - выход
                                return;

                            Message reply = message.replyToMessage();
                            //from = reply.from();
                            from = message.from();
                            lang = from.languageCode();
                            if (from.isBot()) {
                                if ("GroupAnonymousBot".equals(from.username()) || "Channel_Bot".equals(from.username())) {
                                    // это админ канала - chatId не меняем
                                    if (this.processChatReplyCommand(
                                            // Ответ о записи в блокчейн в том же чате на нашу команду будет:
                                            chatId, chat, message.messageId(),
                                            reply.chat(), reply.messageId(), reply.date(), reply, commands, lang))
                                        return;
                                }
                            } else {
                                // чат для общения приватный и счет под него генерим
                                //chatId = from.id(); - если сообщение катать в приватный чат пользователя
                                if (this.processChatReplyCommand(
                                        // Ответ о записи в блокчейн в том же чате на нашу команду будет:
                                        chatId, chat, message.messageId(),
                                        reply.chat(), reply.messageId(), reply.date(), reply, commands, lang))
                                    return;
                            }
                        }

                        return;

                    } else if (text.startsWith(botUserNameF)) {
                        // это сообщение боту из общего чата - видное всем
                        toBotCommand(chatId, message, chat, text);
                        return;

                    } else {

                        if (false) {
                            // Если использовать их внутренний механизм выявления Команд по message.entities() - то русские команды туда не попадают ((
                            MessageEntity[] entities = message.entities();
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
                            // ...
                        }

                        // проверим - а есть ли в сообщении вообще команда или это обычное сообщение
                        String[] commands = retrieveChatMessageCommand(text);
                        if (commands == null || commands[0] == null)
                            // Это сообщение без команды - выход
                            return;

                        // проверку админства - по getChatAdministrators и getChatMember
                        // если это админы то от имени чата сохраняем - иначе оот имени пользователя
                        // ChatMemberOwner и ChatMemberAdministrator + can_manage_chat
                        from = message.from();
                        if (from.isBot()) {
                            if ("GroupAnonymousBot".equals(from.username()) || "Channel_Bot".equals(from.username())) {
                                // это автобот чата, который присоединен к чаты от канала
                                // тут не меняем ничего
                                ;
                            } else {
                                // иной какой-то бот - игнорируем
                                return;
                            }
                        } else {

                            // TODO после определения команды - надо ловить статус члена
                            GetChatMember request = new GetChatMember(chatId, from.id());
                            GetChatMemberResponse response = bot.execute(request);
                            if (!response.isOk()) {
                                // что пошло не так
                                sendMarkdown(chatId, "```java Response\n" + response + "```");
                                return;
                            }

                            ChatMember chatMember = response.chatMember();
                            if (chatMember.status().equals(ChatMember.Status.creator)
                                    || chatMember.status().equals(ChatMember.Status.administrator) && chatMember.canManageChat()) {
                                // chatId = message.chat().id();
                                ;
                            } else {
                                chatId = from.id();
                            }
                        }

                        processCommand(chatId, chat, message.messageId(), chat, message.messageId(), message.date(), message, commands, lang);
                    }
                }
                return;
            }

            MessageReactionUpdated reactions = update.messageReaction();

            ////////// CHANNEL POST
            ////////////////////
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
                if (message.caption() != null) {
                    text = message.caption();
                } else {
                    text = message.text();
                }
                if (text == null)
                    return;

                if (text.startsWith(botUserNameF)) {
                    // это сообщение боту из общего чата - видное всем
                    toBotCommand(chatId, message, chat, text);
                    return;
                }

                if (this.processMessage(chatId, chat, chat, message, lang))
                    return;

                return;
            }

            ////////// CALL BACK
            ////////////////////
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

            ////////// MY CHAT MEMBER
            ////////////////////
            // приглашения и удаления из групп
            ChatMemberUpdated myChatMember = update.myChatMember();
            if (myChatMember != null) {
                chat = myChatMember.chat(); // в каком чате действие произошло
                chatId = chat.id();
                chatErrorId = chat.id();
                from = myChatMember.from(); // кто это сделал

                log.warn("User from: {}", GSON.toJson(from));

                ChatMember newChatMember = myChatMember.newChatMember();
                user = newChatMember.user();
                ChatMember.Status oldStatus = myChatMember.oldChatMember().status();
                ChatMember.Status newStatus = newChatMember.status();
                if (oldStatus.equals(newStatus))
                    return;

                if (user.username().equals(botUserName)) {

                    if (newStatus.equals(ChatMember.Status.kicked) || newStatus.equals(ChatMember.Status.left)) {
                        // нас удалили из группы
                        Integer untilDate = newChatMember.untilDate(); // 0 - просто удалили
                        log.warn("kicked from {} by User {}", chat.title(), from.username());

                        // Конкретный пользователь известен только когда удаляют администратора! Иначе бот кикает и не понятно кто тебя кикнул
                        if (!from.isBot())
                            sendSimpleText(from.id(), from.username() + ", спасибо за использование наших услуг в чате \"" + chat.title() + "\". Надеемся на продолжение сотрудничества в будущем.");

                    } else if (newStatus.equals(ChatMember.Status.administrator)) {
                        // нас сделали администратором

                        // Тут известен конкретный пользователь кто пригласил
                        if (!from.isBot())
                            sendSimpleText(from.id(), from.username() + ", спасибо за допуск меня к работе в чате \"" + chat.title() + "\". Надеюсь быть полезным...");

                        sendGreetingsMessage(chatId, lang);

                        // В приветный чат вышлем данные о счете по данному Имени чата
                        sendChatAccountMessage(chat, null, lang);
                        sendBotAccountMessage(chat.id(), null, lang);
                        //sendAccountChatMessage(from.id(), chat, lang);

                    } else if (newStatus.equals(ChatMember.Status.member)) {
                        // нас внесли в группу или понизили из админа до обычного пользователя

                        if (!oldStatus.equals(ChatMember.Status.administrator)) {
                            sendGreetingsMessage(chatId, lang);
                        }
                        // тут from.username() - это чат бот группы GroupAnonymousBot - бес толку ему что-то лично слать
                        sendOnSetMemberStatus(chatId, newChatMember);

                        if (!from.isBot())
                            sendSimpleText(from.id(), from.username() + ", моя работа в чате \"" + chat.title() + "\" прекращена. Жду назначения администраторам снова...");

                    }

                    if (log.isInfoEnabled())
                        log.info("status: {}", newStatus.name());

                    JSONObject chatSettings = getChatSettings(chatId, chat);
                    chatSettings.put("status", newStatus.name());
                    saveSettings();

                } else {
                    // Это не про меня - хотя ему можно сообщение написать
                    if (user.isBot())
                        return;

                    sendGreetingsNewMember(chatId, user);
                }

            }

        } catch (Throwable e) {
            String error = "TELEGRAM BOt Updates error - " + e;
            sendSimpleText(chatErrorId, error);
            error += "\n" + GSON.toJson(update);
            log.error(error);
            sendToAdminMessage(error);
        }
    }

    void toBotCommand(long chatId, Message message, Chat chat, String text) {
        // это сообщение боту из общего чата - видное всем
        // @blockchain_storage_bot mode 3
        String commandLine = text.length() == botUserNameF.length() ? "" : text.substring(botUserName.length() + 1).trim();
        String[] command = commandLine.split(" ");
        if (command.length == 1) {
            // команда без паарметров - просто ответить как правило выдать Инфо или данные текущих настроек
            if (botAnswerPublic(command[0], chat, message.from()))
                return;

        } else {
            // Команда с параметрами = для управления
            User from = message.from();
            if (from == null) {
                if (message.senderChat() != null && message.senderChat().id() == chatId) {
                    if (botAnswerAdmin(command, chat, from))
                        return;
                }
            } else if (from.isBot()) {
                if (from.username().equals("Channel_Bot")) {
                    // команды от админа канала
                    if (botAnswerAdmin(command, chat, from))
                        return;
                } else if (from.username().equals("GroupAnonymousBot")) {
                    // команды от админа чата
                    // TODO проверить разрешение на управление от админа канала - а если это просто группа ьез канала?
                    if (botAnswerAdmin(command, chat, from))
                        return;
                } else {
                    // иной какой-то бот
                }

            } else {
                // Пользователи - они тоже могут быть админами - см. ниже
                // TODO после определения команды - надо ловить статус члена
                GetChatMember request = new GetChatMember(chatId, from.id());
                GetChatMemberResponse response = bot.execute(request);
                if (!response.isOk()) {
                    // что пошло не так
                    sendMarkdown(chatId, "```java Response\n" + response + "```");
                    return;
                }

                ChatMember chatMember = response.chatMember();
                if (chatMember.status().equals(ChatMember.Status.creator)
                        || chatMember.status().equals(ChatMember.Status.administrator) && chatMember.canManageChat()) {
                    if (botAnswerAdmin(command, chat, from))
                        return;
                } else {
                    replySimpleText(chatId, "Для управления группой нужно быть администратором с правом управлять чатом", message.messageId());
                    return;
                }
            }
        }

        User user = message.from();
        String lang = user == null ? null : user.languageCode();
        BaseResponse response = sendGreetingsMessage(chatId, lang);
        // Проверим сразу счет - если что вышлем подарок
        getPrivKey(chatId, user);

        return;

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

    private void viewReceivers(Long chatId, Object object) {
        String info = "Список получателей";
        JSONArray list = getReceivers(getChatSettings(chatId, object));
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
            viewReceivers(chatId, chat);
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

        viewReceivers(chat.id(), chat);

        return true;

    }

    protected List<Object[]> getSettingsCommandsList() {
        List<Object[]> out = new ArrayList<>();
        out.add(new Object[]{new String[]{"account", "счёт", "счет"}, "Работа со счетами летописца"});
        out.add(new Object[]{new String[]{"for", "receivers", "recipients", "получатели", "для"}, "Список получателей сообщений в блокчейн, которые так же смогут расшифровать сообщение если оно зашифровано (закрытое)"});
        out.add(new Object[]{new String[]{"bot", "бот"}, "Команды для самого бота. Например команда `бот счёт`"});
        out.add(new Object[]{new String[]{"chain", "цепь", "цепочка"}, "Общая информация о блокчейне: имя, подпись начального Блока, скорость блоков и т.д."});
        return out;
    }

    abstract protected List<Object[]> getJobCommandsList();

    protected BaseResponse answerChainInfo(Long chatId, String lang) {
        StringBuilder mess = new StringBuilder();
        mess.append("*Свойства блокчейн сети*\n\n").append("Имя среды: *").append(cnt.getApplicationName(true))
                .append("*\nДата исходного блока: *").append(new Timestamp(cnt.blockChain.getGenesisBlock().getTimestamp()))
                .append("*\nПодпись исходного блока: `").append(Base58.encode(cnt.blockChain.getGenesisBlock().getSignature()))
                .append("`\nСкорость сборки блоков (сек): *").append(BlockChain.GENERATING_MIN_BLOCK_TIME_MS(NTP.getTime()) / 1000)
                .append("*\nПросмотр цепочки данных: ").append(scanUrlMarkdown("сканер блокчейн", "blocks", null));

        return sendMarkdown(chatId, mess.toString());
    }

    protected boolean botAnswerPublic(String receivedMessage, Chat chat, User user) {
        String lang = user == null ? null : user.languageCode();
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
                sendChatAccountMessage(chat, user, lang);
                return true;
            case "for":
            case "receivers":
            case "recipients":
            case "получатели":
            case "для":
                viewReceivers(chat.id(), chat);
                return true;
            case "bot":
            case "бот":
                return botAnswerSelf(command, chat.id(), user.languageCode());
            case "chain":
            case "цепь":
            case "цепочка":
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
                sendChatAccountMessage(chat, user, user.languageCode());
                return true;
            case "for":
            case "receivers":
            case "recipients":
            case "получатели":
            case "для":
                return editReceivers(command, getChatSettings(chat.id(), chat), chat.id(), chat, user);
            case "bot":
            case "бот":
                return botAnswerSelf(command, chat.id(), user.languageCode());
            case "chain":
            case "цепь":
            case "цепочка":
                answerChainInfo(chat.id(), user.languageCode());
                return true;
        }
        return false;
    }

    protected boolean botAnswerPrivate(String[] command, long chatId, Chat chatPrivate, Integer replyMessageId, User user) {
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
                return editReceivers(command, getChatSettings(chatId, chatId), chatId, chatPrivate, user);
            case "bot":
            case "бот":
                return botAnswerSelf(command, chatId, user.languageCode());
            case "chain":
            case "цепь":
            case "цепочка":
                answerChainInfo(chatPrivate.id(), user.languageCode());
                return true;
        }
        return false;
    }

    protected boolean botAnswerSelf(String[] command, long chatId, String lang) {
        if (command.length == 1) {
            sendBotAccountMessage(chatId, null, lang);
            return true;
        }

        switch (command[1].toLowerCase()) {
            case "account":
            case "счёт":
            case "счет":
                sendBotAccountMessage(chatId, null, lang);
                return true;
            case "chain":
            case "цепочка":
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

    protected BaseResponse replySimpleText(long chatId, String textToSend, int messageId) {
        SendMessage message = new SendMessage(chatId, textToSend);
        message.replyToMessageId(messageId);
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
            log.warn("Reply sent: {}", GSON.toJson(sendResponse));
            return sendResponse;
        } catch (Exception e) {
            log.error("Send ERROR: {}", e.toString());
            return null;
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

    protected BaseResponse sendBotAccountMessage(Long chatId, User user, String lang) {
        PublicKeyAccount pubKey = getBotAddress();
        return sendMarkdown(chatId, "Собственный счет бота, который используется для поощрений других пользователей - "
                + accountPerson(pubKey, lang));
    }

    protected BaseResponse sendChatAccountMessage(Chat chat, User user, String lang) {
        PublicKeyAccount pubKey = getAddress(chat.id(), user, lang);
        return sendMarkdown(chat.id(), "Счет для работы летописца чата *" + chat.title() + "* - "
                + accountPerson(pubKey, lang));
    }

    protected BaseResponse sendUserAccountMessage(Long chatId, User user) {
        String lang = user.languageCode();
        PublicKeyAccount pubKey = getAddress(user.id(), user, lang);
        return sendMarkdown(chatId, "Счет для работы вашего, *" + user.firstName() + "*, личного летописца - "
                + accountPerson(pubKey, lang));
    }

    public BaseResponse sendToAdminMessage(String sendMessage) {
        if (adminChatId == null)
            return null;
        return sendMarkdown(adminChatId, sendMessage);
    }

    /**
     * Это счет с которого бот сможет начислять награды на другие счет - для это не забывайте пополнять его
     *
     * @return
     */
    public PrivateKeyAccount getBotPrivateKey() {
        if (myPrivacyKey == null)
            myPrivacyKey = getPrivKey(0L, null);
        return myPrivacyKey;
    }

    private PublicKeyAccount getBotAddress() {
        return getBotPrivateKey();
    }

    private PublicKeyAccount getAddress(Long chatId, User user, String lang) {
        return getPrivKey(chatId, user);
    }

    protected void saveSettings() {
        try {
            SaveStrToFile.saveJsonFine(settingsPath, settingsJSON);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    protected JSONObject getChatSettings(Long chatId, Object object) {
        String key = "" + chatId;
        if (!settingsAllChats.containsKey(key)) {
            JSONObject settings = new JSONObject();
            settingsAllChats.put(key, settings);

            // неизменное Имя канала
            PrivateKeyAccount privKey = new PrivateKeyAccount(Wallet.generateAccountSeed(baseSeed, chatId));
            settings.put(PRIVATE_KEY_SEED, Base58.encode(privKey.getSeed()));
            settings.put("pubKey", privKey.getBase58());
            settings.put("address", privKey.getAddress());
            knownPubKey.put(privKey.getBase58(), chatId.toString());
            knownAddress.put(privKey.getAddress(), chatId.toString());

            String lang = null;
            if (object instanceof User) {
                User user = (User) object;
                settings.put("user", GSON.toJsonTree(user));
                settings.put("lang", lang = user.languageCode());
                settings.put("name", lang = user.username());
            } else if (object instanceof Chat) {
                Chat chat = (Chat) object;
                settings.put("chat", GSON.toJsonTree(chat));
                settings.put("name", lang = chat.username());
            }

            if (chatId != 0 & object != null
                    && !chatId.equals(adminChatId)
                    // и еще проверим -- если на этот счет не поступало ранее никаких средств - он точно пустой
                    // проверка по позиции баланса "Всего Приход":
                    && privKey.getBalanceForPosition(FEE_KEY, Account.BALANCE_POS_OWN).a.compareTo(BigDecimal.ZERO) == 0) {
                // Это новый пользователь - надо ему выслать награду
                giftOnMeet(settings, chatId, object, privKey, lang);
            }

            saveSettings();

        }

        return (JSONObject) settingsAllChats.get(key);

    }

    //
    private void sendOnSetMemberStatus(Long chatId, ChatMember newChatMember) {
        Boolean canEditMessages = newChatMember.canEditMessages();

        sendSimpleText(chatId, "!!!!\nВНИМАНИЕ! Без прав администратора я не смогу читать сообщения и делать свою работу... Пожалуйста наделите меня правами администратора в этом чате");

    }

    protected void giftOnMeet(JSONObject settings, Long chatId, Object object, PublicKeyAccount account, String lang) {
    }

    /**
     * Либо генерирует свое либо берет уже то что задано - можно положить свой туда
     *
     * @param chatId
     * @param object
     * @return
     */
    protected PrivateKeyAccount getPrivKey(Long chatId, Object object) {
        JSONObject settings = getChatSettings(chatId, object);
        return new PrivateKeyAccount(Base58.decode((String) settings.get(PRIVATE_KEY_SEED)));
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

    protected ConcurrentHashMap<String, Fun.Tuple5<Message, Boolean, String, String, Transaction>> pendingTxs = new ConcurrentHashMap<>();


    // TODO sendChatAction - https://core.telegram.org/bots/api#sendchataction
    protected void sendTransaction(JSONObject settings, Transaction transaction, Long replyChatId, Integer replyMessageId, boolean replayOnDone, String pendingMess, String domeMess, String lang) {

        cnt.createForNetwork(transaction);
        int validate = cnt.afterCreateForNetwork(transaction, false, false);
        if (validate == VALIDATE_OK) {
            SendMessage sendMessage;
            if (validate == VALIDATE_OK) {
                // Используем разметку Markdown
                sendMessage = new SendMessage(replyChatId, pendingMess + "\nПосмотреть транзакцию \"" + scanUrlMarkdown(transaction.viewTypeName(langList.getLangJson(lang)), "tx", transaction.viewSignature()) + "\" в блокчейн");

            } else {
                JSONObject out = new JSONObject();
                transaction.updateMapByError2(out, validate, lang);
                String text = "```java Error\n" + out.toJSONString() + "```";
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
                    pendingTxs.put(Base58.encode(transaction.getSignature()), new Fun.Tuple5<>(response.message(), replayOnDone, domeMess, lang, transaction.copy()));
                    log.info("Reply sent");
                } else {
                    log.error("Send ERROR: {}", GSON.toJson(response));
                }
            } catch (Exception e) {
                log.error("Send ERROR: {}", e.toString());
            }

        } else {
            JSONObject out = new JSONObject();
            transaction.updateMapByError2(out, validate, lang);
            sendMarkdown(replyChatId, "```java Error\n" + out.toJSONString() + "```");
        }

    }

    protected void rSend(JSONObject settings, Long chatId, Integer replyMessageId, User user, BigDecimal amount, Account account, String lang) {

        long key = FEE_KEY;
        // TODO hide - если Скрытно задано то имя не писать
        String title = "Награда от " + user.username() + " - " + botUserNameF;
        byte[] message = null;
        byte[] isText = new byte[]{1};
        byte[] encrypted = new byte[]{0};
        long flags = 0L;
        Transaction rSend = new RSend(getBotPrivateKey(), account, key, amount, title, message, isText, encrypted, flags);
        sendTransaction(settings, rSend, chatId, replyMessageId, true,
                String.format("Подарок выслан *%s %s* (Можно сохранить сообщений в общей сложности размером примерно на %d кБ)... Срок доставки подарка примерно через %d секунд.",
                        amount.toPlainString(), AssetCls.FEE_NAME, Account.bytesPerBalance(amount) / 2000, BlockChain.GENERATING_MIN_BLOCK_TIME_MS(rSend.getTimestamp()) / 800),
                "Награда доставлена!", lang);

    }

    /**
     * Опрос блокчейн для проверки подтверждения транзакций
     */
    protected synchronized void updateTransactions() {

        DCSet dcSet = cnt.getDCSet();
        Iterator<Map.Entry<String, Fun.Tuple5<Message, Boolean, String, String, Transaction>>> iterator = pendingTxs.entrySet().iterator();
        Map.Entry<String, Fun.Tuple5<Message, Boolean, String, String, Transaction>> entry;
        while (iterator.hasNext()) {
            entry = iterator.next();

            Fun.Tuple5<Message, Boolean, String, String, Transaction> item = entry.getValue();
            Message messge = item.a;
            if (messge == null) {
                iterator.remove();
                continue;
            }

            if (item.e.getBlockHeight() <= 0) {
                // если транзакция еще не подтверждена
                // проверим ее саму
                Long seq = cnt.getTransactionSeqBySign(item.e.getSignature(), dcSet);
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
                log.info("{} done: {}", botTitle, tx.viewHeightSeq());
                item.e.setHeightSeq(tx.getDBRef());
            }

            if (item.e.getConfirmations(dcSet) < PENDING_CONFIRMS)
                continue;

            // сюда приходит если транзакция подтверждена, но в чате сообщение не обновилось что подтверждено
            BaseResponse response;
            String out = item.c + "\nПроверить транзакцию \"" + scanUrlMarkdown(item.e.viewTypeName(langList.getLangJson(item.d)), "tx", item.e.viewHeightSeq()) + "\" в блокчейн";
            if (item.b) {
                // сделаем как ответ на первое
                SendMessage sendMessage = new SendMessage(messge.chat().id(), out);
                sendMessage.parseMode(ParseMode.Markdown);
                sendMessage.replyToMessageId(item.a.messageId());
                response = bot.execute(sendMessage);
            } else {
                // изменим первое
                EditMessageText editMessage = new EditMessageText(messge.chat().id(), messge.messageId(), out);
                editMessage.parseMode(ParseMode.Markdown);
                response = bot.execute(editMessage);
            }

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
                Transaction tx = iterator.next().getValue().e;
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
        long longId = Long.parseLong(chatId);
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
                sendSimpleText(longId, mess.toString());
            else
                sendMarkdown(longId, "```java Transaction\n" + mess + "```");
        }

        sendMarkdown(longId, "\nПосмотреть транзакцию \"" + scanUrlMarkdown(tx.viewTypeName(langList.getLangJson(lang)), "tx", tx.viewHeightSeq()) + "\" в блокчейн");

        if (test || BlockChain.TEST_MODE)
            sendMarkdown(longId, "```java Transaction\n" + tx.toJson() + "```");

    }

    /**
     * Проверим все транзакции в блоке - если они пришли к нашим известным, то вызов
     *
     * @param txs
     */
    public void processBlock(List<Transaction> txs) {
        if (bot == null || settingsAllChats == null)
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

                chatSettings = (JSONObject) settingsAllChats.get(chatId);
                if (chatSettings == null)
                    return;

                try {
                    processIncomeTx(chatId, tx, chatSettings, (String) chatSettings.get("lang"));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

            }
        }

    }

    @Override
    public void getRechargeable(Map<Account, Fun.Tuple3<ErachainBotCls, BigDecimal, JSONObject>> recharges) {
        if (bot == null || settingsAllChats == null)
            return;

        ((JSONObject) settingsJSON.get("chats")).forEach((id, val) -> {
            JSONObject chatSettings = (JSONObject) val;
            String charge = (String) chatSettings.get("charge");
            if (charge == null && chatSettings.containsKey("chat"))
                charge = "0.05"; // TODO настройку по умолчанию для чатов сделать
            if (charge == null)
                return;
            Account account = new Account((String) chatSettings.get("address"));
            Fun.Tuple3<ErachainBotCls, BigDecimal, JSONObject> recharge = recharges.get(account);
            BigDecimal amount = new BigDecimal(charge);
            if (recharge == null || recharge.b.compareTo(amount) < 0) {
                // только если больше то обновим
                recharges.put(account, new Fun.Tuple3(this, amount, chatSettings));
            }
        });
    }

    public void stop() {
        if (bot == null)
            return;

        sendToAdminMessage("Stopped...");

        settingsAllChats.keySet().forEach(chatId -> {
            if (((JSONObject) settingsAllChats.get(chatId)).get("name") != null)
                // пользователям не пишем
                return;
            sendSimpleText(Long.parseLong((String) chatId), "Моя работа временно прекращена, надеюсь не на долго...");
        });

        bot.removeGetUpdatesListener();
        bot.shutdown();
    }

    interface Chater {
    }
}
