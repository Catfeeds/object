package com.zw.p2p.gps.service.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Properties;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Service;

import com.zw.core.util.DateUtil;
import com.zw.core.util.HttpClientUtil;
import com.zw.core.util.MD5Util;
import com.zw.p2p.gps.service.GpsService;

@Service("gpsService")
public class GpsServiceImpl implements GpsService {

	private static Properties props = new Properties();
	
	private String gpsUrl;
	
	static{
		try {
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("gps.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getGpsUrl() {
		if (gpsUrl==null){
			gpsUrl = props.getProperty("GPS_URL");
		}
		return gpsUrl;
	}
	
	
	/**
	 * 根据imei号码查询经度、纬度
	 * @author majie
	 * @return（经度,纬度）
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 * @date 2016年9月20日 下午4:52:02
	 */
	@Override
	public String getRemoteCoordinate(String imei){
		String mdtstr = imei+DateUtil.DateToString(new Date(),"yyyyMMdd")+"DAEDFFE767815EC894E6D625D1E32348";
		mdtstr=MD5Util.encrypt(mdtstr).toUpperCase();
		String acturl=getGpsUrl()+"?IMEI="+imei+"&TimeZone=China%20Standard%20Time&MapType=Baidu&CheckKey="+mdtstr;
        String result = HttpClientUtil.getResponseBodyAsString(acturl);
        result = result == null?null:result.replaceAll("<[^>]*>","");
        try {
        	JSONObject json = JSONObject.fromObject(result);
            if("0".equals(json.getString("state"))){
            	return json.getString("longitude")+","+json.getString("latitude");
            }
		} catch (Exception e) {
			// TODO: handle exception
		}
        System.out.println("请求地址：："+acturl);
        System.out.println(result);
        return null;
	}
	
//	/**
//	 * 根据imei号码查询经度、纬度
//	 * @author majie
//	 * @return（经度,纬度）
//	 * @throws NoSuchAlgorithmException
//	 * @throws UnsupportedEncodingException
//	 * @date 2016年9月20日 下午4:52:02
//	 */
//	@Override
//	public String getRemoteCoordinate(String imei)
//			throws NoSuchAlgorithmException, UnsupportedEncodingException {
//		String mdtstr = imei+DateUtil.DateToString(new Date(),"yyyyMMdd")+"DAEDFFE767815EC894E6D625D1E32348";
//		mdtstr=MD5Util.encrypt(mdtstr).toUpperCase();
//        String url = "http://modelapi.aichache.cn/OpenAPIModel.asmx";
//        String method = "GetTracking";
//
//        Options options = new Options();   
//        EndpointReference targetEPR = new EndpointReference(url); 
//        options.setTo(targetEPR); 
//        options.setAction("http://tempuri.org/GetTracking");
//        try {
//            RPCServiceClient serviceClient = new RPCServiceClient();
//            serviceClient.setOptions(options);
//            // 在创建QName对象时，QName类的构造方法的第一个参数表示WSDL文件的命名空间名，也就是<wsdl:definitions>元素的targetNamespace属性值
//            QName opAddEntry = new QName(url, method);
//            // 参数，如果有多个，继续往后面增加即可，不用指定参数的名称,到时候会默认未arg0,arg1,arg2,arg3....
//            Object[] opAddEntryArgs = new Object[]{imei,"China Standard Time","Baidu",mdtstr};
//            /*invokeBlocking方法有三个参数，其中第一个参数的类型是QName对象，表示要调用的方法名；
//			            第二个参数表示要调用的WebService方法的参数值，参数类型为Object[]；
//			            第三个参数表示WebService方法的返回值类型的Class对象，参数类型为Class[]。
//			            当方法没有参数时，invokeBlocking方法的第二个参数值不能是null，而要使用new Object[]{}
//			            如果被调用的WebService方法没有返回值，应使用RPCServiceClient类的invokeRobust方法，
//			            该方法只有两个参数，它们的含义与invokeBlocking方法的前两个参数的含义相同*/
//            Class[] classes = new Class[]{Object.class};
//            OMTextImpl omText = (OMTextImpl) serviceClient.invokeBlocking(opAddEntry, opAddEntryArgs, classes)[0];
//            JSONObject json = JSONObject.fromObject(omText.getText());
//            if("0".equals(json.getString("state"))){
//            	return json.getString("latitude")+","+json.getString("longitude");
//            }
//            System.out.println(omText.getText());
//        } catch (AxisFault axisFault) {
//            axisFault.printStackTrace();
//        }
//        return null;
//	}

}
