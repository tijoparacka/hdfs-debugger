package com.examples;

import com.google.common.base.Strings;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;


public class Test
{

  //private Properties props = null;
  public static void main( String[] args ) throws Exception
  {
    Test test = new Test();
    test.test(args);
  }
  public void test(String[] args) throws Exception
  {
    final Configuration conf = new Configuration();

    // Set explicit CL. Otherwise it'll try to use thread context CL, which may not have all of our dependencies.
    conf.setClassLoader(getClass().getClassLoader());

    // Ensure that FileSystem class level initialization happens with correct CL
    // See https://github.com/apache/druid/issues/1714
    ClassLoader currCtxCl = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      FileSystem.get(conf);
    }
    catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    finally {
      Thread.currentThread().setContextClassLoader(currCtxCl);
    }

//        if (props != null) {
//            for (String propName : props.stringPropertyNames()) {
//                if (propName.startsWith("hadoop.")) {
//                    conf.set(propName.substring("hadoop.".length()), props.getProperty(propName));
//                }
//            }
//        }

    authenticate(conf,args[0],args[1]);
    Job job = Job.getInstance(conf);

    FileInputFormat.addInputPaths(job, args[2]);

    System.out.println("Printing the hdfs paths ");
    Set<Path> format = new HdfsFileInputFormat().getSplits(job)
                                                .stream()
                                                .filter(split -> ((FileSplit) split).getLength() > 0)
                                                .map(split -> ((FileSplit) split).getPath())
                                                .collect(Collectors.toSet());
    format.forEach(x-> System.out.println(x));
  }
  public void authenticate(Configuration hadoopConf, String principal,String keytab ) throws Exception
  {

    if (!Strings.isNullOrEmpty(principal) && !Strings.isNullOrEmpty(keytab)) {
      UserGroupInformation.setConfiguration(hadoopConf);
      if (UserGroupInformation.isSecurityEnabled()) {
        try {
          if (UserGroupInformation.getCurrentUser().hasKerberosCredentials() == false
              || !UserGroupInformation.getCurrentUser().getUserName().equals(principal)) {
            System.out.println("Trying to authenticate user"+principal+ " with keytab "+keytab+".");
            UserGroupInformation.loginUserFromKeytab(principal, keytab);
            System.out.println("Authentication with principal "+principal +" successful ..");
          }
        }
        catch (IOException e) {
          e.printStackTrace();
          //  throw new Exception( "Failed to authenticate user principal "+ principal + "with keytab "+ keytab);
        }
      }

    }

  }

  public static class HdfsFileInputFormat extends FileInputFormat<Object, Object>
  {
    @Override
    public RecordReader<Object, Object> createRecordReader(
        org.apache.hadoop.mapreduce.InputSplit inputSplit,
        TaskAttemptContext taskAttemptContext
    )
    {
      throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isSplitable(JobContext context, Path filename)
    {
      return false;  // prevent generating extra paths
    }
  }
}
