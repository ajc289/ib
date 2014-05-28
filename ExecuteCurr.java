package Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.Semaphore;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.EClientSocket;
import com.ib.client.EWrapper;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.UnderComp;

public class ExecuteCurr implements EWrapper
{
	private EClientSocket m_client = new EClientSocket( this);
	
	Contract c = new Contract ();
	Order o = new Order ();
	
	DecimalFormat df = new DecimalFormat("#.####");
	
	static int FIRST_TRADES = 0;
	static int EUR_ONE = 1;
	static int AUD_ONE = 2;
	static int EUR_DONE = 3;
	static int AUD_DONE = 4;
	
	Semaphore mkt_order_stats_sem = new Semaphore (1);
	Semaphore stp_lmt_order_stats_sem = new Semaphore (1);
	Semaphore file_sem = new Semaphore (1);
	Semaphore order_id_sem = new Semaphore (1);
	Semaphore mkt_filled_sem = new Semaphore (0);
	
	int order_id = 51200;
	int outstanding_mkt_orders = 0;
	int mkt_order_id = -1;
	int stp_order_id_aud = -1;
	int lmt_order_id_aud = -1;
	int stp_order_id_eur = -;
	int lmt_order_id_eur = -1;
	
	double observed_low = Double.MAX_VALUE;
	
	int aud_state = FIRST_TRADES;
	int eur_state = FIRST_TRADES;
	
	double aud_gain_short = 2 - 1.00115;
	double aud_loss_short = 2 - .99275;
	double eur_gain_short = 2 - 1.00085;
	double eur_loss_short = 2 - .99575;
	double aud_gain_long = 1.000575;
	double aud_loss_long = .99275;
	double eur_gain_long = 1.000275;
	double eur_loss_long = .99575;	
	
	int desired_short = 0;
	
	int filled_stp_lmt_order_id = -1;
	double mkt_fill_price = 0.0;
	BufferedWriter bw = null;
	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	public ExecuteCurr ()
	{
		df.setRoundingMode(RoundingMode.DOWN);
	}
	
	@Override
	public void error(Exception e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void error(String str) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void error(int id, int errorCode, String errorMsg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectionClosed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickPrice(int tickerId, int field, double price,
			int canAutoExecute) 
	{
	}

	@Override
	public void tickSize(int tickerId, int field, int size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickOptionComputation(int tickerId, int field,
			double impliedVol, double delta, double optPrice,
			double pvDividend, double gamma, double vega, double theta,
			double undPrice) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickString(int tickerId, int tickType, String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickEFP(int tickerId, int tickType, double basisPoints,
			String formattedBasisPoints, double impliedFuture, int holdDays,
			String futureExpiry, double dividendImpact, double dividendsToExpiry) {
		// TODO Auto-generated method stub
		
	}
	
	public void orderStatus(int orderId, String status, int filled,
			int remaining, double avgFillPrice, int permId, int parentId,
			double lastFillPrice, int clientId, String whyHeld) 
	{
		System.out.println (status);
		try
		{
	        file_sem.acquire();
        	bw.write(sdf.format(Calendar.getInstance().getTime()) + ",trade,orderStatus " + status + ",order_id " + Integer.toString(orderId));
        	bw.newLine();
        	file_sem.release();
        	
			mkt_order_stats_sem.acquire();
			if (status.compareTo("Filled") == 0 && orderId == mkt_order_id && outstanding_mkt_orders == 1)
			{
				outstanding_mkt_orders = 0;
				mkt_fill_price = avgFillPrice;
				mkt_filled_sem.release();
			}
			mkt_order_stats_sem.release();
			
			stp_lmt_order_stats_sem.acquire();
			
			if (status.compareTo("Filled") == 0 && orderId == lmt_order_id_aud)
			{
				file_sem.acquire();
				bw.write(sdf.format(Calendar.getInstance().getTime()) + ",trade,LMT_FILLED " + Integer.toString(orderId));
	        	bw.newLine();
	        	file_sem.release();
	        	
	        	filled_stp_lmt_order_id = orderId;
	        	
        		if (aud_state == FIRST_TRADES)
        		{
        			aud_state = AUD_ONE;
					m_client.cancelOrder(stp_order_id_aud);
					c.m_symbol = "aud";
			        o.m_orderType = "STP";
			        o.m_auxPrice = Double.parseDouble(df.format(avgFillPrice*aud_loss_long));
			        o.m_orderId = order_id;
			        stp_order_id_aud = order_id;
			        o.m_action = "SELL";
			        o.m_tif = "DAY";
			        o.m_totalQuantity = desired_short;
			        m_client.placeOrder(order_id, c, o);
			        order_id++;
			        
			        o.m_orderType = "LMT";
			        o.m_lmtPrice = Double.parseDouble(df.format(avgFillPrice*aud_gain_long));
			        o.m_orderId = order_id;
			        lmt_order_id_aud = order_id;
			        o.m_action = "SELL";
			        o.m_tif = "DAY";
			        o.m_totalQuantity = desired_short;
			        m_client.placeOrder(order_id, c, o);
			        order_id++;
        		}
        		else if (aud_state == AUD_ONE)
        		{
        			m_client.cancelOrder(stp_order_id_aud);
        			aud_state = AUD_DONE;
        		}
			}
			else if (status.compareTo("Filled") == 0 && orderId == lmt_order_id_eur)
			{
				file_sem.acquire();
				bw.write(sdf.format(Calendar.getInstance().getTime()) + ",trade,LMT_FILLED " + Integer.toString(orderId));
	        	bw.newLine();
	        	file_sem.release();
	        	
	        	filled_stp_lmt_order_id = orderId;
	        	
        		if (eur_state == FIRST_TRADES)
        		{
        			eur_state = EUR_ONE;
					m_client.cancelOrder(stp_order_id_eur);
					c.m_symbol = "eur";
			        o.m_orderType = "STP";
			        o.m_auxPrice = Double.parseDouble(df.format(avgFillPrice*eur_loss_long));
			        o.m_orderId = order_id;
			        stp_order_id_eur = order_id;
			        o.m_action = "SELL";
			        o.m_tif = "DAY";
			        o.m_totalQuantity = desired_short;
			        m_client.placeOrder(order_id, c, o);
			        order_id++;
			        
			        o.m_orderType = "LMT";
			        o.m_lmtPrice = Double.parseDouble(df.format(avgFillPrice*eur_gain_long));
			        o.m_orderId = order_id;
			        lmt_order_id_eur = order_id;
			        o.m_action = "SELL";
			        o.m_tif = "DAY";
			        o.m_totalQuantity = desired_short;
			        m_client.placeOrder(order_id, c, o);
			        order_id++;
        		}
        		else if (eur_state == EUR_ONE)
        		{
        			m_client.cancelOrder(stp_order_id_eur);
        			eur_state = EUR_DONE;
        		}
			}
			else if (status.compareTo("Filled") == 0 && orderId == stp_order_id_aud)
			{
				file_sem.acquire();
				bw.write(sdf.format(Calendar.getInstance().getTime()) + ",trade,STP_FILLED " + Integer.toString(orderId));
	        	bw.newLine();
	        	file_sem.release();
	        	
	        	filled_stp_lmt_order_id = orderId;
        		
        		if (aud_state == FIRST_TRADES)
        		{
        			c.m_symbol = "aud";
        			aud_state = AUD_ONE;
					m_client.cancelOrder(lmt_order_id_aud);
					
			        o.m_orderType = "STP";
			        o.m_auxPrice = Double.parseDouble(df.format(avgFillPrice*aud_loss_long));
			        o.m_orderId = order_id;
			        stp_order_id_aud = order_id;
			        o.m_action = "SELL";
			        o.m_tif = "DAY";
			        o.m_totalQuantity = desired_short;
			        m_client.placeOrder(order_id, c, o);
			        order_id++;
			        
			        o.m_orderType = "LMT";
			        o.m_lmtPrice = Double.parseDouble(df.format(avgFillPrice*aud_gain_long));
			        o.m_orderId = order_id;
			        lmt_order_id_aud = order_id;
			        o.m_action = "SELL";
			        o.m_tif = "DAY";
			        o.m_totalQuantity = desired_short;
			        m_client.placeOrder(order_id, c, o);
			        order_id++;
        		}
        		else if (aud_state == AUD_ONE)
        		{
        			m_client.cancelOrder(lmt_order_id_aud);
        			aud_state = AUD_DONE;
        		}
			}
			else if (status.compareTo("Filled") == 0 && orderId == stp_order_id_eur)
			{
				file_sem.acquire();
				bw.write(sdf.format(Calendar.getInstance().getTime()) + ",trade,STP_FILLED " + Integer.toString(orderId));
	        	bw.newLine();
	        	file_sem.release();
	        	
	        	filled_stp_lmt_order_id = orderId;
        		
        		if (eur_state == FIRST_TRADES)
        		{
        			eur_state = EUR_ONE;
        			
        			eur_state = EUR_ONE;
					m_client.cancelOrder(lmt_order_id_eur);
					c.m_symbol = "eur";
			        o.m_orderType = "STP";
			        o.m_auxPrice = Double.parseDouble(df.format(avgFillPrice*eur_loss_long));
			        o.m_orderId = order_id;
			        stp_order_id_eur = order_id;
			        o.m_action = "SELL";
			        o.m_tif = "DAY";
			        o.m_totalQuantity = desired_short;
			        m_client.placeOrder(order_id, c, o);
			        order_id++;
			        
			        o.m_orderType = "LMT";
			        o.m_lmtPrice = Double.parseDouble(df.format(avgFillPrice*eur_gain_long));
			        o.m_orderId = order_id;
			        lmt_order_id_eur = order_id;
			        o.m_action = "SELL";
			        o.m_tif = "DAY";
			        o.m_totalQuantity = desired_short;
			        m_client.placeOrder(order_id, c, o);
			        order_id++;
        		}
        		else if (eur_state == EUR_ONE)
        		{
        			m_client.cancelOrder(lmt_order_id_eur);
        			eur_state = EUR_DONE;
        		}
			}
			
			stp_lmt_order_stats_sem.release();
		}
		catch (Exception e) {e.printStackTrace();}
	}

	@Override
	public void openOrder(int orderId, Contract contract, Order order,
			OrderState orderState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openOrderEnd() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateAccountValue(String key, String value, String currency,
			String accountName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updatePortfolio(Contract contract, int position,
			double marketPrice, double marketValue, double averageCost,
			double unrealizedPNL, double realizedPNL, String accountName) {
		// TODO Auto-generated method stub
	}

	@Override
	public void updateAccountTime(String timeStamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void accountDownloadEnd(String accountName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nextValidId(int orderId) 
	{
		try
		{
			file_sem.acquire();
			bw.write(sdf.format(Calendar.getInstance().getTime()) + ",orderid," + Integer.toString(orderId));
			System.out.println ("starting order id");
			bw.newLine();
			file_sem.release();
			
			order_id = orderId + 1;
			
			order_id_sem.release();
		}
		catch (Exception e){e.printStackTrace();}
	}

	@Override
	public void contractDetails(int reqId, ContractDetails contractDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contractDetailsEnd(int reqId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execDetails(int reqId, Contract contract, Execution execution) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execDetailsEnd(int reqId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateMktDepth(int tickerId, int position, int operation,
			int side, double price, int size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateMktDepthL2(int tickerId, int position,
			String marketMaker, int operation, int side, double price, int size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateNewsBulletin(int msgId, int msgType, String message,
			String origExchange) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void managedAccounts(String accountsList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receiveFA(int faDataType, String xml) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void historicalData(int reqId, String date, double open,
			double high, double low, double close, int volume, int count,
			double WAP, boolean hasGaps) {
	}

	@Override
	public void scannerParameters(String xml) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scannerData(int reqId, int rank,
			ContractDetails contractDetails, String distance, String benchmark,
			String projection, String legsStr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scannerDataEnd(int reqId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void realtimeBar(int reqId, long time, double open, double high,
			double low, double close, long volume, double wap, int count)
	{
		try
		{
			stp_lmt_order_stats_sem.acquire();
			if (low < observed_low)
				observed_low = low;
			stp_lmt_order_stats_sem.release();
			
			file_sem.acquire();
        	bw.write(sdf.format(Calendar.getInstance().getTime()) + ",data,potential_high " + Double.toString(high));
        	bw.newLine();
			file_sem.release();
		}
		catch (Exception e){e.printStackTrace();}
	}

	@Override
	public void currentTime(long time) {
		// TODO Auto-generated method stub
	}

	@Override
	public void fundamentalData(int reqId, String data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deltaNeutralValidation(int reqId, UnderComp underComp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tickSnapshotEnd(int reqId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void marketDataType(int reqId, int marketDataType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void commissionReport(CommissionReport commissionReport) {
		// TODO Auto-generated method stub
		
	}
	
	public void Connect ()
	{
		//m_client.eConnect("", 7496, 0);
		m_client.eConnect("", 4001, 0);
	}
	
	public void Disconnect ()
	{
		m_client.eDisconnect();
	}
	
	public void Process ()
	{
		desired_short = 300000;
		
		if (desired_short % 2 == 1)
			desired_short++;
		
		double open_price = 0.0;
		
		try
		{
			Connect ();
        	bw = new BufferedWriter (new FileWriter ("/home/ubuntu/log_cur.csv"));
			//bw = new BufferedWriter (new FileWriter ("C:\\Users\\alan.calabrese\\Desktop\\svxy\\log2.csv"));
        	bw.write("time,category,action");
        	bw.newLine();

        	while (1 == 1)
        	{
	    		//Thread.sleep(30000);
        		Thread.sleep(1000);
	    		
	    		String[] time = sdf.format(Calendar.getInstance().getTime()).split(":");
	    		
	    		if (Integer.parseInt(time[0]) >= 17 && Integer.parseInt(time[1]) >= 16)
	    			break;
        	}
        	
	        c.m_conId = 0;
	        c.m_symbol = "aud";
	        c.m_secType = "cash";
	        c.m_strike = 0.0;
	        c.m_exchange = "IDEALPRO";
	        c.m_currency = "USD";
	        c.m_primaryExch = "";
	        
	        o.m_clientId = 0;
	        o.m_permId = 0;
	        o.m_totalQuantity = desired_short;
	        
	        order_id++;
	        
	        o.m_orderType = "MKT";
	        o.m_action = "SELL";
	        o.m_orderId = order_id;
	        o.m_tif = "DAY";
	        mkt_order_id = order_id;
	        outstanding_mkt_orders = 1;
	        m_client.placeOrder(order_id, c, o);
	        order_id++;

	        file_sem.acquire();
        	bw.write(sdf.format(Calendar.getInstance().getTime()) + " submitted opg order " + Integer.toString(o.m_totalQuantity));
        	bw.newLine();
        	System.out.println(sdf.format(Calendar.getInstance().getTime()) + " submitted opg order " + Integer.toString(o.m_totalQuantity));
        	file_sem.release();
	        
        	System.out.println ("before acquire");
	        mkt_filled_sem.acquire();
	        System.out.println ("after acquire");
	        
	        open_price = mkt_fill_price;
	        
	        System.out.println (mkt_fill_price);
        	
	        System.out.println ("before acquire");
        	stp_lmt_order_stats_sem.acquire();
        	System.out.println ("after acquire");

	        o.m_orderType = "STP";
	        o.m_auxPrice = Double.parseDouble(df.format(open_price*aud_loss_short));
	        o.m_orderId = order_id;
	        stp_order_id_aud = order_id;
	        o.m_action = "BUY";
	        o.m_tif = "DAY";
	        o.m_totalQuantity = desired_short*2;
	        m_client.placeOrder(order_id, c, o);
	        order_id++;
	        
	        o.m_orderType = "LMT";
	        o.m_lmtPrice = Double.parseDouble(df.format(open_price*aud_gain_short));
	        o.m_orderId = order_id;
	        lmt_order_id_aud = order_id;
	        o.m_action = "BUY";
	        o.m_tif = "DAY";
	        o.m_totalQuantity = desired_short*2;
	        m_client.placeOrder(order_id, c, o);
	        order_id++;
	        stp_lmt_order_stats_sem.release();
	        
	        c.m_symbol = "eur";
	        c.m_secType = "cash";
	        c.m_strike = 0.0;
	        c.m_exchange = "IDEALPRO";
	        c.m_currency = "USD";
	        c.m_primaryExch = "";
	        
	        o.m_clientId = 0;
	        o.m_permId = 0;
	        o.m_totalQuantity = desired_short;
	        
	        order_id++;
	        
	        o.m_orderType = "MKT";
	        o.m_action = "SELL";
	        o.m_orderId = order_id;
	        o.m_tif = "DAY";
	        mkt_order_id = order_id;
	        outstanding_mkt_orders = 1;
	        m_client.placeOrder(order_id, c, o);
	        order_id++;

	        file_sem.acquire();
        	bw.write(sdf.format(Calendar.getInstance().getTime()) + " submitted opg order " + Integer.toString(o.m_totalQuantity));
        	bw.newLine();
        	System.out.println(sdf.format(Calendar.getInstance().getTime()) + " submitted opg order " + Integer.toString(o.m_totalQuantity));
        	file_sem.release();
	        
        	System.out.println ("before acquire");
	        mkt_filled_sem.acquire();
	        System.out.println ("after acquire");
	        
	        open_price = mkt_fill_price;
	        
	        System.out.println (mkt_fill_price);
        	
	        System.out.println ("before acquire");
        	stp_lmt_order_stats_sem.acquire();
        	System.out.println ("after acquire");

	        o.m_orderType = "STP";
	        o.m_auxPrice = Double.parseDouble(df.format(open_price*eur_loss_short));
	        o.m_orderId = order_id;
	        stp_order_id_eur = order_id;
	        o.m_action = "BUY";
	        o.m_tif = "DAY";
	        o.m_totalQuantity = desired_short*2;
	        m_client.placeOrder(order_id, c, o);
	        order_id++;
	        
	        o.m_orderType = "LMT";
	        o.m_lmtPrice = Double.parseDouble(df.format(open_price*eur_gain_short));
	        o.m_orderId = order_id;
	        lmt_order_id_eur = order_id;
	        o.m_action = "BUY";
	        o.m_tif = "DAY";
	        o.m_totalQuantity = desired_short*2;
	        m_client.placeOrder(order_id, c, o);
	        order_id++;
	        
	        stp_lmt_order_stats_sem.release();
	        
	        while (1 == 1)
	        {
	        	Thread.sleep (30000);
	        	
				stp_lmt_order_stats_sem.acquire();
				stp_lmt_order_stats_sem.release();
				if (eur_state == EUR_DONE && aud_state == AUD_DONE)
					break;
	        }
	        
	        Disconnect();
		}
		catch (Exception e){e.printStackTrace();}
	}
	
	public static void main (String[] args)
	{
		ExecuteCurr e = new ExecuteCurr ();
		
		e.Process();
	}
}
