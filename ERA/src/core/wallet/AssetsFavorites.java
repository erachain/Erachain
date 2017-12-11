package core.wallet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.mapdb.Fun.Tuple5;

import controller.Controller;
import core.account.Account;
import core.item.assets.AssetCls;
import core.transaction.Transaction;
import datachain.DCSet;
import datachain.SortableList;
import gui.Gui;
import utils.ObserverMessage;
import utils.Pair;		


public class AssetsFavorites implements Observer{

	private List<Long> favorites;
	
	public AssetsFavorites() {
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
	
	public List<AssetCls> getAssets()
	{
		List<AssetCls> assets = new ArrayList<AssetCls>();
		for (Long key : this.favorites) {
			assets.add(Controller.getInstance().getAsset(key));
		}
		return assets;
	}
	
	public void reload()
	{
		List<Long> favoritesUpadate = new ArrayList<Long>();
		
		for (Account account : Controller.getInstance().getAccounts()) {
			SortableList<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> balancesList = DCSet.getInstance().getAssetBalanceMap().getBalancesSortableList(account);
			
			for (Pair<Tuple2<String, Long>, Tuple5<Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> balance : balancesList) {
				if(balance.getB().a.b.compareTo(BigDecimal.ZERO) != 0
						|| balance.getB().b.b.compareTo(BigDecimal.ZERO) != 0
						|| balance.getB().c.b.compareTo(BigDecimal.ZERO) != 0
						|| balance.getB().d.b.compareTo(BigDecimal.ZERO) != 0
						|| balance.getB().e.b.compareTo(BigDecimal.ZERO) != 0) {
					if(!favoritesUpadate.contains(balance.getA().b)){
						favoritesUpadate.add(balance.getA().b);
					}
				}
			}
		}
		this.favorites = favoritesUpadate;

		//Controller.getInstance().replaseAssetsFavorites();
		//////Controller.getInstance().replaseAssetsFavorites();

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