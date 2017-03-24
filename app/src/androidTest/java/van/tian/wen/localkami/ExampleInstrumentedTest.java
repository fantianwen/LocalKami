package van.tian.wen.localkami;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.core.deps.guava.base.Strings;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import van.tian.wen.library.LocalSaverManager;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("van.tian.wen.localkami", appContext.getPackageName());
    }

    @Test
    public void testLocalKami() {
        Context appContext = InstrumentationRegistry.getTargetContext();

        LocalSaverManager localSaverManager = LocalSaverManager.getInstance().context(appContext).keyFile("LOCAL_SAVER");

        localSaverManager.set("localSaverTest", "Hello,LocalSaver");

        String localSaverTest = localSaverManager.get("localSaverTest", String.class);

        System.out.print("localsaver===>" + localSaverTest);
    }

}
