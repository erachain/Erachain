package core.wallet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import controller.Controller;
import core.account.Account;
import core.item.ItemCls;
import core.transaction.Transaction;
import database.DBSet;
import database.SortableList;
import gui.Gui;
import utils.ObserverMessage;
import utils.Pair;		


public class ItemsFavorites_not implements Observer{

	private List<Long> favorites;
	private int type;
	
	
	public ItemsFavorites_not(int type) {
		
		this.type = type;
		this.favorites = new ArrayList<Long>(); 
		
		Controller.getInstance().addWalletListener(this);
		Controller.getInstance().addObserver(this);
		///this.reload();
		//this.getAssets();
		
	}
	
	public List<Long> getKeys()
	{
		return this.favorites;
	}
	
	public List<ItemCls> getItems()
	{
		List<ItemCls> assets = new ArrayList<ItemCls>();
		for (Long key : this.favorites) {
			assets.add(Controller.getInstance().getItem(this.type, key));
		}
		return assets;
	}
	
	public void reload()
	{
		List<Long> favoritesUpadate = new ArrayList<Long>();
		
		for (Account account : Controller.getInstance().getAccounts()) {
			SortableList<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>> balancesList = DBSet.getInstance().getAssetBalanceMap().getBalancesSortableList(account);
			
			for (Pair<Tuple2<String, Long>, Tuple3<BigDecimal, BigDecimal, BigDecimal>> balance : balancesList) {
				if(balance.getB().a.compareTo(BigDecimal.ZERO) != 0
						|| balance.getB().b.compareTo(BigDecimal.ZERO) != 0
						|| balance.getB().c.compareTo(BigDecimal.ZERO) != 0
						) {
					if(!favoritesUpadate.contains(balance.getA().b)){
						favoritesUpadate.add(balance.getA().b);
					}
				}
			}
		}
		this.favorites = favoritesUpadate;

		//Controller.getInstance().replaseAssetsFavorites();
		Controller.getInstance().replaseFavoriteItems(this.type);

	}
	
	@Override
	public void update(Observable o, Object arg) {

		if(!Gui.isGuiStarted()){
			return;
		}
		
		ObserverMessage message = (ObserverMessage) arg;

		if((message.getType() == ObserverMessage.NETWORK_STATUS && (int) message.getValue() == Controller.STATUS_OK)
			||((Controller.getInstance().getStatus() == Controller.STATUS_OK) && 
					(
							message.getType() == ObserverMessage.ADD_ACCOUNT_TYPE
							||
							message.getType() == ObserverMessage.REMOVE_ACCOUNT_TYPE
							||
							message.getType() == ObserverMessage.ADD_BALANCE_TYPE
							||
							message.getType() == ObserverMessage.REMOVE_BALANCE_TYPE
					)))
		{
			this.reload();
		}
	}
}