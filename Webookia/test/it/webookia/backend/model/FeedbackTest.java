package it.webookia.backend.model;

import org.slim3.tester.AppEngineTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class FeedbackTest extends AppEngineTestCase {

    private Feedback model = new Feedback();

    @Test
    public void test() throws Exception {
        assertThat(model, is(notNullValue()));
    }
}
