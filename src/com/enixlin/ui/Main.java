
package com.enixlin.ui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;

import com.enixlin.utils.ExcelToolService;
import com.enixlin.utils.NetService;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JScrollPane;
import javax.swing.DropMode;

public class Main {

	private JFrame frame;
	private JTextField clientName;
	private JTextField verifyCode;
	private JLabel image_verify;
	private JPasswordField pwd;
	private JTextField structCode;
	private CloseableHttpClient httpclient;
	private NetService ns;
	private JTextArea textArea;
	private JButton fetch;
	private String LTPAToken;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		this.ns = new NetService();
		initialize();
		this.refreshVerifyImage();

		// ArrayList<String> url_list=getClientDetailLinks();
		// String table=getClientInformationTable(url_list.get(100));
		System.out.println("");
	}

	/**
	 * 处理用户登录asone系统
	 * 
	 * @param structCode
	 *            机构码
	 * @param name
	 *            用户 名
	 * @param password
	 *            用户密码
	 * @param verifyCode
	 *            验证码
	 */
	public void login(String structCode, String name, String password, String verifyCode) {

		// 请求的网址
		String requestUrl = "";
		// 请求参数
		Map<String, String> postParams = new LinkedHashMap<>();
		// 请求返回结果
		String result = "";

		// 客户名单分页的总页数
		int maxPage = 0;

		// 对密码进行MD5加密,使用jdk里的scriptEngine类，运行md5.js里的hex_md5函数
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("javascript");
		// 读取js
		String jsName = "./libs/md5.js";
		FileReader fileReader;
		String encrytPassword = "";
		try {
			fileReader = new FileReader(jsName);
			// 执行指定脚本
			engine.eval(fileReader);
			// 执行脚本里的加密函数
			encrytPassword = (String) ((Invocable) engine).invokeFunction("hex_md5", password); // 执行

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/**
		 * 审核输入的验证码
		 * 
		 */
		requestUrl = "http://asone.safe/asone/jsp/checkCode.jsp";
		postParams.clear();
		postParams.put("loginType", "");
		postParams.put("user_radio", "2");
		postParams.put("callbackUrl", "");
		postParams.put("orgCode", structCode);
		postParams.put("userCode", name);
		postParams.put("pwd", encrytPassword);
		postParams.put("pwd_f", "");
		postParams.put("check", verifyCode);
		result = ns.HttpPost(requestUrl, postParams, "utf-8");
		// System.out.println("checkCode return result is:" + result);

		// 从返回的请求响应html中取得加密的验证码
		int start = result.indexOf("name=\"safeValidateCode\" value=\"") + 31;
		String encrytVerifyCode = result.substring(start, start + 32);
		// System.out.println(encrytVerifyCode);

		/*
		 * 用户登录 请求地址：http://zwfw.safe.gov.cn/asone/servlet/UniLoginServlet 登录使用POST方式
		 * ，请求参数如下： orgCode: 075093053 userCode: billluo1
		 * pwd:3e6c7d141e32189c917761138b026b74 backUrl: null enterUrl: null
		 * safeValidateCode: 340ed3cb761fd8ca6146544e8bff0302 user_radio: 2 loginType:
		 * biztype: null
		 */
		requestUrl = "http://asone.safe/asone/servlet/UniLoginServlet";
		postParams.clear();
		postParams.put("orgCode", structCode);
		postParams.put("userCode", name);
		postParams.put("pwd", encrytPassword);
		postParams.put("backUrl", "null");
		postParams.put("enterUrl", "null");
		postParams.put("safeValidateCode", encrytVerifyCode);
		postParams.put("user_radio", "2");
		postParams.put("loginType", "");
		postParams.put("biztype", "null");
		result = ns.HttpPost(requestUrl, postParams, "utf-8");
		Header[] headers = ns.getHeaders();
		String str = headers[0].toString();
		LTPAToken = str.substring(22, str.length());
		// System.out.println(LTPAToken);

		fetch.setEnabled(true);
		
		textArea.append("。。。");
		textArea.append("用户登录成功。。。");
		textArea.append("。。。");

	}

	/**
	 * 取得整个单位基本情况表的html代码
	 * 请求单位基本情况表时，系统会做两次自动中转，因此要取得两次的location 然后做两次get请求
	 * （这里有一个注意点，请求具体客户的单位情况表时，有自动验证）
	 * 
	 * @author linzhenhuan </br>
	 *         方法说明： </br>
	 * @param url
	 * @return String 创建时间：2020年7月9日
	 */
	public String getClientInformationTable(String url) {
		String html = ns.HttpGet(url);
		CloseableHttpResponse response1 = ns.getResponse();

		int responseCode1 = response1.getStatusLine().getStatusCode();
		Header locationHeader1 = response1.getFirstHeader("Location");
		String location1 = locationHeader1.getValue();

		html = ns.HttpGet(location1);
		CloseableHttpResponse response2 = ns.getResponse();

		int responseCode2 = response2.getStatusLine().getStatusCode();
		Header locationHeader2 = response2.getFirstHeader("Location");
		String location2 = locationHeader2.getValue();

		html = ns.HttpGet(location2);
//		System.out.println("html  " + html);
		return html;
	}

	public int getMaxPage() {
		// 请求最后一页,先取得请求的链接,然后从返回的html中提取最后一页的页码
		int maxPage = 0;
		String result = getNPage("0");
		String regex_lastPageAction = "<li class=\"firstpage\"><input type=\"hidden\" name=\"curPageNum\" value=\"\">第.*页</li>";
		Pattern p_page = Pattern.compile(regex_lastPageAction);
		Matcher m = p_page.matcher(result);
		while (m.find()) {

			String pageNum = m.group().substring(m.group().indexOf("第") + 1, m.group().length() - 6);
			maxPage = Integer.parseInt(pageNum.trim());
		}
		return maxPage;
	}

	// 取得第N页的客户清单
	public String getNPage(String page_n) {
		String html = "";
		// 请求
		String requestUrl = "http://asone.safe/BizforBankWeb/servlet/customerSearch";
		// 请求参数
		Map<String, String> postParams = new LinkedHashMap<>();
		postParams.put("pageSize", "60000");
		postParams.put("expPgNo", "0");
		postParams.put("expCount", "0");
		postParams.put("customerCode", "");
		postParams.put("customerName", "");
		postParams.put("dominationType", "1");
		postParams.put("recsts", "9");
		postParams.put("curPageNum", page_n);
		html = ns.HttpPost(requestUrl, postParams, "utf-8");
		if (page_n == "0") {
			System.out.println("取得列表的最后一页");
		} else {
			System.out.println("取得列表的第" + page_n + "页");
		}
		return html;
	}

	/**
	 * 从html代码中提取客户单位基本情况表的各栏位信息，转换成LinkedHashMap
	 * 
	 * @author linzhenhuan </br>
	 *         方法说明： </br>
	 * @param html
	 * @return LinkedHashMap<String,String> 创建时间：2020年7月9日
	 */
	public LinkedHashMap<String, String> getClientTableColumnDetail(String html) {
		LinkedHashMap<String, String> table_cols = new LinkedHashMap<>();
		ArrayList<String> rows = new ArrayList<>();
		// 正则匹配所有文体框的栏位信息
		String regex_input = "<input name=\".*/>";
		Pattern p = Pattern.compile(regex_input);
		Matcher m = p.matcher(html);
		while (m.find()) {
			String line=m.group();
			String [] attrs=line.split(" ");
			String att_name="";
			String att_value="";
		
			for(String attr : attrs) {
				if(attr.matches("fieldTitle=.*")) {
					//将每一行的name属性提取出来
					 att_name=attr.substring(attr.indexOf("fieldTitle=")+12,attr.length()-1);
				}else if(attr.matches("name=.*")) {
					att_name=attr.substring(attr.indexOf("name=")+6,attr.length()-1);
				}
				if(attr.matches("value=.*")) {
					//将每一行的name属性提取出来,要注意可能有两个value属性，其中前一个可能有值，如何解决？？？？？ 
					 if(attr.substring(attr.indexOf("value=")+7,attr.length()-1).length()!=0) {
						 att_value=attr.substring(attr.indexOf("value=")+7,attr.length()-1);
					 }
					
				}
			}
			table_cols.put(att_name, att_value);
		}
		// 正则匹配所有单选框的栏位信息
		String regex_radio = "<input type=\"radio\".*/>";
		Pattern p_radio = Pattern.compile(regex_radio);
		Matcher m_radio = p_radio.matcher(html);
		while (m_radio.find()) {
			String line=m_radio.group();
			//如果当前行包括有checked　属性
			if(line.indexOf("checked")!=-1) {
				String [] attrs=line.split(" ");
				String att_name="";
				String att_value="";
				
				for(String attr : attrs) {					
					if(attr.matches("name=.*")) {
						//将每一行的name属性提取出来
						 att_name=attr.substring(attr.indexOf("name=")+6,attr.length()-1);
					}
					if(attr.matches("value=.*")) {
						//将每一行的name属性提取出来
						 att_value=attr.substring(attr.indexOf("value=")+7,attr.length()-1);
					}
				}
				table_cols.put(att_name, att_value);
			}
			
		}
		
		
		
		//取得联系统人，电话，
		String regex_contract = "<td><div align=\"center\">.*</td>";
		Pattern p_contract = Pattern.compile(regex_contract);
		Matcher m_contract = p_contract.matcher(html);
		int line_number=0;
		while(m_contract.find()) {
			
			String line=m_contract.group();
			if(line_number==1) {
				String att_name="bankcode";
				String att_value=line.replace("<td><div align=\"center\">", "").replace("</div></td>", "");
				table_cols.put(att_name, att_value);
				
			}
			if(line_number==2) {
				String att_name="contractor";
				String att_value=line.replace("<td><div align=\"center\">", "").replace("</div></td>", "");
				table_cols.put(att_name, att_value);
				
			}
			if(line_number==3) {
				String att_name="companyTelephone";
				String att_value=line.replace("<td><div align=\"center\">", "").replace("</div></td>", "");
				table_cols.put(att_name, att_value);
				
			}
			if(line_number==4) {
				String att_name="companyFax";
				String att_value=line.replace("<td><div align=\"center\">", "").replace("</div></td>", "");
				table_cols.put(att_name, att_value);
			}
			if(line_number==5) {
				String att_name="modifyDate";
				String att_value=line.replace("<td><div align=\"center\">", "").replace("</div></td>", "");
				table_cols.put(att_name, att_value);
			}
			line_number++;
		}
		
		
		return table_cols;

	}

	// 从html代码中提取单个客户的单位基本情况表的链接
	public ArrayList<String> getClientDetailLinks() {
		ArrayList<String> links = new ArrayList<>();
		String requestUrl = "http://asone.safe/asone/servlet/SSOServer?falseflag=ClientNotLogin&userCode=&orgCode=&orgType=&password=&task=doauthenticate&callbackUrl=http%3A%2F%2Fasone.safe%3A80%2FBizforBankWeb%2Fservlet%2FcustomerSearch%3Fcurrent_appCode%3DBZBO%26asone_addr%3Dasone.safe%253A80%26userType%3D";
		String result = ns.HttpGet(requestUrl);
		CloseableHttpResponse response = ns.getResponse();
		int responseCode = response.getStatusLine().getStatusCode();
		Header locationHeader = response.getFirstHeader("Location");
		String location = locationHeader.getValue();
		// System.out.println(result);

		String regex = "&LTPAToken=.*";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(location);
		while (m.find()) {
			LTPAToken = m.group().substring(11, m.group().length());
		}

		// 请求单位基本情况表客户列表
		requestUrl = "http://asone.safe/BizforBankWeb/servlet/customerSearch?current_appCode=BZBO&asone_addr=asone.safe%3A80&userType=&login_result_sign=login&LTPAToken="
				+ LTPAToken;
		result = ns.HttpGet(requestUrl);

		//
		int maxPage = getMaxPage();
//		int maxPage = 1;
		textArea.append( "。。。。\n");
		textArea.append( "。。。。\n");
		textArea.append("一共要收集" + maxPage + "批的客户\n");
		textArea.append( "。。。。\n");
		textArea.append( "。。。。\n");
	
		for (int n = 1; n <= maxPage; n++) {
			textArea.append("正在收集第" + n + "批的客户\n");
			String html = getNPage(Integer.toString(n));
			Pattern p1 = Pattern.compile("<a href=\".*\">");
			Matcher m1 = p1.matcher(html);
			int count = 0;
			while (m1.find()) {
				count++;
				String url = "http://asone.safe" + m1.group().substring(9, m1.group().length() - 2);
				// System.out.println(url);
				if (url.equals("http://asone.safe#") || count == 11) {
					// System.out.println("不可用链接");
					// System.out.println(url);
				} else {
					links.add(url);
				}
			}

		}
		return links;
	}

	/**
	 * 更新验证码
	 */
	public void refreshVerifyImage() {
		// 先请求登录页面，里面有一个IMage 的src
		String html = this.ns.HttpGet("http://asone.safe/asone/");

		int start = html.indexOf("/asone/jsp/code.jsp") + 28;
		String refreshCode = html.substring(start, start + 13);
		String url = "http://asone.safe/asone/jsp/code.jsp?refresh=" + refreshCode;
		this.ns.downloadPicture(url, "./", "verifyCode.png");
		Icon icon = null;
		try {
			icon = new ImageIcon(ImageIO.read(new File("verifyCode.png")));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		image_verify.setIcon(icon);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frame = new JFrame();
		frame.setFont(new Font("Arial", Font.PLAIN, 12));
		frame.setTitle("\u5355\u4F4D\u57FA\u672C\u60C5\u51B5\u8868\u4E0B\u8F7D\u5668");
		frame.setBounds(100, 100, 771, 518);
		frame.getContentPane().setLayout(null);

		JButton btnNewButton = new JButton("\u767B\u5F55");
		btnNewButton.addActionListener(new ActionListener() {
			// 登录
			public void actionPerformed(ActionEvent e) {
				// 取得各个输入框中的文本值
				 String name = clientName.getText();
				 String password = new String(pwd.getPassword());
				 String struct = structCode.getText();
				 String verify = verifyCode.getText();
//				String name = "luojing";
//				String password = "CIci8287";
//				String struct = "440700851401";
				login(struct, name, password, verify);

			}
		});
		btnNewButton.setBounds(650, 14, 96, 73);
		frame.getContentPane().add(btnNewButton);

		JSeparator separator = new JSeparator();
		separator.setBounds(52, 197, 464, -94);
		frame.getContentPane().add(separator);

		clientName = new JTextField();
		clientName.setBounds(90, 10, 200, 30);
		frame.getContentPane().add(clientName);
		clientName.setColumns(10);

		fetch = new JButton("\u6293\u53D6");
		fetch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						ArrayList<String> links = getClientDetailLinks();
						textArea.append("共有企业基本情况表:" + links.size() + "户\n");
						
						//取得客户的单位基本情况表
						ArrayList<LinkedHashMap<String,String >> clients=new ArrayList<>();
						int n=1;
						for(String url :links) {
							clients.add(getClientTableColumnDetail(getClientInformationTable(url)));
							textArea.append("正在下载第"+n+"家企业情况表，共有"+links.size()+"家企业\n");
							n++;
						}
						
//						clients.add(getClientTableColumnDetail(getClientInformationTable(links.get(50))));
						ExcelToolService nts=new ExcelToolService();
						nts.exportToexcel(clients, "client.xls", "", "", "", "客户基本情况表");
						System.out.println("");
						
						
					}
				}).start();
				;

			}
		});
		fetch.setEnabled(false);
		fetch.setBounds(562, 449, 67, 23);
		frame.getContentPane().add(fetch);

		JButton button_1 = new JButton("\u5BFC\u51FA\u6570\u636E");
		button_1.setEnabled(false);
		button_1.setBounds(664, 450, 84, 23);
		frame.getContentPane().add(button_1);

		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(9, 436, 730, 4);
		frame.getContentPane().add(separator_1);

		JProgressBar progressBar = new JProgressBar();
		progressBar.setBounds(21, 449, 398, 19);
		frame.getContentPane().add(progressBar);

		JLabel lblNewLabel = new JLabel("New label");
		lblNewLabel.setBounds(457, 449, 53, 18);
		frame.getContentPane().add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("\u7528\u6237\u540D");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setBounds(30, 19, 53, 18);
		frame.getContentPane().add(lblNewLabel_1);

		JLabel label = new JLabel("\u5BC6\u7801");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setBounds(44, 57, 45, 18);
		frame.getContentPane().add(label);

		verifyCode = new JTextField();
		verifyCode.setColumns(10);
		verifyCode.setBounds(430, 50, 100, 30);
		frame.getContentPane().add(verifyCode);

		JLabel label_1 = new JLabel("\u9A8C\u8BC1\u7801");
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		label_1.setBounds(374, 57, 45, 18);

		frame.getContentPane().add(label_1);

		pwd = new JPasswordField();
		pwd.setBounds(90, 50, 200, 30);
		frame.getContentPane().add(pwd);

		JSeparator separator_2 = new JSeparator();
		separator_2.setBounds(16, 97, 730, 4);
		frame.getContentPane().add(separator_2);

		JLabel label_2 = new JLabel("\u673A\u6784\u7801");
		label_2.setHorizontalAlignment(SwingConstants.CENTER);
		label_2.setBounds(367, 19, 53, 18);
		frame.getContentPane().add(label_2);

		structCode = new JTextField();
		structCode.setColumns(10);
		structCode.setBounds(430, 10, 200, 30);
		frame.getContentPane().add(structCode);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(9, 115, 737, 309);
		frame.getContentPane().add(scrollPane);

		textArea = new JTextArea();
		textArea.setDropMode(DropMode.INSERT);
		scrollPane.setViewportView(textArea);

		image_verify = new JLabel();
		image_verify.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				refreshVerifyImage();
				textArea.append("已更新验证码。。。\n");
			}
		});
		image_verify.setIcon(new ImageIcon("verifyCode.png"));
		image_verify.setBounds(542, 57, 76, 23);

		frame.getContentPane().add(image_verify);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
