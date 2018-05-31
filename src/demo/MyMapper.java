package demo;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;


/*
 * ��ָ����־�ļ������м�¼���й���
 * ��־���ͣ�
 * 27.19.74.143 - - [30/May/2013:17:38:20 +0800] "GET /static/image/common/faq.gif HTTP/1.1" 200 1127
 */
public class MyMapper extends Mapper<LongWritable, Text, LongWritable, Text> {
	LogParser logParser = new LogParser();
	Text outputValue = new Text();
	
	@Override
	protected void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		final String[] parsed = logParser.parse(value.toString());
		
		//1�����˵���̬��Դ��������
		if(parsed[2].startsWith("GET /static/") || parsed[2].startsWith("GET /uc_server")){
			return;
		}
		
		//2�����˵���ͷ��ָ���ַ���
		if(parsed[2].startsWith("GET /")){
			parsed[2] = parsed[2].substring("GET /".length());
		}else if(parsed[2].startsWith("POST /")){
			parsed[2] = parsed[2].substring("POST /".length());
		}
		
		//3�����˵���β���ض��ַ���
		if(parsed[2].endsWith(" HTTP/1.1")){
			parsed[2] = parsed[2].substring(0, parsed[2].length() - " HTTP/1.1".length());
		}
		
		//4��ֻд��ǰ������¼������
		outputValue.set(parsed[0] + "\t" + parsed[1] + "\t" + parsed[2]);
		context.write(key, outputValue);
	}
	
}
