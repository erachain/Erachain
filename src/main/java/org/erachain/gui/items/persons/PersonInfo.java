package org.erachain.gui.items.persons;

import org.erachain.core.item.persons.PersonCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemStatusMap;
import org.erachain.lang.Lang;
import org.erachain.ntp.NTP;
import org.mapdb.Fun.Tuple5;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

// Info for person
public class PersonInfo extends JTextPane {

    //private static final long serialVersionUID = 4763074704570450206L;
    private static final long serialVersionUID = 2717571093561259483L;


    public PersonInfo() {

        this.setContentType("text/html");
        //	this.setBackground(MainFrame.getFrames()[0].getBackground());

    }


    static String Get_HTML_Person_Info_001(PersonCls person) {

        String message = "";
        String date_birthday;
        SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy"); // HH:mm");

        if (person == null) return "Empty Person";

        if (!person.isConfirmed()) {
            message = Lang.getInstance().translate("Not confirmed");
        } else {
            message = "" + person.getKey();
        }

        message = "<b>" + message + "</b> : " + person.viewName();
        //date_birthday =  formatDate.format(new Date(Long.valueOf(person.getBirthday())));
        date_birthday = person.getBirthdayStr();
        message += " (" + date_birthday;
        if (!person.isAlive(0l)) //NTP.getTime()))
            message += " - " + person.getDeathdayStr();
        message += ")";
        message = "<div>" + message + "</div>";

		/*
		// GET CERTIFIED ACCOUNTS
		TreeMap<String, java.util.Stack<Tuple3<Integer, Integer, Integer>>> addresses= DLSet.getInstance().getPersonAddressMap().getItems(person.getKey());
		if ( addresses.isEmpty()){
			message += "<p>" +  Lang.getInstance().translate("Not personalized")+ "</p";
		} else {
			message += "<h3>"+ Lang.getInstance().translate("Personalized Accounts") +"</h3>";
			// for each account seek active date
			String active_date_str;
			for( Map.Entry<String, java.util.Stack<Tuple3<Integer, Integer, Integer>>> entry : addresses.entrySet())
			{
				if (entry == null) continue;
				
				java.util.Stack<Tuple3<Integer, Integer, Integer>> stack = entry.getValue(); 
				if ( stack == null || stack.isEmpty() ) 
					active_date_str = "???";
				else {					
					Tuple3<Integer, Integer, Integer> active_date = stack.peek();
					if (active_date.a == 0) active_date_str = "active";
					else active_date_str = formatDate.format( new Date(active_date.a * (long)86400000));
				}
				
				message += "<div><input  style='background: #00ffff'; type='text' size='33' value='"+ entry.getKey() +"' disabled='disabled' class='disabled' onchange =''>"
						+ " -> <b>" + active_date_str +"</b></div>";
			}
		}
		*/

        /// STATUSES
        TreeMap<Long, Stack<Tuple5<Long, Long, byte[], Integer, Integer>>> statuses = DCSet.getInstance().getPersonStatusMap().get(person.getKey());
        if (statuses.isEmpty()) {
            message += "<div>" + Lang.getInstance().translate("Not statuses") + "</div";
        } else {
            //message += "<h3>"+ "Statuses" +"</h3>";
            String from_date_str;
            String to_date_str;
            Long dte;
            ItemStatusMap statusesMap = DCSet.getInstance().getItemStatusMap();
            for (Map.Entry<Long, java.util.Stack<Tuple5<Long, Long, byte[], Integer, Integer>>> status : statuses.entrySet()) {
                if (status == null) continue;

                Stack<Tuple5<Long, Long, byte[], Integer, Integer>> stack = status.getValue();
                if (stack == null || stack.isEmpty()) {
                    continue;
                }

                Tuple5<Long, Long, byte[], Integer, Integer> dates = stack.peek();

                message += "<div>" + statusesMap.get(status.getKey()).toString(DCSet.getInstance(), dates.c) + " : ";

                dte = dates.a;
                if (dte == null || dte == Long.MIN_VALUE) from_date_str = " ? ";
                else from_date_str = formatDate.format(new Date(dte));

                dte = dates.b;
                if (dte == null || dte == Long.MAX_VALUE) to_date_str = " ? ";
                else to_date_str = formatDate.format(new Date(dte));

                message += from_date_str + " - " + to_date_str + "</div";

            }

        }

        //message += "<div><font size='2'>" + Base58.encode(person.getReference()).substring(0, 25) + "..</font></div>";
        //message += "<div>" + Base58.encode(person.getReference()).substring(0, 25) + "..</div>";

        return message;
    }

    public void show_001(PersonCls person) {

        setText("<html><head><style> body{ font-family:"
                + UIManager.getFont("Label.font").getFamily() + "; font-size:" + UIManager.getFont("Label.font").getSize() + "px}"
                + "</style> </head><body>" + Get_HTML_Person_Info_001(person) + "</body></html>");
        return;
    }

}
