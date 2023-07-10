package org.erachain.network.message;

import com.google.common.primitives.Ints;
import org.erachain.core.block.Block;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.transaction.Transaction;
import org.erachain.lang.Lang;
import org.erachain.network.Peer;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageFactory {

    static Logger LOGGER = LoggerFactory.getLogger(MessageFactory.class.getName());
    private static MessageFactory instance;

    public static MessageFactory getInstance() {
        if (instance == null) {
            instance = new MessageFactory();
        }

        return instance;
    }

    public Message createGetHWeightMessage() {
        //CREATE A MESSAGE WITH ping ACTION
        return new GetHWeightMessage();
    }

    public Message createGetPeersMessage() {
        //CREATE A MESSAGE WITH getPeers ACTION
        return new GetPeersMessage();
    }

    public Message createPeersMessage(List<Peer> peers) {
        return new PeersMessage(peers);
    }

    public Message createHWeightMessage(Tuple2<Integer, Long> value) {
        return new HWeightMessage(value);
    }

    public Message createVersionMessage(String strVersion, long buildDateTime) {
        return new VersionMessage(strVersion, buildDateTime);
    }

    public Message createFindMyselfMessage(byte[] foundMyselfID) {
        return new FindMyselfMessage(foundMyselfID);
    }

    public Message createGetHeadersMessage(byte[] parent) {
        return new GetSignaturesMessage(parent);
    }

    public Message createHeadersMessage(List<byte[]> headers) {
        return new SignaturesMessage(headers);
    }

    public Message createGetBlockMessage(byte[] header) {
        return new GetBlockMessage(header);
    }

    public Message createWinBlockMessage(Block block) {
        return new BlockWinMessage(block);
    }

    public Message createBlockMessage(Block block) {
        return new BlockMessage(block);
    }

    public Message createTransactionMessage(Transaction transaction) {
        return new TransactionMessage(transaction);
    }

    public Message createTelegramMessage(Transaction transaction) {
        return new TelegramMessage(transaction);
    }

    public Message createTelegramGetMessage() {
        return new TelegramGetMessage();
    }

    public Message createTelegramGetAnswerMessage(ArrayList<String> addresses) {
        return new TelegramAnswerMessage(addresses);
    }

    protected void checkSun(byte[] checksum, byte[] data) throws Exception {

        byte[] digest = Crypto.getInstance().digest(data);

        //TAKE FOR FIRST BYTES
        digest = Arrays.copyOfRange(digest, 0, Message.CHECKSUM_LENGTH);

        //CHECK IF CHECKSUM MATCHES
        if (!Arrays.equals(checksum, digest)) {
            throw new Exception(Lang.T("Invalid data checksum length="));
        }

    }

    public Message parse(Peer sender, DataInputStream inputStream) throws Exception {
        //READ MESSAGE TYPE
        byte[] typeBytes = new byte[Message.TYPE_LENGTH];
        inputStream.readFully(typeBytes);

        int type = Ints.fromByteArray(typeBytes);

        //READ HAS ID
        int hasId = inputStream.read();
        int id = -1;

        byte[] idBytes;
        if (hasId == 1) {
            //READ ID
            idBytes = new byte[Message.ID_LENGTH];
            inputStream.readFully(idBytes);
            id = Ints.fromByteArray(idBytes);
        }

        //READ LENGTH
        int length = inputStream.readInt();

        //IF MESSAGE CONTAINS DATA READ DATA AND VALIDATE CHECKSUM
        byte[] data = new byte[length];
        byte[] checksum;
        if (length > 0) {
            //READ CHECKSUM
            checksum = new byte[Message.CHECKSUM_LENGTH];
            inputStream.readFully(checksum);

            //READ DATA
            inputStream.readFully(data);

            // не проверяем - подпись проверит - лишние вычисления зачем checkSun(checksum, data);
        }

        Message message = null;

        switch (type) {

            // TELEGRAM
            case Message.TELEGRAM_TYPE:

                // может быть это повтор?
                if (!sender.network.checkHandledTelegramMessages(data, sender, false)) {
                    //logger.debug(sender + " <-- Telegram REPEATED...");
                    return null;
                }

                //CREATE MESSAGE FROM DATA
                message = TelegramMessage.parse(data);
                break;

            //TRANSACTION
            case Message.TRANSACTION_TYPE:

                // может быть это повтор?
                // TODO сделаь тут проверку - если идет синхронизация и конец ее скоро (меньше 333) блока
                // то разрешить парсинг прилетающего блока, иначе не заморачиваться даже
                // так чтобы не было постоянной синхронизации форжинга
                if (!sender.network.checkHandledTransactionMessages(data, sender, false)) {
                    //logger.debug(sender + " <-- Transaction REPEATED...");
                    return null;
                }

                //CREATE MESSAGE FROM MDATA
                message = TransactionMessage.parse(data);
                break;

            //BLOCK
            case Message.WIN_BLOCK_TYPE:

                // может быть это повтор?
                if (!sender.network.checkHandledWinBlockMessages(data, sender, false)) {
                    //logger.debug(sender + " <-- Win Block REPEATED...");
                    return null;
                }

                //CREATE MESSAGE FROM DATA
                message = BlockWinMessage.parse(data);
                break;

            //TODO: delete PING and GET HWeight
            case Message.GET_PING_TYPE:

                message = new GetHWeightMessage();
                break;

            //GET PEERS
            case Message.GET_PEERS_TYPE:

                message = new GetPeersMessage();
                break;

            //PEERS
            case Message.PEERS_TYPE:

                //CREATE MESSAGE FROM DATA
                message = PeersMessage.parse(data);
                break;

            //PING and GET HWeight
            case Message.GET_HWEIGHT_TYPE:

                message = new GetHWeightMessage();
                break;

            //HEIGHT
            case Message.HWEIGHT_TYPE:

                //CREATE HEIGHT FROM DATA
                message = HWeightMessage.parse(data);
                break;

            //GETSIGNATURES
            case Message.GET_SIGNATURES_TYPE:

                //CREATE MESSAGE FROM DATA
                message = GetSignaturesMessage.parse(data);
                break;

            //SIGNATURES
            case Message.SIGNATURES_TYPE:

                //CREATE MESSAGE FROM DATA
                message = SignaturesMessage.parse(data);
                break;

            //GETBLOCK
            case Message.GET_BLOCK_TYPE:

                //CREATE MESSAGE FROM DATA
                message = GetBlockMessage.parse(data);
                break;

            //BLOCK
            case Message.BLOCK_TYPE:

                //CREATE MESSAGE FROM DATA
                message = BlockMessage.parse(data);
                break;

            //VERSION
            case Message.VERSION_TYPE:

                //CREATE MESSAGE FROM DATA
                message = VersionMessage.parse(data);
                break;

            //FIND_MYSELF
            case Message.FIND_MYSELF_TYPE:

                //CREATE MESSAGE FROM DATA
                message = FindMyselfMessage.parse(data);
                break;
            case Message.TELEGRAM_GET_TYPE:
                message = TelegramGetMessage.parse(data);
                break;

            case Message.TELEGRAM_ANSWER_TYPE:
                message = TelegramAnswerMessage.parse(data);
                break;
            default:

                //UNKNOWN MESSAGE
                LOGGER.info(Lang.T("Received unknown type message!"));
                return null; //new Message(type);

        }

        //SET SENDER
        message.setSender(sender);
        message.setLength(length);
        message.setLoadBytes(data);

        //SET ID
        if (hasId == 1) {
            message.setId(id);
        }

        //RETURN
        return message;
    }
}
