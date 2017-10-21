package sparkcontext_contructor;

import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Aspect
public class ProbeAspect {
  private static final ThreadLocal<encoder.Encoder> encodertl =
     new ThreadLocal<encoder.Encoder>() {
        @Override
        protected encoder.Encoder initialValue() {
           return new encoder.Encoder();
        }
     };

  private static final ThreadLocal<StringBuilder> buildertl =
     new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
           return new StringBuilder(256);
        }
     };


  @Around("call( org.apache.spark.SparkContext.new(org.apache.spark.SparkConf))")
  public Object probe(ProceedingJoinPoint point) throws Throwable {

     Object[] args = point.getArgs();

     long ostart = System.currentTimeMillis();
     Object originalReturn = point.proceed(args);
     long oelapsed = System.currentTimeMillis() - ostart;
     
     long nstart = System.currentTimeMillis();
     Object newReturn = encodertl.get().encode(args, originalReturn);
     long nelapsed = System.currentTimeMillis() - nstart;
     
     StringBuilder builder = buildertl.get();
     builder.setLength(0);
     builder.append("[JvmProber] Timestamp(yyyy.MM.dd.HH.mm.ss)=");
     builder.append(new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
     builder.append(" ProbeName=sparkcontext_contructor");
     builder.append(" MethodSignature=");
     builder.append(point.getSignature());
     builder.append(" Arguments=");
     builder.append(Arrays.toString(point.getArgs()));
     builder.append(" OriginalReturn=");
     builder.append(originalReturn);
     builder.append(" OriginalReturnElapsedTime=");
     builder.append(oelapsed);
     builder.append(" NewReturn=");
     builder.append(newReturn);
     builder.append(" NewReturnElapsedTime=");
     builder.append(nelapsed);

     System.out.println(builder.toString());
     
     return newReturn;
  }
}