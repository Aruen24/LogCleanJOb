package demo;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class MyReducer extends Reducer<LongWritable, Text, Text, NullWritable> {

	@Override
	protected void reduce(LongWritable k3, Iterable<Text> v3,
			Context context) throws IOException, InterruptedException {
		for(Text v : v3){
			//Êä³ök4µ¥´Ê£¬v4
			context.write(v, NullWritable.get());
		}
	}
	
}
