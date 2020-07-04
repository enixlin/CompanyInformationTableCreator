package com.enixlin.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * 网络连接服务实现类
 * 
 * @author enixlin
 * 
 *
 */
public class NetService {

	private CloseableHttpClient httpClient;
	private HttpGet httpGet;
	private HttpPost httpPost;

	public CloseableHttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * 网络连接服务实现类NetService构造函数
	 */
	public NetService() {
		super();
		// TODO Auto-generated constructor stub
		this.httpClient = this.createHttpClient();
	}

	/**
	 * 创建一个http连接客户端，整个应用程序都会使用户个客户端进行http请求<br>
	 * 主要是用户登录验证和用户查询等操作
	 * 
	 * @return
	 */
	public CloseableHttpClient createHttpClient() {
		this.httpClient = HttpClients.createDefault();
		return this.httpClient;
	}

	/**
	 * 使用 get方法提交HTTP请求
	 * 
	 * @param requestUrl请求的地址
	 * @return 结果字符串
	 */
	public String HttpGet(String requestUrl) {
		String result = null;

		HttpGet httpGet = new HttpGet(requestUrl);
		try {
			CloseableHttpResponse response = this.httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toString(entity);

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;

	}

	public String HttpPost(String url, Map<String, String> map,
			String encoding) {

		String result=null;
    	String body="";
    	
    	HttpPost httpPost=new HttpPost(url);
    	 //装填参数
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        if(map!=null){
            for (Entry<String, String> entry : map.entrySet()) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }
        //设置参数到请求对象中
        try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, encoding));
			   //设置header信息
	        //指定报文头【Content-type】、【User-Agent】
	        //httpPost.setHeader("Content-type", "application/x-www-form-urlencoded");
	        //httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
	        
	        //执行请求操作，并拿到结果（同步阻塞）
	        CloseableHttpResponse response = this.httpClient.execute(httpPost);
	        //获取结果实体
	        HttpEntity entity = response.getEntity();
	        if (entity != null) {
	            //按指定编码转换结果实体为String类型
	            result = EntityUtils.toString(entity, encoding);
	        }
	        EntityUtils.consume(entity);
	        //释放链接
	        response.close();
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    	return result;
	}
	
	
	/**
	 * 从网络上下载图片
	 */
	public  void downloadPicture(String url, String dirPath,
			String filePath) {
		
		
		
		HttpGet httpget = new HttpGet(url);
 
		httpget
				.setHeader(
						"User-Agent",
						"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.79 Safari/537.1");
		httpget
				.setHeader("Accept",
						"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
 
		try {
			HttpResponse resp = this.httpClient.execute(httpget);
			if (HttpStatus.SC_OK == resp.getStatusLine().getStatusCode()) {
				HttpEntity entity = resp.getEntity();
 
				InputStream in = entity.getContent();
 
				savePicToDisk(in, dirPath, filePath);
				
				System.out.println("保存图片 "+filePath+" 成功....");
			}
 
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
		}
	}
 
	/**
	 * 将图片写到 硬盘指定目录下
	 * @param in
	 * @param dirPath
	 * @param filePath
	 */
	private static void savePicToDisk(InputStream in, String dirPath,
			String filePath) {
 
		try {
			File dir = new File(dirPath);
			if (dir == null || !dir.exists()) {
				dir.mkdirs();
			}
 
			//文件真实路径
			String realPath = dirPath.concat(filePath);
			File file = new File(realPath);
			if (file == null || !file.exists()) {
				file.createNewFile();
			}
 
			FileOutputStream fos = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len = 0;
			while ((len = in.read(buf)) != -1) {
				fos.write(buf, 0, len);
			}
			fos.flush();
			fos.close();
 
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


}