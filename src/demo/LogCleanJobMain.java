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
		// 创建一个job: job = map + reduce
		 Job job = Job.getInstance(new Configuration());
		 
		 //指定任务的入口
		 job.setJarByClass(LogCleanJobMain.class);
		 
		 //指定任务的Mapper和数据的输出类型: k2,v2
		 job.setMapperClass(MyMapper.class);
		 job.setMapOutputKeyClass(LongWritable.class);//指定k2
		 job.setMapOutputValueClass(Text.class);//指定v2
		 
		 //指定任务的Reducer和数据的输出类型: k4,v4
		 job.setReducerClass(MyReducer.class);
		 job.setOutputKeyClass(Text.class);//指定k4
		 job.setOutputValueClass(NullWritable.class);//指定v4
		 
		 //指定任务的输入路径（map）,输出路径（reducer）
		 FileInputFormat.setInputPaths(job, new Path(args[0]));
		 FileOutputFormat.setOutputPath(job, new Path(args[1]));
		 
		 //执行任务
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
