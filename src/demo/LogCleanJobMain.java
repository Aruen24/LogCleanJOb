package demo;



import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;




public class LogCleanJobMain {

	public static void main(String[] args) throws Exception {
		// ����һ��job: job = map + reduce
		 Job job = Job.getInstance(new Configuration());
		 
		 //ָ����������
		 job.setJarByClass(LogCleanJobMain.class);
		 
		 //ָ�������Mapper�����ݵ��������: k2,v2
		 job.setMapperClass(MyMapper.class);
		 job.setMapOutputKeyClass(LongWritable.class);//ָ��k2
		 job.setMapOutputValueClass(Text.class);//ָ��v2
		 
		 //ָ�������Reducer�����ݵ��������: k4,v4
		 job.setReducerClass(MyReducer.class);
		 job.setOutputKeyClass(Text.class);//ָ��k4
		 job.setOutputValueClass(NullWritable.class);//ָ��v4
		 
		 //ָ�����������·����map��,���·����reducer��
		 FileInputFormat.setInputPaths(job, new Path(args[0]));
		 FileOutputFormat.setOutputPath(job, new Path(args[1]));
		 
		 //ִ������
//		 job.waitForCompletion(true);
		 boolean success = job.waitForCompletion(true);
	        if(success){
	            System.out.println("Clean process success!");
	        }
	        else{
	            System.out.println("Clean process failed!");
	        }
	}
	
}
