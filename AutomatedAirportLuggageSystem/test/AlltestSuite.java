package test;



import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        TaskManagerTest.class,
        AGVTest.class,
        QueueManageTest.class,
        LogServiceTest.class,
     
})


public class AlltestSuite {

}
