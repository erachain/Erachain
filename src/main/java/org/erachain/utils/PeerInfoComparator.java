package org.erachain.utils;

import org.erachain.database.PeerMap.PeerInfo;
import org.erachain.ntp.NTP;

import java.util.Comparator;

public class PeerInfoComparator implements Comparator<PeerInfo> {

    @Override
    public int compare(PeerInfo o1, PeerInfo o2) {

        long now = NTP.getTime();

        boolean o1findMoreWeekAgo = (now - o1.getFindingTime() > 7 * 24 * 60 * 60 * 1000);
        boolean o2findMoreWeekAgo = (now - o2.getFindingTime() > 7 * 24 * 60 * 60 * 1000);

        boolean o1neverWhite = o1.getWhitePingCouner() == 0;
        boolean o2neverWhite = o2.getWhitePingCouner() == 0;

        boolean o1badNode = (o1neverWhite && o1findMoreWeekAgo);
        boolean o2badNode = (o2neverWhite && o2findMoreWeekAgo);

        if (!o1badNode && o2badNode)
            return -1;
        else if (o1badNode && !o2badNode)
            return 1;
        else {
            if (o1.getWhiteConnectTime() < o2.getWhiteConnectTime())
                return -1;
            else if (o1.getWhiteConnectTime() > o2.getWhiteConnectTime())
                return 1;
            else {
                if (o1.getWhitePingCouner() < o2.getWhitePingCouner())
                    return -1;
                else if (o1.getWhitePingCouner() > o2.getWhitePingCouner())
                    return 1;
                else
                    return 0;
            }
        }
    }

}
