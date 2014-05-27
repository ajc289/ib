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

public class Execute implements EWrapper
{
	private EClientSocket m_client = new EClientSocket( this);
	
	Contract c = new Contract ();
	Order o = new Order ();
	
	DecimalFormat df = new DecimalFormat("#.##");
	
	static int FIRST_TRADES = 0;
	static int SECOND_TRADES = 1;
	static int THIRD_TRADES = 2;
	static int RUN_LOW = 3;
	static int RUN_HIGH = 4;
	
	Semaphore mkt_order_stats_sem = new Semaphore (1);
	Semaphore stp_lmt_order_stats_sem = new Semaphore (1);
	Semaphore file_sem = new Semaphore (1);
	Semaphore order_id_sem = new Semaphore (1);
	Semaphore mkt_filled_sem = new Semaphore (0);
	
	int order_id = 51200;
	int outstanding_mkt_orders = 0;
	int mkt_order_id = -327;
	int stp_order_id = -11212;
	int lmt_order_id = -7438743;
	
	double observed_low = Double.MAX_VALUE;
	
	int state = FIRST_TRADES;
	int strategy = RUN_LOW;
	
	int desired_short = 0;
	
	int filled_stp_lmt_order_id = -89;
	double mkt_fill_price = 0.0;
	BufferedWriter bw = null;
	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	public Execute ()
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
			
			if (status.compareTo("Filled") == 0 && orderId == lmt_order_id)
			{
				file_sem.acquire();
				bw.write(sdf.format(Calendar.getInstance().getTime()) + ",trade,LMT_FILLED " + Integer.toString(orderId));
	        	bw.newLine();
	        	file_sem.release();
	        	
	        	filled_stp_lmt_order_id = orderId;
	        	
        		if (state == FIRST_TRADES)
        			state = SECOND_TRADES;
        		else if (state == SECOND_TRADES)
        			state = THIRD_TRADES;
			}
			else if (status.compareTo("Filled") == 0 && orderId == stp_order_id)
			{
				file_sem.acquire();
				bw.write(sdf.format(Calendar.getInstance().getTime()) + ",trade,STP_FILLED " + Integer.toString(orderId));
	        	bw.newLine();
	        	file_sem.release();
	        	
	        	filled_stp_lmt_order_id = orderId;
        		
        		if (state == FIRST_TRADES)
        			state = SECOND_TRADES;
        		else if (state == SECOND_TRADES)
        			state = THIRD_TRADES;
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
		double[] raw_vol = {1019500, 843700, 761000, 1135300, 980200};
		double[] raw_vol_diff = {raw_vol[0] - raw_vol[1], raw_vol[1] - raw_vol[2], raw_vol[2] - raw_vol[3], raw_vol[3] - raw_vol[4]};
		int cur_long = 0;
		desired_short = 1920;
		
		double open_price = 0.0;
		
		double cash_out = .9805;
		
		double high_gain = .98;
		double high_hit_one_cutoff = .9925;
		double high_get_out_early = 1.0070;
		double high_loss = 1.017;
		
		double low_gain = .9925;
		double low_hit_one_cutoff = .995;
		double low_get_out_early = 1.0045;
		double low_loss = 1.0120;
		
		double used_gain = 0;
		double used_hit_one_cutoff = 0;
		double used_get_out_early = 0;
		double used_loss = 0;
		
		boolean seen_second = false;
		
		if (raw_vol_diff[0] < ((raw_vol_diff[1] + raw_vol_diff[2] + raw_vol_diff[3]) / 3))
			state = RUN_LOW;
		else
			state = RUN_HIGH;
		
		if (state == RUN_LOW)
		{
			used_gain = low_gain;
			used_hit_one_cutoff = low_hit_one_cutoff;
			used_get_out_early = low_get_out_early;
			used_loss = low_loss;
		}
		else
		{
			used_gain = high_gain;
			used_hit_one_cutoff = high_hit_one_cutoff;
			used_get_out_early = high_get_out_early;
			used_loss = high_loss;
		}
		
		try
		{
			Connect ();
			boolean tripped_hit_one_cutoff_used = false;
			boolean tripped_hit_one_cutoff_high = false;
        	bw = new BufferedWriter (new FileWriter ("/home/ubuntu/log_svxy.csv"));
        	bw.write("time,category,action");
        	bw.newLine();

        	while (1 == 1)
        	{
	    		Thread.sleep(30000);
	    		
	    		String[] time = sdf.format(Calendar.getInstance().getTime()).split(":");
	    		
	    		if (Integer.parseInt(time[0]) >= 9 && Integer.parseInt(time[1]) >= 10)
	    			break;
        	}
        	
	        c.m_conId = 0;
	        c.m_symbol = "SVXY";
	        c.m_secType = "STK";
	        c.m_strike = 0.0;
	        c.m_exchange = "SMART";
	        c.m_currency = "USD";
	        c.m_primaryExch = "ISLAND";
	        
	        o.m_clientId = 0;
	        o.m_permId = 0;
	        o.m_totalQuantity = cur_long + desired_short;
	        
	        order_id++;
	        
	        o.m_orderType = "MKT";
	        o.m_action = "SELL";
	        o.m_orderId = order_id;
	        o.m_tif = "OPG";
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
	        o.m_auxPrice = Double.parseDouble(df.format(open_price*used_loss));
	        o.m_orderId = order_id;
	        stp_order_id = order_id;
	        o.m_action = "BUY";
	        o.m_tif = "DAY";
	        o.m_totalQuantity = desired_short*2 + 100;
	        m_client.placeOrder(order_id, c, o);
	        order_id++;
	        
	        file_sem.acquire();
        	bw.write(sdf.format(Calendar.getInstance().getTime()) + " submitted loss stop " + Double.toString(o.m_auxPrice));
        	bw.newLine();
        	System.out.println(sdf.format(Calendar.getInstance().getTime()) + " submitted loss stop " + Double.toString(o.m_auxPrice));
        	file_sem.release();
        	
	        o.m_orderType = "LMT";
	        o.m_lmtPrice = Double.parseDouble(df.format(open_price*used_gain));
	        o.m_orderId = order_id;
	        lmt_order_id = order_id;
	        o.m_action = "BUY";
	        o.m_tif = "DAY";
	        o.m_totalQuantity = desired_short*2 + 100;
	        m_client.placeOrder(order_id, c, o);
	        order_id++;
	        
	        file_sem.acquire();
        	bw.write(sdf.format(Calendar.getInstance().getTime()) + " submitted gain limit " + Double.toString(o.m_lmtPrice));
        	System.out.println(sdf.format(Calendar.getInstance().getTime()) + " submitted gain limit " + Double.toString(o.m_lmtPrice));
        	bw.newLine();
        	file_sem.release();
        	
	        m_client.reqRealTimeBars(order_id, c, 5, "TRADES", false);
	        order_id++;
	        
	        stp_lmt_order_stats_sem.release();
	        
	        while (1 == 1)
	        {
	        	Thread.sleep (1000);
	        	
				stp_lmt_order_stats_sem.acquire();
				
	    		String[] time = sdf.format(Calendar.getInstance().getTime()).split(":");
	    		
	    		if (Integer.parseInt(time[0]) >= 13 && Integer.parseInt(time[1]) >= 0)
	    		{
	    			m_client.cancelOrder(lmt_order_id);
	    			m_client.cancelOrder(stp_order_id);
	    			
	    			if (state == FIRST_TRADES)
	    				o.m_action = "BUY";
	    			else if (state == SECOND_TRADES)
	    				o.m_action = "SELL";	    			
	    			
	    			o.m_totalQuantity = desired_short;
	    	        o.m_orderType = "MKT";
	    	        o.m_orderId = order_id;
	    	        o.m_tif = "DAY";
	    	        mkt_order_id = order_id;
	    	        outstanding_mkt_orders = 1;
	    	        m_client.placeOrder(order_id, c, o);
	    	        order_id++;
	    	        
	    	        bw.close();
	    	        break;
	    		}
	    		else if (state == SECOND_TRADES && !seen_second)
				{
					seen_second = true;
					if (filled_stp_lmt_order_id == stp_order_id)
					{
						m_client.cancelOrder(lmt_order_id);
						
						if (tripped_hit_one_cutoff_used)
							o.m_auxPrice = Double.parseDouble(df.format(open_price*used_get_out_early * cash_out));
						else
							o.m_auxPrice = Double.parseDouble(df.format(open_price * used_loss * cash_out));
					}
					else if (filled_stp_lmt_order_id == lmt_order_id)
					{
						m_client.cancelOrder(stp_order_id);
						o.m_auxPrice = Double.parseDouble(df.format(open_price*used_gain * cash_out));
					}
					
					o.m_totalQuantity = desired_short;
			        o.m_orderType = "STP";
			        o.m_orderId = order_id;
			        o.m_action = "SELL";
			        o.m_tif = "DAY";			    
			        m_client.placeOrder(order_id, c, o);
			        order_id++;
			        
			        file_sem.acquire();
		        	bw.write(sdf.format(Calendar.getInstance().getTime()) + " set final stop at " + Double.toString(o.m_auxPrice));
		        	System.out.println(sdf.format(Calendar.getInstance().getTime()) + " set final stop at " + Double.toString(o.m_auxPrice));
		        	bw.newLine();
		        	file_sem.release();
				}
				else if (state == THIRD_TRADES)
				{
					bw.close();
					break;
				}
				else if (!tripped_hit_one_cutoff_used && observed_low < used_hit_one_cutoff * open_price && !seen_second)
				{
					m_client.cancelOrder(stp_order_id);
					
			        o.m_orderType = "STP";
			        o.m_auxPrice = Double.parseDouble(df.format(open_price*used_get_out_early));
			        o.m_orderId = order_id;
			        stp_order_id = order_id;
			        o.m_action = "BUY";
			        o.m_tif = "DAY";
			        o.m_totalQuantity = desired_short*2;
			        m_client.placeOrder(order_id, c, o);
			        order_id++;
			        
			        file_sem.acquire();
		        	bw.write(sdf.format(Calendar.getInstance().getTime()) + " set get out early stop at " + Double.toString(o.m_auxPrice));
		        	System.out.println(sdf.format(Calendar.getInstance().getTime()) + " set get out early stop at " + Double.toString(o.m_auxPrice));
		        	bw.newLine();
		        	file_sem.release();
			        
			        tripped_hit_one_cutoff_used = true;
				}
				
		        file_sem.acquire();
	        	bw.write(sdf.format(Calendar.getInstance().getTime()) + " used " + Double.toString(observed_low) + " " + Boolean.toString(tripped_hit_one_cutoff_high) + " " + Double.toString(high_hit_one_cutoff * open_price));
	        	bw.newLine();
	        	file_sem.release();
				stp_lmt_order_stats_sem.release();
	        }
	        
	        stp_lmt_order_stats_sem.release();
	        
	        Disconnect();
		}
		catch (Exception e){e.printStackTrace();}
	}
	
	public static void main (String[] args)
	{
		Execute e = new Execute ();
		
		e.Process();
	}
}
