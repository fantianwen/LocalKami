package van.tian.wen.localkami;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.Serializable;
import java.util.ArrayList;

import van.tian.wen.library.LocalSaver;
import van.tian.wen.library.LocalSaverManager;

import static org.junit.Assert.assertEquals;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest implements Serializable{
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("van.tian.wen.localkami", appContext.getPackageName());
    }

    @Test
    public void testLocalKami() {
        Context appContext = InstrumentationRegistry.getTargetContext();

        LocalSaverManager.getInstance().install(appContext);

        ArrayList<String> strings = new ArrayList<>();
        strings.add("dddd");
        strings.add("second");
        strings.add("third");

        LocalSaver dfadfad = new LocalSaver("dfadfad");

        dfadfad.set("localSaverTester", strings);

        ArrayList localSaverTest = dfadfad.get("localSaverTester");
        System.out.print(localSaverTest);

    }


}
