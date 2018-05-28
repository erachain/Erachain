package webserver;

import controller.Controller;
import core.BlockGenerator;
import utils.ObserverMessage;

import java.util.Observable;
import java.util.Observer;

public class Status implements Observer {

    static BlockGenerator.ForgingStatus for_status;
    static Status status;


    public static Status getinstance() {
        if (status == null) {
            status = new Status();
            Controller.getInstance().addObserver(status);
            for_status = Controller.getInstance().getForgingStatus();

        }
        return status;

    }

    @Override
    public void update(Observable arg0, Object arg1) {
        ObserverMessage message = (ObserverMessage) arg1;

        if (message.getType() == ObserverMessage.FORGING_STATUS) {
            for_status = (BlockGenerator.ForgingStatus) message.getValue();


        }
    }

    public BlockGenerator.ForgingStatus getForgingStatus() {

        return for_status;
    }

}
