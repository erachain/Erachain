package org.erachain.bot.telegram;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.MessageEntity;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.message.origin.MessageOrigin;
import com.pengrad.telegrambot.model.message.origin.MessageOriginChannel;
import com.pengrad.telegrambot.model.message.origin.MessageOriginChat;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.response.BaseResponse;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.exdata.ExData;
import org.erachain.core.exdata.exActions.ExAction;
import org.erachain.core.exdata.exLink.ExLink;
import org.erachain.core.exdata.exLink.ExLinkAuthor;
import org.erachain.core.exdata.exLink.ExLinkSource;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.RSignNote;
import org.erachain.core.transaction.Transaction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.erachain.core.item.assets.AssetCls.FEE_KEY;
import static org.erachain.core.transaction.Transaction.VALIDATE_OK;

/**
 * Бот для Телеграм.
 * see https://github.com/pengrad/java-telegram-bot-api?tab=readme-ov-file#creating-your-bot
 * set Environment variable blockchain_storage_bot=[TOKEN]
 * В файле настроек [blockchain_storage_bot.json] нужно задать основной Сид из которого будут создаваться ключи для групп, поле baseSeed. Если его не задать он создаётся сам. Но его нужно потом сохранить - чтобы не потерять связь со счетами чатов
 */
public class ErachainStorageBot extends ErachainBotCls {

    private static final Logger log = LoggerFactory.getLogger(ErachainStorageBot.class.getSimpleName());

    public ErachainStorageBot(Controller cnt) {
        super(cnt);
    }

    @Override
    protected String getShortName() {
        return "storage";
    }

    enum Mode {
        AsOpen, AsHide, OnlyMarked, OnlyOpen, OnlyHide;

        static Mode get(Integer id) {
            switch (id) {
                case 1:
                    return AsOpen;
                case 2:
                    return AsHide;
                case 3:
                    return OnlyMarked;
                case 4:
                    return OnlyOpen;
                case 5:
                    return OnlyHide;
            }
            return null;
        }

        static Integer get(Mode id) {
            switch (id) {
                case AsOpen:
                    return 1;
                case AsHide:
                    return 2;
                case OnlyMarked:
                    return 3;
                case OnlyOpen:
                    return 4;
                case OnlyHide:
                    return 5;
            }
            return null;
        }

        static String getModeDescr(Integer mode) {
            switch (mode) {
                case 1:
                    return "Режим Открыто. Сохраняются все сообщения и сообщения без пометки сохраняются *открыто*.";
                case 2:
                    return "Режим Скрыто. Сохраняются все сообщения и сообщения без пометки сохраняются *скрыто*.";
                case 3:
                    return "Режим Запрет без меток. Сообщения без меток *Открыто* или *Скрыто* не сохраняются.";
                case 4:
                    return "Режим только Открыто. Сохраняются сообщения только с пометкой *Открыто*.";
                case 5:
                    return "Режим только Скрыто. Сохраняются сообщения только с пометкой *Скрыто*.";
            }
            return "UNKNOWN";
        }

        static String view(Integer mode) {
            return " работы: " + mode + "\n\n_" + Mode.getModeDescr(mode) + "_";
        }

    }

    int getModeInt(JSONObject chatSettings) {
        Number modeL = (Number) chatSettings.get("mode");
        Object user = chatSettings.get("user");
        if (modeL == null)
            // для чатов по умолчанию только с пометками, для личных - открыто
            return user == null ? 3 : 1;
        else
            return modeL.intValue();
    }

    Mode getModeType(JSONObject chatSettings) {
        Number modeL = (Number) chatSettings.get("mode");
        if (modeL == null)
            //return Mode.OnlyMarked;
            return Mode.AsOpen;
        else
            return Mode.get(modeL.intValue());
    }

    int getModeInt(Long chatId, Object object) {
        return getModeInt(getChatSettings(chatId, object));
    }

    boolean getFullDefault(JSONObject chatSettings) {
        Number value = (Number) chatSettings.get("full");
        if (value == null)
            return false;
        else
            return value.intValue() == 1;
    }

    static String getFullDefaultDescr(boolean value) {
        return value ? "По умолчанию сохранять Оригинал сообщения. Данные сообщения будут сохранены полностью, включая и форматирование. Это позволяет отображать сообщение с форматированием"
                : "По умолчанию сохраняется только текст сообщения. Форматирование будет утеряно";
    }

    protected List<Object[]> getSettingsCommandsList() {
        List<Object[]> out = super.getSettingsCommandsList();
        out.add(new Object[]{new String[]{"mode", "режим"}, "Режим работы по умолчанию: как сохранять сообщения в блокчейн? Открыто или закрыто или только по команде"});
        out.add(new Object[]{new String[]{"full", "весь", "всё", "все"}, "Сохранить все данные из сообщения включая и форматирование. Если задать с минусом, например так: `-full`, то сохранит только текст"});
        return out;
    }

    @Override
    protected List<Object[]> getJobCommandsList() {
        List<Object[]> out = new ArrayList<>();
        out.add(new Object[]{new String[]{"store", "храни"}, "Сохранить сообщение"});
        return out;
    }

    protected List<Object[]> getPrivateJobCommandsList() {
        List<Object[]> out = new ArrayList<>();
        out.add(new Object[]{new String[]{"say", "весть"}, "Послать весточку (сообщение) на заданный счет в блокчейне. Более высокая секретность чем сообщения в Телеграм, так как получатель неизвестен Телеграму. весточки не сохраняются в блокчейне и удаляются из сети через некоторое время (за час). (задумка)"});
        out.add(new Object[]{new String[]{"text", "сказ"}, "Послать сообщение н заданный счёт в блокчейн. Такое сообщение будет на века сохранено в блокчейне. Получатель так же неизвестен для Телеграм. (задумка)"});
        out.add(new Object[]{new String[]{"gift", "дар"}, "Наградить получателя в блокчейне. награда высылается на заданный счёт. Например так: `дар 0.034 7N4QCN3wBH2u8hhRANHwJCNm4vDLTmrmy1` . (задумка)"});
        return out;
    }

    @Override
    protected boolean botAnswerPublic(String receivedMessage, Chat chat, User user) {
        if (super.botAnswerPublic(receivedMessage, chat, user))
            return true;

        switch (receivedMessage) {
            case "mode":
            case "режим":

                int mode = getModeInt(chat.id(), chat);
                sendMarkdown(chat.id(), "Текущий режим" + Mode.view(mode));
                return true;

            case "full":
            case "весь":
            case "всё":
            case "все":
                sendSimpleText(chat.id(), getFullDefaultDescr(getFullDefault(getChatSettings(chat.id(), chat))));
                return true;

            case "restore":
                sendSimpleText(chat.id(), "Coming soon...");
                return true;
        }

        if (receivedMessage.startsWith(":store") || receivedMessage.startsWith(":храни")) {
            sendSimpleText(chat.id(), "Coming soon...");
            return true;
        }

        return false;
    }

    private boolean editEncryptMode(String[] command, Long chatId, Object object) {
        // Задать другой режим работы
        // @blockchain_storage_bot mode 3
        if (command.length == 1) {
            int mode = getModeInt(chatId, object);
            sendMarkdown(chatId, "Текущий режим" + Mode.view(mode));
            return true;
        }

        try {
            Integer newMode = Integer.parseInt(command[1]);
            if (Mode.get(newMode) == null) {
                sendSimpleText(chatId, "Неверный режим. Используйте значения 1..4");
            } else {
                getChatSettings(chatId, object).put("mode", newMode);
                saveSettings();
                sendMarkdown(chatId, "Задан новый режим" + Mode.view(newMode));
            }
        } catch (NumberFormatException e) {
            sendSimpleText(chatId, "Неверный режим. Используйте значения 1..4");
        }

        return true;
    }

    private boolean editFullMode(String[] command, Long chatId, Object object) {
        // Задать другой режим работы
        // @blockchain_storage_bot mode 3
        if (command.length == 1) {
            JSONObject chatSettings = getChatSettings(chatId, object);
            sendSimpleText(chatId, getFullDefaultDescr(getFullDefault(chatSettings)));
            return true;
        }

        try {
            Integer newFullMode = Integer.parseInt(command[1]);
            if (newFullMode == null) {
                sendSimpleText(chatId, "Неверное значение. Используйте значения 0 или 1");
            } else {
                JSONObject chatSettings = getChatSettings(chatId, object);
                chatSettings.put("full", newFullMode);
                saveSettings();
                sendSimpleText(chatId, getFullDefaultDescr(getFullDefault(chatSettings)));
            }
        } catch (NumberFormatException e) {
            sendSimpleText(chatId, "Неверное значение. Используйте значения 0 или 1");
        }
        return true;
    }

    private boolean giftFor(String[] command, Long chatId, Integer replyMessageId, Chat chat, User user) {
        // Задать другой режим работы
        // @blockchain_storage_bot mode 3
        if (command.length < 3) {
            sendMarkdown(chatId, "Недостаточно данных. Необходимо указать *Сколько* и *Кому*, например так:\n\n`дар 0.05 7Jsj54LJaZd7TbRExstsDb8YmCi1aGLRvZ`");
            return true;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(command[1]);
        } catch (NumberFormatException у) {
            sendMarkdown(chatId, "Необходимо правильно указать количество (через точку), например так:\n\n`дар 0.05 7Jsj54LJaZd7TbRExstsDb8YmCi1aGLRvZ`");
            return true;
        }

        Fun.Tuple2<Account, String> result = Account.tryMakeAccount(command[2]);
        if (result.a == null) {
            sendSimpleText(chatId, "Неправильный счет получателя: " + result.b);
            return true;
        }

        rSend(null, chatId, replyMessageId, user, amount, result.a, user.languageCode());
        return true;
    }

    protected boolean botAnswerAdmin(String[] command, Chat chat, User user) {
        if (super.botAnswerAdmin(command, chat, user))
            return true;

        switch (command[0].toLowerCase()) {
            case "mode":
            case "режим":
                return editEncryptMode(command, chat.id(), chat);
            case "full":
            case "весь":
            case "всё":
            case "все":
                return editFullMode(command, chat.id(), chat);
        }
        return false;
    }


    protected boolean botAnswerPrivate(String[] command, long chatId, Chat chatPrivate, Integer replyMessageId, User user) {
        if (super.botAnswerPrivate(command, chatId, chatPrivate, replyMessageId, user))
            return true;

        switch (command[0].toLowerCase()) {
            case "mode":
            case "режим":
                // Задать другой режим работы - -1002230015360
                // @blockchain_storage_bot mode 3
                return editEncryptMode(command, chatId, chatPrivate);
            case "full":
            case "весь":
            case "всё":
            case "все":
                return editFullMode(command, chatId, chatPrivate);
            case "gift":
            case "дар":
            case "дари":
                return giftFor(command, chatId, replyMessageId, chatPrivate, user);
            case "say":
            case "весть":
            case "text":
            case "сказ":
                sendSimpleText(chatId, "Пока не реализовано...");
                return true;
            case "game":
            case "игра":
            case "играть":
                sendSimpleText(chatId, "Скоро... Игра уже в разработке!");
                return true;
            case "yes":
            case "no":
            case "да":
            case "нет":
                return true;
        }

        return false;
    }

    @Override
    protected String getHeadHelpMessage(String lang) {
        return commandsHelp = commandsHelp == null ? "" : commandsHelp;
    }

    @Override
    protected String helpLinksMessage(String lang) {
        return "Полное [описание работы](https://docs.google.com/document/d/1aW9KqeKFTgGru7z6g4GplRIyqQERWIQVd4Zjje_OrNc/edit?usp=sharing)" +
                " бота. Исходные коды блокчейн-платформы *Erachain* включая и код самого бота [лежат тут](https://gitlab.com/erachain/Erachain).";
    }

    protected String helpSettingCommands(boolean asPrivate) {
        String text = "\n\n*Команды настройки*\n====\n";
        for (Object[] obj : getSettingsCommandsList()) {
            String[] commands = (String[]) obj[0];
            text += "- `" + String.join("`, `", commands) + "` - " + obj[1] + "\n";
        }

        text += "\nКоманды настроек пишутся без наклонной черты. В групповых чатах команды настройки начинаются с имени бота, например так:";
        text += "\n\n`" + botUserNameF + " счет`\n\n";
        text += "Если же вы пишете команду приватно боту, то имя вставлять не надо. Например так:\n\n`счет`\n\n";

        return text;
    }

    protected String helpJobCommands() {
        String text = "\n\n*Автоматическое сохранение сообщений*\n====\n";
        text += "Для автоматического сохранения сообщений из чата (группы, канала) летописец должен быть запущен в этом чате как администратор."
                + "Для свободного сохранения открытых сообщений необходимо удостоверить счет бота.\n"
                + "Так же не забывайте пополнять счет бота для его полноценной работы.\n";
        text += "\n*Сохранение сообщений личным летописцем*\n====\n";
        text += "Для сохранения любого сообщения перешлите его в личного летописца (через приватное сообщение боту).\n";
        text += "\n*Команды работы*\n====\n";
        text += "*Сохранить сообщение*\n`/store`\n`/храни`\nЧтобы сохранить вручную отдельное сообщение, сделайте ответ на него и укажите эту команду."
                + "";

        return text;
    }

    protected String helpPrivateJobCommands() {
        String text = "\n\n*Команды личного летописца*\n====\n";
        for (Object[] obj : getPrivateJobCommandsList()) {
            String[] commands = (String[]) obj[0];
            text += "- `" + String.join("`, `", commands) + "` - " + obj[1] + "\n";
        }

        return text;
    }

    @Override
    protected BaseResponse sendGreetingsMessage(Long chatId, String lang) {
        StringBuilder mess = new StringBuilder();
        mess.append("Приветствую!\n\nЭто летописец для вашего чата - ").append(botUserNameMD).append(".\n")
                .append(getGreetingsHelp(true, lang)).append(getHeadHelpMessage(lang));
        return sendMarkdown(chatId, mess.toString());
    }

    @Override
    protected BaseResponse sendGreetingsNewMember(Long chatId, User user) {
        return sendSimpleText(chatId, user.username() + ", приветствую тебя! Я могу записывать сообщения в блокчейн.");
    }

    @Override
    protected BaseResponse sendHelpMessage(Long chatId, String lang) {
        String text = "Приветствую!\n\nЭто летописец для вашего чата - " + botUserNameMD + ".\n";

        text += getHeadHelpMessage(lang);
        sendMarkdown(chatId, text);
        sendMarkdown(chatId, helpSettingCommands(false));
        sendMarkdown(chatId, helpJobCommands());
        return sendMarkdown(chatId, helpLinksMessage(lang));

    }

    @Override
    protected BaseResponse sendGreetingsPrivateMessage(Long chatId, User user, String lang) {
        StringBuilder mess = new StringBuilder();
        mess.append("Приветствую *").append(user.firstName()).append("!*\n\nЭто ваш персональный летописец - ").append(botUserNameMD).append(".\n")
                .append(getGreetingsHelp(false, lang));

        if (chatId.equals(adminChatId))
            mess.append("\n----\nВы являетесь администратором бота\n");

        mess.append(getHeadHelpMessage(lang));
        return sendMarkdown(chatId, mess.toString());

    }

    @Override
    protected BaseResponse sendHelpPrivateMessage(Long chatId, User user, String lang) {
        String text = "Приветствую *" + user.firstName() + "!*\n\nЭто ваш персональный летописец - " + botUserNameMD + ".\n";
        if (chatId.equals(adminChatId))
            sendSimpleText(chatId, "\n----\nВы являетесь администратором бота\n");

        text += getHeadHelpMessage(lang);
        sendMarkdown(chatId, text);
        sendMarkdown(chatId, helpSettingCommands(true));
        sendMarkdown(chatId, helpPrivateJobCommands());
        sendMarkdown(chatId, helpJobCommands());
        return sendMarkdown(chatId, helpLinksMessage(lang));

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

    void game(JSONObject settings, Long chatId, PublicKeyAccount account, User user, String lang) {
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            return;
        }

        sendSimpleText(chatId, user.firstName() + ", а хотите сыграть со мной в игру?");

        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            return;
        }

        sendMarkdown(chatId, "Просто напишите слово `игра`...");

    }

    @Override
    protected void giftOnMeet(JSONObject settings, Long chatId, Object object, PublicKeyAccount account, String lang) {

        if (object != null && object instanceof User)
            new Thread(() -> {
                game(settings, chatId, account, (User) object, lang);
            }).start();

        sendSimpleText(chatId, "Вы новый пользователь, Вас ждет награда!");
        long key = FEE_KEY;
        BigDecimal amount = new BigDecimal("0.05");
        String title = "Награда от бота " + botUserNameF;
        byte[] message = "Это награда новому пользователю за использование телеграм-бота".getBytes(StandardCharsets.UTF_8);
        byte[] isText = new byte[]{1};
        byte[] encrypted = new byte[]{0};
        long flags = 0L;
        Transaction tx = new RSend(getBotPrivateKey(), account, key, amount, title, message, isText, encrypted, flags);
        cnt.createForNetwork(tx);
        int result = cnt.afterCreateForNetwork(tx, false, false);
        if (result == VALIDATE_OK) {
            sendMarkdown(chatId, String.format("Вам выслан Подарок *%s %s* (Вы сможете сохранить сообщений в общей сложности размером примерно на %d кБ)... Срок доставки подарка примерно через %d секунд.",
                    amount.toPlainString(), AssetCls.FEE_NAME, Account.bytesPerBalance(amount) / 2000, BlockChain.GENERATING_MIN_BLOCK_TIME_MS(tx.getTimestamp()) / 800));
        } else {
            // запомним что ему не выплачен бонус за вступление
            settings.put("meet", amount.toPlainString());
            sendSimpleText(chatId, "Подарок будет отправлен позже...");
            // сообщить админу
            sendToAdminMessage(String.format("Баланс счёта бота пуст, пополните `%s`", getBotPrivateKey().getAddress()));
        }

    }

    /**
     * Команда как ответ на некоторое сообщение
     *
     * @param text
     * @return
     */
    @Override
    protected String[] retrieveChatReplyCommand(String text) {

        if (text.startsWith("/store") || text.startsWith("/храни")) {
            return new String[]{text, null};
        }
        return null;
    }

    @Override
    protected String[] retrieveChatMessageCommand(String text) {

        String[] result = new String[2];

        String command = null;
        int lineStart = -1, lineEnd = -1;

        String[] lines = text.split("\n");

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith(":store") || lines[i].startsWith(":храни")) {
                // найдена команда на сохранение - начало
                lineStart = i + 1;
                command = lines[i];
                result[0] = command;

            } else if (lines[i].equals(":")) {
                // найдена команда на сохранение - начало
                lineEnd = i;
            }
        }

        if (lineStart == -1 || command != null && (command.contains("full") || command.equals("весь") || command.equals("всё") || command.equals("все"))) {
            // команд нет, без изменений
        } else if (lineStart == lines.length) {
            // команда в конце - уберем ее с конца
            text = "";
            for (int i = 0; i < lineStart - 1; i++) {
                text += lines[i] + "\n";
            }
        } else {
            // сохранить весь текст - в команде параметры заданы
            if (lineEnd == -1)
                lineEnd = lines.length;

            // вырежем нужный участок текста
            text = "";
            for (int i = lineStart; i < lineEnd; i++) {
                text += lines[i] + "\n";
            }
        }

        result[1] = text;
        return result;

    }

    @Override
    protected boolean privateForwardReplyHasCommand(String text) {
        // Если это одна строка и в ней есть команда впереди
        return !text.contains("\n") && (
                text.startsWith("/store") || text.startsWith("/храни")
                        || text.startsWith(":store") || text.startsWith(":храни")
                        || text.startsWith("store") || text.startsWith("храни")
        );
    }

    /**
     * Команду в сообщении может дать как админ чата так и простой пользователь.
     * Транзакция создается по makerTxId, но
     *
     * @param makerTxId
     * @param replyChat
     * @param chatMain
     * @param message
     * @param lang
     * @return
     */
    @Override
    protected boolean processAutomaticForward(Long makerTxId, Chat replyChat, Chat chatMain, Message message, String lang) {

        String text = message.caption();
        if (text == null)
            text = message.text();

        String[] commands = retrieveChatMessageCommand(text);
        JSONObject settings = getChatSettings(makerTxId, replyChat);
        Mode mode = getModeType(settings);
        if (!mode.equals(Mode.AsOpen) && !mode.equals(Mode.AsHide)) {
            // Значит по умолчанию нельзя - тогда проверим а есть ли команда внутри сообщения
            if (commands[0] == null)
                return true;
        }

        MessageOrigin originMessage = message.forwardOrigin();
        if (originMessage != null) {
            if (originMessage instanceof MessageOriginChannel) {
                MessageOriginChannel messageOriginChannel = (MessageOriginChannel) originMessage;
                return processCommand(makerTxId, replyChat, message.messageId(), messageOriginChannel.chat(), messageOriginChannel.messageId(), originMessage.date(), message, commands, lang);
            } else if (originMessage instanceof MessageOriginChat) {
                MessageOriginChat messageOriginChat = (MessageOriginChat) originMessage;
                return processCommand(makerTxId, replyChat, message.messageId(), messageOriginChat.senderChat(), message.messageId(), originMessage.date(), message, commands, lang);
            }
        }

        return processCommand(makerTxId, replyChat, message.messageId(), chatMain, message.messageId(), message.date(), message, commands, lang);
    }

    @Override
    protected boolean processMessage(Long makerTxId, Chat replyChat, Chat chatMain, Message message, String lang) {

        String text = message.caption();
        if (text == null)
            text = message.text();

        String[] commands = retrieveChatMessageCommand(text);
        JSONObject settings = getChatSettings(makerTxId, replyChat);
        Mode mode = getModeType(settings);
        if (!mode.equals(Mode.AsOpen) && !mode.equals(Mode.AsHide)) {
            // Значит по умолчанию нельзя - тогда проверим а есть ли команда внутри сообщения
            if (commands[0] == null)
                return true;
        }

        MessageOrigin originMessage = message.forwardOrigin();
        if (originMessage != null) {
            if (originMessage instanceof MessageOriginChannel) {
                MessageOriginChannel messageOriginChannel = (MessageOriginChannel) originMessage;
                return processCommand(makerTxId, replyChat, message.messageId(), messageOriginChannel.chat(), messageOriginChannel.messageId(), originMessage.date(), message, commands, lang);
            } else if (originMessage instanceof MessageOriginChat) {
                MessageOriginChat messageOriginChat = (MessageOriginChat) originMessage;
                return processCommand(makerTxId, replyChat, message.messageId(), messageOriginChat.senderChat(), message.messageId(), originMessage.date(), message, commands, lang);
            }
        }

        return processCommand(makerTxId, replyChat, message.messageId(), chatMain, message.messageId(), message.date(), message, commands, lang);
    }

    @Override
    protected void makeEntitiesCommands(String text, List<MessageEntity> commands, MessageEntity entity) {
        String command = text.substring(entity.offset() + 1, entity.offset() + entity.length());
        if (commands.isEmpty()) {
            if (command.equals("/store")) {
                //int end = text.indexOf('\n', entity.offset());

                //entity.url()
                commands.add(entity);
            }
        } else if (commands.size() == 1) {
            // тут может быт только конец

        } else
            // больше 2-х команд игнорируем
            return;
    }


    /**
     * 1. Ответ на новость из канала - makerTxId = бот_админ, <br>
     * 2. ответ на личное сообщение боту - <br>
     * 3. ответ на команду в ответе на сообщение <br>
     * 4. ответ на сообщение с командой в чате от админа <br>
     * 5. ответ на сообщение с командой в чате от пользователя <br>
     *
     * @param makerTxId
     * @param replyChat
     * @param replyMessageId
     * @param origChat
     * @param origMessageId
     * @param origMessageDate
     * @param message
     * @param commands 0 - строка с командами, 1 - сам текст, иначе берет из message
     * @param lang
     * @return
     */
    @Override
    protected boolean processForwardOrigin(Long makerTxId, Chat replyChat, Integer replyMessageId, Chat origChat, Integer origMessageId, Integer origMessageDate, Message message, String[] commands, String lang) {
        return processCommand(makerTxId, replyChat, replyMessageId, origChat, origMessageId, origMessageDate, message, commands, lang);
    }

    @Override
    protected boolean processLinkPreviewOptions(Long makerTxId, Chat replyChat, Integer replyMessageId, Message message, String[] commands, String lang) {
        return processCommand(makerTxId, replyChat, replyMessageId, null, null, null, message, commands, lang);
    }

    @Override
    protected boolean processChatReplyCommand(Long makerTxId, Chat replyChat, Integer replyMessageId, Chat origChat, Integer origMessageId, Integer origMessageDate, Message message, String[] commands, String lang) {
        return processCommand(makerTxId, replyChat, replyMessageId, origChat, origMessageId, origMessageDate, message, commands, lang);
    }

    @Override
    protected boolean processCommand(Long makerTxId, Chat replyChat, Integer replyMessageId, Chat origChat, Integer origMessageId, Integer origMessageDate, Message message, String[] commands, String lang) {

        JSONObject settings = getChatSettings(makerTxId, replyChat);
        int mode = getModeInt(settings);
        boolean fullModeDefault = getFullDefault(settings);

        Boolean isEncrypted = null;
        Boolean storeFullJson = null;
        boolean testProc = test ? test : false;
        if (commands != null && commands[0] != null) {
            // соберем параметры
            String[] words = commands[0].split(":");
            for (String word : words) {
                if (word.equals("hide") || word.equals("закрыто") || word.equals("скрыто")) isEncrypted = true;
                else if (word.equals("open") || word.equals("открыто")) isEncrypted = false;
                else if (word.equals("full") || word.equals("весь") || word.equals("всё") || word.equals("все"))
                    storeFullJson = true;
                else if (word.equals("-full") || word.equals("-весь") || word.equals("-всё") || word.equals("-все"))
                    storeFullJson = false;
                else if (word.equals("test") || word.equals("проба")) testProc = true;
            }
        }

        if (storeFullJson == null) {
            storeFullJson = fullModeDefault;
        }

        String text = commands == null ? null : commands[1];
        if (Boolean.TRUE.equals(storeFullJson)) {
            text = GSON.toJson(message);
        } else {
            if (text == null)
                text = message.caption();
            if (text == null)
                text = message.text();
        }

        if (isEncrypted == null) {
            if (mode == 1) isEncrypted = false;
            else if (mode == 2) isEncrypted = true;
            else return true; // без меток пропускаем
        } else {
            if (mode == 4 && isEncrypted)
                // только открытые, закрытые пропускаем
                return true;
            else if (mode == 5 && !isEncrypted) {
                // только закрытые, открытые пропускаем
                return true;
            }
        }

        JSONObject out = new JSONObject();
        if (testProc) {
            out.put("command", commands);
            out.put("isEncrypted", isEncrypted);
            out.put("resultText", text);
            if (true)
                log.warn(out.toJSONString());
            else
                sendSimpleText(replyChat.id(), out.toJSONString());
            return true;
        }

        PrivateKeyAccount creator = getPrivKey(makerTxId, replyChat);

        JSONArray chatReceivers = getReceivers(settings);
        Account[] recipients;
        if (chatReceivers.isEmpty()) {
            recipients = null;
        } else {
            recipients = new Account[chatReceivers.size()];
            for (int i = 0; i < chatReceivers.size(); i++) {
                Fun.Tuple2<Account, String> result = Account.tryMakeAccount((String) chatReceivers.get(i));
                if (result.a == null) {
                    sendSimpleText(replyChat.id(), "Неправильный счет получателя: " + chatReceivers.get(i) + result.b);
                    return true;
                }
                recipients[i] = result.a;
            }
        }

        // TODO сделать конвертор из Телеграмм-формата в Маркдаун

        byte[] exDataBytes;
        ExLink exLink = null;
        ExAction action = null;
        String title = this.botTitle;
        String tagsStr = "telegram";

        if (storeFullJson) {
            text = "@TGM" + text;
        } else {
            if (origChat != null) {
                // /https://t.me/iuyiuyiuyiy/530
                // username - URL
                //String title = chatMain.username() == null ? chatMain.title() : "t.me/" + chatMain.username() + "/" + message.messageId();
                title += " - " + origChat.title();
                tagsStr += ",@" + origChat.title() + "," + origChat.id();

                // Будем формировать Маркдаун
                String head = origChat.username() == null ? "## Сказ из Telegram - " + origChat.title()
                        : origMessageId == null ? "## Сказ из Telegram - " + origChat.username()
                        : String.format("## [Сказ из Telegram](http://%s)", "t.me/" + origChat.username() + "/" + origMessageId);
                if (message != null && message.messageThreadId() != null) {
                    head += "\n#### " + String.format("[Тема из Telegram](http://%s)", "t.me/" + origChat.username() + "/" + message.messageThreadId());
                }

                text = head + "\n\n" + text;
                text += "\n\nchat:" + origChat.title() + ", chatId:" + origChat.id() + ", chatURL:" + origChat.username();
                if (origMessageId != null)
                    text += ", messageId:" + origMessageId;
                text += ", time:" + origMessageDate;

            } else {
                if (text != null && message.linkPreviewOptions() != null && text.equals(message.linkPreviewOptions().url())) {
                    title += " " + message.linkPreviewOptions().url();
                    text = null;
                }
            }
        }

        boolean signCanOnlyRecipients = false;
        ExLinkAuthor[] authors = null;
        ExLinkSource[] sources = null;
        Long templateKey = null;
        HashMap<String, String> params_Template = null;
        boolean uniqueTemplate = false;
        boolean uniqueMessage = false;
        HashMap<String, String> hashes_Map = null;
        boolean uniqueHashes = false;
        Set<Fun.Tuple3<String, Boolean, byte[]>> files_Set = Collections.EMPTY_SET;
        boolean uniqueFiles = false;
        try {
            exDataBytes = ExData.make(exLink, action, creator, title,
                    signCanOnlyRecipients, recipients, authors, sources, tagsStr, isEncrypted,
                    templateKey, params_Template, uniqueTemplate,
                    text, uniqueMessage,
                    hashes_Map, uniqueHashes,
                    files_Set, uniqueFiles, true);
        } catch (Exception e) {
            out = out == null ? new JSONObject() : out;
            Transaction.updateMapByErrorSimple2(out, Transaction.INVALID_DATA, e.getMessage(), lang);
            sendSimpleText(replyChat.id(), out.toJSONString());
            return true;
        }

        // CREATE TX MESSAGE
        byte property1 = (byte) 0;
        byte property2 = (byte) 0;
        long key = 0L; // not need for 3 version

        RSignNote issueDoc = (RSignNote) cnt.r_SignNote(RSignNote.CURRENT_VERS, property1, property2,
                creator, 0, key, exDataBytes);

        sendTransaction(settings, issueDoc, replyChat.id(), replyMessageId, false,
                botUserNameMD + (isEncrypted ? ": Запущено сохранение скрытого сообщения... " : ": Запущено сохранение отрытого сообщения... "),
                botUserNameMD + (isEncrypted ? ": Скрытое сообщение сохранено!!" : ": Отрытое сообщение сохранено!!"), lang);

        if (test || settingsInstance.isTestNet()) {
            sendMarkdown(replyChat.id(), "```java " + GSON.toJson(commands) + "```");
            sendSimpleText(replyChat.id(), text);
            sendMarkdown(replyChat.id(), "```java Transaction\n" + issueDoc.toJson().toJSONString() + "```");
        }

        return true;
    }

}
