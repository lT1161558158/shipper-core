package top.trister.shipper.core.implHandler

import top.trister.shipper.core.event.Event
import top.trister.shipper.core.exception.ShipperException
import org.junit.Before
import org.junit.Test
import top.trister.shipper.core.implHandler.codec.SimpleCodec

import java.util.concurrent.TimeUnit

import static junit.framework.TestCase.assertEquals
import static junit.framework.TestCase.assertTrue

class StdInputTest {

    private StdInput input
    @Before
    void setUp() {
        input = new StdInput()
        input.codec(new SimpleCodec())
    }

    @Test
    void readNormal() {
        String inputMessage = "hello"
        input.scanner = new Scanner(new ByteArrayInputStream((inputMessage + input.delimiter).getBytes()))
        assertTrue(input.ready())
        assertEquals(input.read().get(Event.MESSAGE), inputMessage)
    }

    @Test(expected = ShipperException.class)
    void readTimeout() {
        input.scanner = new Scanner(new ByteArrayInputStream(("").getBytes()))
        input.read(TimeUnit.MICROSECONDS, 50)
    }
}