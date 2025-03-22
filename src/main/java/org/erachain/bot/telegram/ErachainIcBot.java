package org.erachain.bot.telegram;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.MessageEntity;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.response.BaseResponse;
import org.erachain.controller.Controller;

import java.util.ArrayList;
import java.util.List;

/**
 * Бот для Телеграм. Если мы ведем учет по ID пользователей без возможности "снять" со счета, то можно просто создавать счет по ID без приватного и публичного ключей
 * see https://github.com/pengrad/java-telegram-bot-api?tab=readme-ov-file#creating-your-bot
 * set start args:  -bot_tgm_token=TOKEN
 * В файле настроек нужно задать основной Сид из которого будут создаваться ключи для групп - файл settings_tgm_bot.json, поле baseSeed. Если его не задать он создаётся сам. Но его нужно потом сохранить - чтобы не потерять связь со счетами чатов
 */
public class ErachainIcBot extends ErachainBotCls {

    public ErachainIcBot(Controller cnt) {
        super(cnt);
    }

    @Override
    protected String getShortName() {
        return "ic";
    }

    @Override
    protected String[] retrieveChatReplyCommand(String text) {
        return null;
    }

    @Override
    protected String[] retrieveChatMessageCommand(String text) {
        return null;
    }

    @Override
    protected boolean privateForwardReplyHasCommand(String text) {
        return false;
    }

    @Override
    protected boolean processAutomaticForward(Long makerTxId, Chat replyChat, Chat chatMain, Message message, String lang) {
        return false;
    }

    @Override
    protected boolean processMessage(Long makerTxId, Chat replyChat, Chat chatMain, Message message, String lang) {
        return false;
    }

    @Override
    protected void makeEntitiesCommands(String text, List<MessageEntity> commands, MessageEntity entity) {
    }

    @Override
    protected boolean processForwardOrigin(Long makerTxId, Chat replyChat, Integer replyMessageId, Chat origChat, Integer origMessageId, Integer origMessageDate, Message message, String[] commands, String lang) {
        return false;
    }

    @Override
    protected boolean processLinkPreviewOptions(Long makerTxId, Chat replyChat, Integer replyMessageId, Message message, String[] commands, String lang) {
        return false;
    }

    @Override
    protected boolean processChatReplyCommand(Long makerTxId, Chat replyChat, Integer replyMessageId, Chat origChat, Integer origMessageId, Integer origMessageDate, Message message, String[] commands, String lang) {
        return false;
    }

    @Override
    protected boolean processCommand(Long makerTxId, Chat replyChat, Integer replyMessageId, Chat origChat, Integer origMessageId, Integer origMessageDate, Message message, String[] commands, String lang) {
        return false;
    }

    @Override
    protected List<Object[]> getJobCommandsList() {
        return new ArrayList<>();
    }

    @Override
    protected String getHeadHelpMessage(String lang) {
        return "...help mess...";
    }

    @Override
    protected String helpLinksMessage(String lang) {
        return "...help mess...";
    }

    @Override
    protected BaseResponse sendGreetingsMessage(Long chatId, String lang) {
        return sendMarkdown(chatId, "Привет!\n" + getGreetingsHelp(true, lang));
    }

    @Override
    protected BaseResponse sendHelpMessage(Long chatId, String lang) {
        return sendSimpleText(chatId, "Помощь...");
    }

    @Override
    protected BaseResponse sendGreetingsPrivateMessage(Long chatId, User user, String lang) {
        return sendMarkdown(chatId, "Привет *" + user.firstName() + "*!\n" + getGreetingsHelp(false, lang));
    }

    @Override
    protected BaseResponse sendHelpPrivateMessage(Long chatId, User user, String lang) {
        return sendSimpleText(chatId, "Помощь...");
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

}
