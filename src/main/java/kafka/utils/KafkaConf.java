package kafka.utils;

import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zk.client.ZkExecutor;
import zk.utils.ZkConf;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hp on 14-12-12.
 */
public class KafkaConf {

    private static Logger logger = LoggerFactory.getLogger(KafkaConf.class);

    public static String brokerList = "localhost:9092";//"12:9092,13.9092,14:9092"
    public static int port = 9092;
    public static String zk = "localhost:2181";
    public static String serializer = "kafka.serializer.DefaultEncoder";//default is byte[]
    public static String keySerializer = "kafka.serializer.StringEncoder";//default is message's byte[]
    public static String partitioner = "kafka.producer.DefaultPartitioner";
    public static String acks = "1";
    public static String sendBufferSize = String.valueOf(1024 * 1024);//1MB
    public static String topic;//queue topic
    public static int partition = 0;
    public static List<String> topics = new ArrayList<String>();//distribute the multiple topic
    public static List<String> brokerSeeds = new ArrayList<String>();//"12,13,14"
    public static List<Integer> portList = new ArrayList<Integer>();//9092 9093 9094
    public static int readBufferSize = 1 * 1024 * 1024;//1 MB
    public static String clientName = "cc456687IUGHG";

    //load the zkPos to find the bokerList and port zkPos : 172.17.36.60:2181/kafka
    public static void loadZk(String zkPos) throws Exception {
        logger.info("parser load the string : " + zkPos);
        if(zkPos == null) throw new Exception("zk path is null");
        String[] ss = zkPos.split("/");
        String zkServer = "";
        String zkPath = "";
        for(int i = 0; i<= ss.length - 1; i++) {
            logger.info("!!!!! debug : " + ss[i]);
            if(i == 0) {
                zkServer = ss[i];
            } else {
                zkPath += ("/" + ss[i]);
            }
        }
        zkPath += ("/brokers/ids");
        ZkConf zcnf = new ZkConf();
        zcnf.zkServers = zkServer;
        logger.info("load conf : " + zcnf.zkServers);
        ZkExecutor zkexe = new ZkExecutor(zcnf);
        zkexe.connect();
        logger.info("load path : " + zkPath);
        List<String> ids = zkexe.getChildren(zkPath);
        brokerList = "";
        brokerSeeds.clear();
        portList.clear();
        for(String brokerNode : ids) {
            String zkNodeJson = zkexe.get(zkPath + "/" + brokerNode);
            if(zkNodeJson == null) continue;
            JSONObject jo = JSONObject.fromObject(zkNodeJson);
            String host = jo.getString("host");
            int port = jo.getInt("port");
            brokerSeeds.add(host);
            portList.add(port);
            brokerList += (host + ":" + port + ",");
            logger.info("load zk host and port: " + host + " # " + port);
        }
        brokerList = brokerList.substring(0, brokerList.lastIndexOf(","));
        zkexe.close();
    }

}