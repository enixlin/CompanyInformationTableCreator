
package com.enixlin.ui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
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

import org.apache.http.impl.client.CloseableHttpClient;

import com.enixlin.utils.NetService;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JScrollPane;

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
	}

	/**
	 * 处理用户登录asone系统
	 * 
	 * @param structCode 机构码
	 * @param name       用户 名
	 * @param password   用户密码
	 * @param verifyCode 验证码
	 */
	public void login(String structCode, String name, String password, String verifyCode) {

		// 请求的网址
		String requestUrl = "";
		// 请求参数
		Map<String, String> postParams = new LinkedHashMap<>();
		// 请求返回结果
		String result = "";

		// 对密码进行MD5加密,使用jdk里的scriptEngine运行md5.js里的hex_md5函数
		// document.all.pwd.value=hex_md5( pwdValue );
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
		requestUrl = "http://zwfw.safe.gov.cn/asone/jsp/checkCode.jsp";
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
//		System.out.println("checkCode return result is:" + result);
		
		//从返回的请求响应html中取得加密的验证码
		int start = result.indexOf("name=\"safeValidateCode\" value=\"")+31 ;
		String encrytVerifyCode = result.substring(start, start + 32);
		System.out.println(encrytVerifyCode);
		
		/*
		 * 用户登录 请求地址：http://zwfw.safe.gov.cn/asone/servlet/UniLoginServlet 登录使用POST方式
		 * ，请求参数如下： orgCode: 075093053 userCode: billluo1
		 * pwd:3e6c7d141e32189c917761138b026b74 backUrl: null enterUrl: null
		 * safeValidateCode: 340ed3cb761fd8ca6146544e8bff0302 user_radio: 2 loginType:
		 * biztype: null
		 */
		requestUrl = "http://zwfw.safe.gov.cn/asone/servlet/UniLoginServlet";
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
		System.out.println(result);

	}

	/**
	 * 更新验证码
	 */
	public void refreshVerifyImage() {
		// 先请求登录页面，里面有一个IMage 的src
		String html = this.ns.HttpGet("http://zwfw.safe.gov.cn/asone/WelcomeServlet?code=90000&flag=false");

		int start = html.indexOf("/asone/jsp/code.jsp") + 28;
		String refreshCode = html.substring(start, start + 13);
		String url = "http://zwfw.safe.gov.cn/asone/jsp/code.jsp?refresh=" + refreshCode;
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
//				取得各个输入框中的文本值
//				String name = clientName.getText();
//				String password = new String(pwd.getPassword());
//				String struct = structCode.getText();
				String verify = verifyCode.getText();
				String name = "billluo1";
				String password = "123456Aa";
				String struct = "075093053";
				
				login(struct,name,password,verify);



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

		JButton button = new JButton("\u6293\u53D6");
		button.setEnabled(false);
		button.setBounds(562, 449, 67, 23);
		frame.getContentPane().add(button);

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
