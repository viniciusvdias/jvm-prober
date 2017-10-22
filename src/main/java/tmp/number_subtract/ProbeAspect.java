package number_subtract;

import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

@Aspect
public class ProbeAspect {
  private static final ThreadLocal<encoder.NumberSubtractToAddEncoder> encodertl =
     new ThreadLocal<encoder.NumberSubtractToAddEncoder>() {
        @Override
        protected encoder.NumberSubtractToAddEncoder initialValue() {
           return new encoder.NumberSubtractToAddEncoder();
        }
     };

  private static final ThreadLocal<StringBuilder> buildertl =
     new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
           return new StringBuilder(256);
        }
     };


  @Around("call(example.Number example.Number.subtract(int))")
  public Object probe(ProceedingJoinPoint point) throws Throwable {

     Object caller = point.getTarget();
     Object[] args = point.getArgs();

     long ostart = System.currentTimeMillis();
     Object originalReturn = point.proceed(args);
     long oelapsed = System.currentTimeMillis() - ostart;
     
     long nstart = System.currentTimeMillis();
     Object newReturn = encodertl.get().encode(caller, args, originalReturn);
     long nelapsed = System.currentTimeMillis() - nstart;
     
     StringBuilder builder = buildertl.get();
     builder.setLength(0);
     builder.append("[JvmProber] Timestamp(yyyy.MM.dd.HH.mm.ss)=");
     builder.append(new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
     builder.append(" ProbeName=number_subtract");
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