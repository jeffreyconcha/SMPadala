package com.bestfriend.constant;

public class RemittanceKey {

	/**
	 * 04Oct 1626: Received P800.00 with P11.50 commission from 09085299588 to
	 * LOADING.LIBRE ang pag-claim! Ref:af6bddc37278 Bal:P56,186.12
	 */
	public static final String RECEIVE_SP_1 = "commission from";

	/**
	 * 1/2 04Sep 1537:Remittance of PHP1,000.00 & commission of PHP11.50 was received
	 * from 639085235355.LIBRE ang pag-claim ng iyong customer.Ref:8b660f5b0e76
	 */
	public static final String RECEIVE_SP_2 = "was received from";

	/**
	 * 2017-10-04 15:59:46: Received PHP600.00 from POSIBLE w/commission kung ikaw
	 * ay Smart Padala Agent.LIBRE ang pag-claim ni suki! Ref:96cfadd8e633 Bal: PHP42390.62. Para
	 * malaman ang iyong komisyon, i-check ang balance sa Smart Money menu.
	 */
	public static final String RECEIVE_SP_3 = "w/commission kung ikaw ay Smart Padala " +
			"Agent.LIBRE ang pag-claim ni suki!";

	/**
	 * 04Sep 1540:Remittance of PHP500.00 & commission of PHP11.50 was added to your
	 * account.Avail Bal:PHP709,282.13.Ref:122db819e67b
	 */
	public static final String RECEIVE_SP_4 = "was added to your account";

	/**
	 * Format here
	 */
	public static final String RECEIVE_SP_5 = "was transferred from";

	/**
	 * 31Dec 15:32:Sent P1,000.00 from LOADING to ****3100 at 09497749132. Also deducted P18.50
	 * from your account.Bal:P140,115.27.Ref:1d800db4b7b1
	 */
	public static final String TRANSFER_SP_1 = "Also deducted";

	/**
	 * 14Sep 0908:Remittance of PHP4,500.00 & fee of PHP83.25 was deducted from your
	 * account.Avail bal:PHP300,752.88.Ref:d846534c1f26
	 */
	public static final String TRANSFER_SP_2 = "was deducted from";

	/**
	 * 1/2 14Sep 0913:Sent PHP8,300.00 from LOADING to 557751******1103 at
	 * 639107367545.Ref:0235c3340bbd.Sa next msg,i-type ang customer
	 * &receiver cellphone#
	 */
	public static final String TRANSFER_SP_3 = "Sa next msg,i-type ang customer " +
			"&receiver cellphone#";

	/**
	 * Length = 22<br/>
	 * 2017-11-02 14:01:52: You have transferred PHP500.00 from your SMARTMoney Account to
	 * PayMaya User +639306678918. Your available balance is PHP53080.00. Ref. No: aa42aee79b9e.
	 *<br/><br/>
	 * Length = 20<br/>
	 * 05Nov 1437: You have transferred P2,000.00 from Bayad Center-ALTERNATE REALITIES - FESTIVAL
	 * MALL with card no. 557751******4100 to 557751******3101 Ref:ffeee72bc8f6
	 */
	public static final String TRANSFER_SM = "have transferred";

	public static final String BALANCE = "2/2 Bal:";
	public static final String SENDER_SP = "SmartPadala";
	public static final String SENDER_SM = "SmartMoney";
	public static final String SENDER_PM = "PayMaya";
	public static final String SENDER_T1 = "+639998076414";
	public static final String SENDER_T2 = "+639206087750";
}
