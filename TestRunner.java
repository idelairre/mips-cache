import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

class TestRunner {
  public static void main(String[] args) {
    if (args.length != 0 && args[0].equals("test")) {
      Cache.test = true;
    }
    if (Cache.test) {
      Result result = JUnitCore.runClasses(TestSuite.class);
      for (Failure failure : result.getFailures()) {
         System.out.println(failure.toString());
      }
      System.out.println("Passed? " + result.wasSuccessful());

    } else {
      TestSuite.run();
    }
  }
}
