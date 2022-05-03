package com.bestfriend.constant;

public class RemittanceKey {

	/**
	 * 2019-12-12 10:14:33: You have received PHP2,000.00 <b>on your PayMaya Negosyo
	 * Account from</b> +639981888688. Also added PHP23.00 commission to your account.
	 * Your available balance is PHP2,023.00. Ref. No: 623b0122ff65
	 */
	public static final String RECEIVE_PN_1 = "on your PayMaya Negosyo Account from";

	/**
	 * 2019-12-13 12:57:55: You have transferred PHP35,000.00 <b>from your PayMaya
	 * Negosyo Account to</b> +639298549996. Also deducted PHP402.50 receiving agent
	 * commission and PHP245.00 transfer fee from your account. Your available
	 * balance is PHP29,877.50. Ref. No: 401179e89db9
	 */
	public static final String TRANSFER_PN_1 = "from your PayMaya Negosyo Account to";

	/**
	 * Approval of PHP10020.00 purchase at Retail Store            T
	 * from 609777*********4159. Available balance: PHP240420.03 with Reference Number 000000686891.
	 * <p>
	 * Find out how you can maximize your PayMaya account. Check out http://www.paymaya.com/deals
	 * *Promos are updated regularly
	 * <p>
	 * FreeInfoMsg
	 */
	public static final String TRANSFER_WD_1 = "purchase at Retail Store";

	/**
	 * You have withdrawn PHP10000.00 from 609777*********4159 at the ATM BANCNET.
	 * Available balance: PHP59204.92 with Reference Number 000000000632491.
	 */
	public static final String TRANSFER_WD_2 = "You have withdrawn";

	/**
	 * 2019-12-26 06:47:24: Awesome! You have successfully transferred PHP29,999.26 <b>to the account
	 * ending in 1423 via Instapay.</b> Your available balance is PHP222,077.47. Ref. No: 22724b0a7e24.
	 */
	public static final String TRANSFER_PN_2A = "to the account ending in";
	public static final String TRANSFER_PN_2B = "via Instapay";
	public static final String TRANSFER_PN_2C = "You have successfully transferred";

	/**
	 * Length = 15<br/>
	 * 04Oct 1626: Received P800.00 with P11.50 <b>commission from</b> 09085299588 to
	 * LOADING.LIBRE ang pag-claim! Ref:af6bddc37278 Bal:P56,186.12
	 * <br/><br/>
	 * Length = 14<br/>
	 * 18Jul 07:52: Received PHP500.00 with PHP11.50 <b>commission from</b> +639218599502.
	 * LIBRE ang pag-claim! Bal:PHP6,160.75. Ref:cffcba11e6e1
	 */
	public static final String RECEIVE_SP_1 = "commission from";

	/**
	 * 1/2 04Sep 1537:Remittance of PHP1,000.00 & commission of PHP11.50
	 * <b>was received from</b> 639085235355.LIBRE ang pag-claim ng
	 * iyong customer.Ref:8b660f5b0e76
	 */
	public static final String RECEIVE_SP_2 = "was received from";

	/**
	 * 2017-10-04 15:59:46: Received PHP600.00 from POSIBLE <b>w/commission kung ikaw
	 * ay Smart Padala Agent.LIBRE ang pag-claim ni suki!</b> Ref:96cfadd8e633 Bal: PHP42390.62. Para
	 * malaman ang iyong komisyon, i-check ang balance sa Smart Money menu.
	 */
	public static final String RECEIVE_SP_3 = "w/commission kung ikaw ay Smart Padala " +
		"Agent.LIBRE ang pag-claim ni suki!";

	/**
	 * 04Sep 1540:Remittance of PHP500.00 & commission of PHP11.50 <b>was added to your
	 * account</b>.Avail Bal:PHP709,282.13.Ref:122db819e67b
	 */
	public static final String RECEIVE_SP_4 = "was added to your account";

	/**
	 * Format here
	 */
	public static final String RECEIVE_SP_5 = "was transferred from";

	/**
	 * 31Dec 15:32:Sent P1,000.00 from LOADING to ****3100 at 09497749132. <b>Also deducted</b>
	 * P18.50 from your account.Bal:P140,115.27.Ref:1d800db4b7b1
	 */
	public static final String TRANSFER_SP_1 = "Also deducted";

	/**
	 * 14Sep 0908:Remittance of PHP4,500.00 & fee of PHP83.25 <b>was deducted from</b> your
	 * account.Avail bal:PHP300,752.88.Ref:d846534c1f26
	 */
	public static final String TRANSFER_SP_2 = "was deducted from";

	/**
	 * 1/2 14Sep 0913:Sent PHP8,300.00 from LOADING to 557751******1103 at
	 * 639107367545.Ref:0235c3340bbd.<b>Sa next msg,i-type ang customer
	 * &receiver cellphone#</b>
	 */
	public static final String TRANSFER_SP_3 = "Sa next msg,i-type ang customer " +
		"&receiver cellphone#";

	/**
	 * Length = 22<br/>
	 * 2017-11-02 14:01:52: You <b>have transferred</b> PHP500.00 from your SMARTMoney Account to
	 * PayMaya User +639306678918. Your available balance is PHP53080.00. Ref. No: aa42aee79b9e.
	 * <br/><br/>
	 * Length = 20<br/>
	 * 05Nov 1437: You <b>have transferred</b> P2,000.00 from Bayad Center-ALTERNATE REALITIES - FESTIVAL
	 * MALL with card no. 557751******4100 to 557751******3101 Ref:ffeee72bc8f6
	 */
	public static final String TRANSFER_SM = "have transferred";

	public static final String TRANSFER_PM = "to account ending in";

	public static final String BALANCE_EXT = "2/2 Bal:";
	public static final String SENDER_SP = "SmartPadala";
	public static final String SENDER_SM = "SmartMoney";
	public static final String SENDER_PM = "PayMaya";
	public static final String SENDER_PN = "PYMYNegosyo";
	public static final String SENDER_PM1 = "Maya";
	public static final String SENDER_PM2 = "MayaAgent";
	public static final String SENDER_T1 = "+639998076414";
	public static final String SENDER_T2 = "+639206087750";
	public static final String SENDER_T3 = "+639084019072";


	public static final String[] INGOING = new String[]{
		"received",
		"was added to your"
	};

	public static final String[] OUTGOING = new String[]{
		"transferred",
		"sent",
		"withdrawn",
		"was deducted from your",
		"you have paid",
		"approval of",
		"paid",
		"to account ending in",
		"to the account ending in",
		"deducted",
		"from your account"
	};

	public static final String[] KEYS_AMOUNT_AFTER = new String[]{
		"remittance of",
		"received",
		"transferred",
		"withdrawn",
		"you have paid",
		"approval of",
		"sent",
		"paid",
		"deducted"
	};

	public static final String[] KEYS_AMOUNT_BEFORE = new String[]{
		"deposit to",
		"deposited",
		"transferred",
		"received",
		"transferred",
		"withdrawn",
		"paid",
		"sent",
		"deducted"
	};

	public static final String[] KEYS_CHARGE_AFTER = new String[]{
		"commission of",
		"fee of",
		"also deducted",
		"also added",
		"with php",
		"with p"
	};

	public static final String[] KEYS_BALANCE_AFTER = new String[]{
		"bal.",
		"bal:",
		"balance",
	};

	public static final String[] KEYS_REFERENCE_AFTER = new String[]{
		"reference number",
		"reference no",
		"reference #",
		"ref. number",
		"ref. no",
		"ref. #",
		"ref number",
		"ref no",
		"ref #",
		"ref#",
		"ref:"
	};
}
