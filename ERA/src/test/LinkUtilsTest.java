package test;

import org.junit.Test;
import utils.LinkUtils;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class LinkUtilsTest {
    @Test
    public void testGetAllLinks() throws Exception {
        String text = "anytext https://test.com/test-test/core some other text	";

        ArrayList<String> links = LinkUtils.getAllLinks(text);
        assertEquals("https://test.com/test-test/core", links.get(0));

        String transformURLIntoLinks = LinkUtils.transformURLIntoLinks(links.get(0));
        assertEquals("<a target=\"_blank\" href='https://test.com/test-test/core'>https://test.com/test-test/core</a>", transformURLIntoLinks);

    }
}
